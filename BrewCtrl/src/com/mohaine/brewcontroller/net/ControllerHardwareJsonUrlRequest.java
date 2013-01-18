package com.mohaine.brewcontroller.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.ControllerUrlLoader;
import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.net.BrewJsonConverter;
import com.mohaine.brewcontroller.client.net.Callback;
import com.mohaine.brewcontroller.client.net.ControllerHardwareJson;

public class ControllerHardwareJsonUrlRequest extends ControllerHardwareJson {

	private ConfigurationLoader configurationLoader;
	private Thread statusThread;
	private ControllerUrlLoader controllerUrlLoader;

	@Inject
	public ControllerHardwareJsonUrlRequest(EventBus eventBusp, BrewJsonConverter converter, ConfigurationLoader configurationLoader, ControllerUrlLoader controllerUrlLoader) throws Exception {
		super(eventBusp, converter);
		this.controllerUrlLoader = controllerUrlLoader;
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
					if (connected) {
						updateStatus();
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// ignore
					}
				} catch (IOException e) {
					setStatus(e.getMessage());
					try {
						disconnect();
					} catch (Exception e1) {
						// Ignore
					}

				} catch (Exception e) {
					e.printStackTrace();
					setStatus(e.getMessage());

					try {
						disconnect();
					} catch (Exception e1) {
						// Ignore
					}
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
		Configuration configuration = configurationLoader.getConfiguration();
		return configuration;
	}

	@Override
	protected void saveDefaultConfiguration() throws Exception {
		configurationLoader.saveConfiguration(getConfiguration());

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
			} finally {
				r.close();
			}
		}
	}

	private String getBaseCmdUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append(controllerUrlLoader.getUrl());
		if (sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}
		sb.append("cmd/");
		return sb.toString();
	}
}
