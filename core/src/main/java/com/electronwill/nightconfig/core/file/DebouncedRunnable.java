package com.electronwill.nightconfig.core.file;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

final class DebouncedRunnable {
	private final Runnable runnable;
	private final long debounceTimeNanos;
	private ScheduledFuture<?> scheduledTask;

  	public DebouncedRunnable(Runnable runnable, Duration debounceTime) {
		this.runnable = runnable;
		this.debounceTimeNanos = debounceTime.toNanos();
	}

	private DebouncedRunnable(Runnable runnable, long debounceTimeNanos, ScheduledFuture<?> scheduledTask) {
		this.runnable = runnable;
		this.debounceTimeNanos = debounceTimeNanos;
		this.scheduledTask = scheduledTask;
	}

	/**
	 * Runs the underlying {@link Runnable} after the debounce time has elapsed,
	 * if {@code run} is not called again before its execution.
	 */
	public void run(ScheduledExecutorService executor) {
		if (scheduledTask != null) {
			// cancel the previously scheduled execution (it's ok if it has already been completed or cancelled)
			scheduledTask.cancel(false);
		}
		// schedule the new execution after the debouncing delay
		scheduledTask = executor.schedule(runnable, debounceTimeNanos, TimeUnit.NANOSECONDS);
	}

	/** Combine this runnable with another one, while keeping the debouncing state. */
	public DebouncedRunnable andThen(Runnable then) {
		Runnable combined = () -> {
			runnable.run();
			then.run();
		};
		return new DebouncedRunnable(combined, debounceTimeNanos, scheduledTask);
	}
}
