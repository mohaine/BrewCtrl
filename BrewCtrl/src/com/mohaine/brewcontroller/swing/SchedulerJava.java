package com.mohaine.brewcontroller.swing;

import java.util.Timer;
import java.util.TimerTask;

import com.mohaine.brewcontroller.client.display.Scheduler;

public class SchedulerJava implements Scheduler {

	private static class State {
		final Timer t = new Timer();
		private long delayMs;
		private final RunRepeat run;
		private TimerTask timerTask;

		public State(RunRepeat run, long delayMs) {
			super();
			this.run = run;
			this.delayMs = delayMs;
		}

		public void schedule() {
			timerTask = new TimerTask() {
				@Override
				public void run() {
					delayMs = State.this.run.run();
					if (delayMs > -1) {
						State.this.schedule();
					}
				}
			};
			t.schedule(timerTask, delayMs);
		}

		public void cancelIfNeeded() {
			if (timerTask != null) {
				timerTask.cancel();
				timerTask = null;
			}

		}
	}

	@Override
	public Cancelable scheduleReapeating(final RunRepeat run, final long delayMs) {
		final State s = new State(run, delayMs);
		s.schedule();
		return new Cancelable() {
			@Override
			public void cancel() {
				s.cancelIfNeeded();
			}
		};
	}

}
