package com.mohaine.brewcontroller.web.client.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
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
			statusTimer.schedule(2000);
		}

	};

	protected void updateStatusAsap() {
		super.updateStatusAsap();
		statusTimer.cancel();
		statusTimer.schedule(10);
	}

	@Inject
	public ControllerHardwareJsonGwt(EventBus eventBusp, BrewJsonConverter converter) throws Exception {
		super(eventBusp, converter);

		statusTimer.schedule(500);

	}

	@Override
	protected void saveDefaultConfiguration() throws Exception {

	}

	@Override
	protected Configuration loadDefaultConfiguration() {
		return null;
	}

	@Override
	protected CommandRequest getCommandRequest(String cmd) {

		// System.out.println("ControllerHardwareJsonGwt.getCommandRequest(): "
		// + cmd);
		return new CommandRunner(cmd);
	}

	private final class CommandRunner implements CommandRequest {
		private String url;
		private Map<String, String> params;

		public CommandRunner(String cmd) {
			this.url = "/cmd/" + cmd;
		}

		@Override
		public void addParameter(String name, String value) {
			if (params == null) {
				params = new HashMap<String, String>();
			}
			params.put(name, value);
		}

		@Override
		public void runRequest(final Callback<String> callback) {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
			builder.setHeader("Content-Type", "application/x-www-form-urlencoded");

			String requestData = "";
			if (params != null) {
				StringBuilder sb = new StringBuilder();
				Set<Entry<String, String>> entrySet = params.entrySet();
				for (Entry<String, String> entry : entrySet) {
					sb.append(URL.encode(entry.getKey()));
					sb.append("=");
					sb.append(URL.encode(entry.getValue()));
					sb.append("\r\n");
				}
				requestData = sb.toString();
			}

			try {
				builder.sendRequest(requestData, new RequestCallback() {
					public void onError(Request request, Throwable exception) {
						callback.onNotSuccess(exception);
					}

					public void onResponseReceived(Request request, Response response) {

						if (200 == response.getStatusCode()) {
							callback.onSuccess(response.getText());
						} else {
							callback.onNotSuccess(new Exception("Error Code: " + response.getStatusCode()));
						}
					}
				});
			} catch (RequestException e) {
				callback.onNotSuccess(e);
			}
		}
	}

}
