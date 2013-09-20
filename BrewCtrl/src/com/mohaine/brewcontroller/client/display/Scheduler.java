package com.mohaine.brewcontroller.client.display;

public interface Scheduler {

	public interface RunRepeat {
		long run();
	}

	public interface Cancelable {
		void cancel();
	}

	public Cancelable scheduleReapeating(RunRepeat run, long delayMs);
}
