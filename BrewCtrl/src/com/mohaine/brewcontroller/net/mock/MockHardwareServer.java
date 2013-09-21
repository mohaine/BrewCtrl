/*
 Copyright 2009-2013 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package com.mohaine.brewcontroller.net.mock;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.ControllerUrlLoader;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.bean.ControllerStatus;
import com.mohaine.brewcontroller.client.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.client.bean.TempSensor;
import com.mohaine.brewcontroller.swing.FileConfigurationLoader;

public class MockHardwareServer {
	private boolean run = true;

	private List<HtmlService> services = new ArrayList<HtmlService>();

	public interface HtmlService {

		public String getPath();

		public void process(HTTPRequest request, HTTPResponse response) throws Exception;

	}

	public static void main(String[] args) {

		MockHardware mock = new MockHardware();
		ControllerStatus status = new ControllerStatus();
		status.setSteps(new ArrayList<ControlStep>());

		List<TempSensor> sensors = new ArrayList<TempSensor>();

		TempSensor sensor1 = new TempSensor();
		sensor1.setAddress("0000000000000001");
		sensor1.setReading(true);
		sensor1.setTemperatureC(25);
		sensors.add(sensor1);
		TempSensor sensor2 = new TempSensor();
		sensor2.setAddress("0000000000000002");
		sensor2.setReading(true);
		sensor2.setTemperatureC(26);
		sensors.add(sensor2);

		status.setSensors(sensors);

		status.setMode(Mode.OFF);
		mock.setStatus(status);

		MockHardwareServer server = new MockHardwareServer();
		server.addHtmlService(new VersionService());

		File configFile = new File("BrewControllerConfig.json");
		server.addHtmlService(new ConfigurationService(mock, new FileConfigurationLoader(configFile)));
		server.addHtmlService(new StatusService(mock));

		server.listen(ControllerUrlLoader.DEFAULT_PORT);

	}

	public MockHardwareServer() {
	}

	public boolean listen(int port) {
		System.out.println("Mock listen on port " + port);
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			try {

				while (run) {
					try {

						Socket socket = serverSocket.accept();
						try {
							processConnect(socket);
						} finally {
							socket.close();
						}
					} catch (SocketException se) {
						// Ignore
					} catch (Exception e) {
						e.printStackTrace();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
						}
					}
				}
			} finally {
				serverSocket.close();
			}

		} catch (Throwable t) {
			return false;
		}

		return true;
	}

	private void processConnect(Socket socket) throws Exception {

		HTTPRequest request = new HTTPRequest(socket);
		HTTPResponse response = new HTTPResponse(socket);
		try {
			request.readHeaders();
			String requestPath = request.getPath();

			response.setContentType("text/html");

			boolean found = false;

			if (requestPath != null) {
				for (HtmlService service : services) {
					if (requestPath.startsWith(service.getPath())) {
						found = true;
						service.process(request, response);
						break;
					}
				}
			}
			if (!found) {
				response.setStatusCode(HttpCodes.NOT_FOUND);
				response.setStatus("Not Found");
				response.sendContent("404 Not Found\n");

			}

		} finally {
			request.close();
			response.close();
		}
	}

	public void addHtmlService(HtmlService processor) {
		services.add(processor);
	}

}