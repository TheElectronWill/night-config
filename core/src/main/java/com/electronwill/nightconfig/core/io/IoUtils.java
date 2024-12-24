package com.electronwill.nightconfig.core.io;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * IO utilities for INTERNAL use only (do not use oustide of night-config).
 */
public final class IoUtils {
	/**
	 * Like {@code Runnable}, but with a throwable {@code IOException}.
	 */
	@FunctionalInterface
	public static interface IoRunnable {
		void run() throws IOException;
	}

	public static final class RetryFailedException extends IOException {
		RetryFailedException(String msg, IOException cause) {
			super(msg, cause);
		}
	}

	static class OptionHolder {
		static final long RETRY_DELAY_MILLIS;
		static final int RETRY_MAX_TIMES;

		static {
			boolean isWindows = System.getProperty("os.name", "?").trim().toLowerCase().startsWith("windows");
			// Default max delay (delay*times) per OS (chosen arbitrarily, knowing that most
			// issues happen on Windows):
			// - Windows: 1.5s (3 retrys)
			// - Others: 0.5s (1 try + 500ms delay + 1 retry)

			String delayProps = System.getProperty("nightconfig.accessDeniedRetryDelayMillis", "?");
			long delay;
			try {
				delay = Long.parseLong(delayProps);
			} catch (NumberFormatException ex) {
				delay = 500;
			}

			String timesProps = System.getProperty("nightconfig.accessDeniedRetryMaxTimes", "?");
			int times;
			try {
				times = Integer.parseInt(timesProps);
			} catch (NumberFormatException ex) {
				times = isWindows ? 3 : 1;
			}

			RETRY_DELAY_MILLIS = delay;
			RETRY_MAX_TIMES = times;
		}
	}

	static String[] splitOnce(String s, char c) {
		int i = s.lastIndexOf(c);
		if (i < 0) {
			return new String[] { s };
		} else {
			return new String[] { s.substring(0, i), s.substring(i + 1, s.length()) };
		}
	}

	/**
	 * Generates a filename (not path) for a temporary config file, for use with {@link WritingMode#REPLACE_ATOMIC}.
	 * Tries to keep the extension of the original file, to make it easier to find config files and to add them to
	 * file scanning whitelist (see the issue related to Windows Defender locking config files).
	 *
	 * @param originalFile the original config file
	 * @return a filename for the temporary file (the file is not created by this method)
	 */
	public static String tempConfigFileName(Path originalFile) {
		String filename = originalFile.toString();
		String[] parts = splitOnce(filename, '.');
		if (parts.length == 1) {
			return filename + ".new.tmp";
		} else {
			return parts[0] + ".new.tmp." + parts[1];
		}
	}

	/**
	 * Run an IO operation and retry it (at most {@code maxRetries} retries) if it
	 * fails with {@code AccessDeniedException}.
	 * See https://github.com/TheElectronWill/night-config/issues/183.
	 *
	 * @param name a name to print in error messages
	 * @param r the operation to run
	 * @throws IOException if it fails after retrying too many times, or for an error other than {@code AccessDeniedException}
	 */
	public static void retryIfAccessDenied(String name, IoRunnable r) throws IOException {
		// load the default values from java properties, keep them in memory once loaded
		retryIfAccessDenied(
			name,
			r,
			OptionHolder.RETRY_MAX_TIMES,
			OptionHolder.RETRY_DELAY_MILLIS,
			TimeUnit.MILLISECONDS
		);
	}

	public static void retryIfAccessDenied(String name, IoRunnable r, int maxRetries, long retryDelay,
			TimeUnit delayUnit)
			throws IOException {
		AccessDeniedException lastException = null;
		for (int i = 0; i <= maxRetries; i++) {
			try {
				r.run();
				return;
			} catch (AccessDeniedException ex) {
				// The file may be locked by another application (like an antivirus),
				// try again after some time
				lastException = ex;
				try {
					Thread.sleep(delayUnit.toMillis(retryDelay));
				} catch (InterruptedException e) {
					// ignore
				}
			} catch (IOException ex) {
				throw ex;
			}
		}
		String msg = String.format("IO operation '%s' failed after %s attempts", name, maxRetries);
		throw new RetryFailedException(msg, lastException);
	}
}
