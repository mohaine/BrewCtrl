package com.mohaine.brewcontroller.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.net.BrewJsonConverter;
import com.mohaine.brewcontroller.client.net.Callback;
import com.mohaine.brewcontroller.client.net.ControllerHardwareJson;

public class ControllerHardwareJsonUrlRequest extends ControllerHardwareJson {

	private ConfigurationLoader configurationLoader;
	private Thread statusThread;

	@Inject
	public ControllerHardwareJsonUrlRequest(EventBus eventBusp, BrewJsonConverter converter, ConfigurationLoader configurationLoader) throws Exception {
		super(eventBusp, converter);

		this.configurationLoader = configurationLoader;

		statusThread = new Thread(new CommThread());
		statusThread.start();

	}

	private final class CommThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					if (!connected) {
						connect();
					}
					updateStatus();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignore
					}
				} catch (IOException e) {
					disconnect();
					setStatus(e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					setStatus(e.getMessage());
				}

			}
		}
	}

	protected void updateStatusAsap() {
		super.updateStatusAsap();
		statusThread.interrupt();
	}

	@Override
	protected Configuration loadDefaultConfiguration() {
		return configurationLoader.getConfiguration();
	}

	protected CommandRequest getCommandRequest(String cmd) {
		String baseUrl = getBaseCmdUrl();
		try {
			URL url = new URL(baseUrl + cmd);
			URLRequest r = new URLRequest(url);
			return new CommandRequestURLRequest(r);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}

	private static class CommandRequestURLRequest implements CommandRequest {
		private URLRequest r;

		public CommandRequestURLRequest(URLRequest r) {
			this.r = r;
		}

		@Override
		public void addParameter(String name, String value) {
			r.addParameter(name, value);
		}

		@Override
		public void runRequest(Callback<String> callback) {
			try {
				String result = r.getString();
				callback.onSuccess(result);
			} catch (IOException e) {
				callback.onNotSuccess(e);
			}
		}
	}

	private String getBaseCmdUrl() {
		String baseUrl = "http://localhost:" + DEFAULT_PORT + "/cmd/";
		return baseUrl;
	}
}
