package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.CharsWrapper;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author TheElectronWill
 */
final class WriteAsyncFileConfig<C extends Config> extends ConfigWrapper<C> implements FileConfig {
	private final File file;
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

	private final ConfigWriter<? super C> writer;
	private final WriteCompletedHandler writeCompletedHandler;
	private final OpenOption[] openOptions;

	private final ConfigParser<?, ? super C> parser;
	private final FileNotFoundAction nefAction;
	private final ParsingMode parsingMode;

	WriteAsyncFileConfig(C config, File file, Charset charset, ConfigWriter<? super C> writer,
						 WritingMode writingMode, ConfigParser<?, ? super C> parser,
						 ParsingMode parsingMode, FileNotFoundAction nefAction) {
		super(config);
		this.file = file;
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
	public File getFile() {
		return file;
	}

	@Override
	public void save() {
		if (closed.get()) {
			throw new IllegalStateException("Cannot save a closed FileConfig");
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
					channel = AsynchronousFileChannel.open(file.toPath(), openOptions);
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
			throw new IllegalStateException("Cannot (re)load a closed FileConfig");
		}
		parser.parse(file, config, parsingMode, nefAction);//blocking read, not async
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
			throw new WritingException("Error while saving the FileConfig to " + file, exc);
		}
	}
}