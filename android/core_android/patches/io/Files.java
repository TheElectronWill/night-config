package com.electronwill.nightconfig.core.io;

import java.io.*;

/**
 * Implements some functions of java.nio.file.Files without the package java.nio.file,
 * in order to be compatible with Android &lt; 8.
 * <p>
 * Don't use this class in your application! It's only there for internal use.
 */
public final class Files {
	private Files() {} // utility class can't be constructed

	public static boolean notExists(File file) {
		return !file.exists();
	}

	public static File createFile(File file) throws IOException {
		boolean created = file.createNewFile();
		if (!created) {
			throw new IOException("The file already exists and cannot be created: " + file);
		}
		return file;
	}

	public static InputStream newInputStream(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

	public static OutputStream newOutputStream(File file, boolean create, boolean append) throws FileNotFoundException {
		if (!create && !file.exists()) {
			throw new FileNotFoundException("This file doesn't exist and was requested not to be created: " + file);
		}
		return new FileOutputStream(file, append);
	}

	public static void copy(InputStream from, File to) throws IOException {
		try (OutputStream out = new FileOutputStream(to)) {
			byte[] bytes = new byte[4096];
			int read;
			while ((read = from.read(bytes)) > 0) {
				out.write(bytes, 0, read);
			}
		}
	}

	public static void copy(File from, File to) throws IOException {
		try (InputStream in = new FileInputStream(from)) {
			copy(in, to);
		}
	}
}
