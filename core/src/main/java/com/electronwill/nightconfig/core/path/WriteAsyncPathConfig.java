package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.*;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author TheElectronWill
 */
final class WriteAsyncPathConfig<C extends Config> extends ConfigWrapper<C> implements PathConfig {
	private final Path path;
	private final Charset charset;
	/**
	 * True if this file config has been closed.
	 */
	private final AtomicBoolean closed = new AtomicBoolean();
	/**
	 * The channel used to write asynchronously to the file.
	 */
	private AsynchronousFileChannel channel;
	/**
	 * Guards the channel to prevent it from being used and closed at the same time.
	 */
	private final Object channelGuard = new Object();
	/**
	 * True if there is a write operation in progress.
	 */
	private final AtomicBoolean currentlyWriting = new AtomicBoolean();
	/**
	 * True if the config has changed during the write operation, and thus must be written again.
	 */
	private final AtomicBoolean mustWriteAgain = new AtomicBoolean();

	private final ConfigWriter writer;
	private final WriteCompletedHandler writeCompletedHandler;
	private final OpenOption[] openOptions;

	private final ConfigParser<?> parser;
	private final PathNotFoundAction nefAction;
	private final ParsingMode parsingMode;

	WriteAsyncPathConfig(C config, Path path, Charset charset, ConfigWriter writer,
						 WritingMode writingMode, ConfigParser<?> parser,
						 ParsingMode parsingMode, PathNotFoundAction nefAction) {
		super(config);
		this.path = path;
		this.charset = charset;
		this.writer = writer;
		this.parser = parser;
		this.parsingMode = parsingMode;
		this.nefAction = nefAction;
		if (writingMode == WritingMode.APPEND) {
			this.openOptions = new OpenOption[]{WRITE, CREATE};
		} else {
			this.openOptions = new OpenOption[]{WRITE, CREATE, TRUNCATE_EXISTING};
		}
		this.writeCompletedHandler = new WriteCompletedHandler();
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public void save() {
		if (closed.get()) {
			throw new IllegalStateException("Cannot save a closed PathConfig");
		}
		save(true);
	}

	@Override
	public void close() {
		if (closed.compareAndSet(false, true)) {// The content of this block is called only once
			synchronized (channelGuard) {
				while (currentlyWriting.get()) {// Writing in progres
					// Waits for the operation to complete, to ensure that the data is written:
					try {
						channelGuard.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;// Exits from the loop and returns from the method
					}
				}
			}
		}
	}

	private void save(boolean saveLaterIfWriting) {
		boolean canSaveNow = currentlyWriting.compareAndSet(false,
															true);// atomically sets to true if false
		if (canSaveNow) {// no writing is in progress: start one immediately
			// Writes the config data to a ByteBuffer
			CharsWrapper.Builder builder = new CharsWrapper.Builder(512);
			writer.write(config, builder);
			CharBuffer chars = CharBuffer.wrap(builder.build());
			ByteBuffer buffer = charset.encode(chars);

			// Writes the ByteBuffer to the file, asynchronously
			synchronized (channelGuard) {
				try {
					channel = AsynchronousFileChannel.open(path, openOptions);
					channel.write(buffer, channel.size(), null, writeCompletedHandler);
				} catch (IOException e) {
					writeCompletedHandler.failed(e, null);
				}
			}
		} else if (saveLaterIfWriting) {// there is a writing in progress: start one later
			mustWriteAgain.set(true);
		}
	}

	@Override
	public void load() {
		if (closed.get()) {
			throw new IllegalStateException("Cannot (re)load a closed PathConfig");
		}
		if (!currentlyWriting.get()) { // Skips load when writing
			parser.parse(path, config, parsingMode, nefAction);//blocking read, not async
		}
	}

	private final class WriteCompletedHandler implements CompletionHandler<Integer, Object> {
		@Override
		public void completed(Integer result, Object attachment) {
			currentlyWriting.set(false);// Resets currentlyWriting
			if (mustWriteAgain.getAndSet(false)) {// Gets and resets mustWriteAgain
				save(false);// Saves the config without setting mustWriteAgain to true if canSaveNow is false
			} else {
				/* All operations have completed and we don't need to start a new one. Therefore
				the channel may be closed. */
				synchronized (channelGuard) {
					try {
						channel.close();
						channel = null;
					} catch (IOException e) {
						failed(e, null);
					} finally {
						channelGuard.notify();// Notifies the waiter (if any). See method close()
					}
				}
			}
		}

		@Override
		public void failed(Throwable exc, Object attachment) {
			throw new WritingException("Error while saving the PathConfig to " + path, exc);
		}
	}
}