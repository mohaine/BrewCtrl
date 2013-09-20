package com.mohaine.brewcontroller.web.client;

import com.google.gwt.user.client.Timer;
import com.mohaine.brewcontroller.client.display.Scheduler;

public class SchedulerGwt implements Scheduler {

	@Override
	public Cancelable scheduleReapeating(final RunRepeat run, final long delayMs) {

		final Timer t = new Timer() {
			@Override
			public void run() {
				long nextDelay = run.run();
				if (nextDelay > -1) {
					this.schedule((int) nextDelay);
				}
			}
		};

		t.schedule((int) delayMs);

		return new Cancelable() {

			@Override
			public void cancel() {
				t.cancel();
			}
		};
	}

}
