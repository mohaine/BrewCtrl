package com.mohaine.brewcontroller.web.client.net;

import java.io.IOException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.client.net.BrewJsonConverter;
import com.mohaine.brewcontroller.client.net.Callback;
import com.mohaine.brewcontroller.client.net.ControllerHardwareJson;

public class ControllerHardwareJsonGwt extends ControllerHardwareJson {

	private Timer statusTimer = new Timer() {
		@Override
		public void run() {
			try {
				if (!connected) {
					connect();
				}
				updateStatus();
			} catch (IOException e) {
				e.printStackTrace();
				disconnect();
				setStatus(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(e.getMessage());
			}
			statusTimer.schedule(500);
		}

	};

	protected void updateStatusAsap() {
		super.updateStatusAsap();
		statusTimer.cancel();
		statusTimer.schedule(10);
	}

	@Inject
	public ControllerHardwareJsonGwt(EventBus eventBusp,
			BrewJsonConverter converter) throws Exception {
		super(eventBusp, converter);

		statusTimer.schedule(500);

	}

	@Override
	protected BreweryLayout loadDefaultLayout() {
		return null;
	}

	@Override
	protected CommandRequest getCommandRequest(String cmd) {

		System.out.println("ControllerHardwareJsonGwt.getCommandRequest(): "
				+ cmd);
		return new CommandRequest() {

			@Override
			public void addParameter(String name, String value) {
				System.out.println("Parameter: " + name + " = " + value);
			}

			@Override
			public void runRequest(Callback<String> callback) {
				// TODO Auto-generated method stub

			}
		};
	}

	private void runReq(String url, String requestData) throws Exception {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
		builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
		builder.sendRequest(requestData, new RequestCallback() {
			public void onError(Request request, Throwable exception) {

			}

			public void onResponseReceived(Request request, Response response) {
				if (200 == response.getStatusCode()) {

				} else {

				}
			}
		});

	}

}
