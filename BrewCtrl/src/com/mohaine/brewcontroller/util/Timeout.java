package com.mohaine.brewcontroller.util;

public class Timeout {

	private Run run;

	public Timeout() {
	}

	public synchronized void start(int millis, Runnable runnable) {

		cancel();

		run = new Run();
		run.active = true;
		run.millis = millis;
		run.runnable = runnable;

		run.thread = new Thread(run);
		run.thread.start();
	}

	public synchronized void cancel() {
		if (run != null) {
			run.active = false;
			run.thread.interrupt();
			run = null;
		}
	}

	private static class Run implements Runnable {
		public Thread thread;
		private long millis;
		private boolean active;
		private Runnable runnable;

		@Override
		public void run() {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				// ignore
			}

			if (active) {
				runnable.run();
			}

		}
	}

}
