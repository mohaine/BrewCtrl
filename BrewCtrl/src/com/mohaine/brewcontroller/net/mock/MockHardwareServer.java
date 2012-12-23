/*
 Copyright 2009-2012 Michael Graessle

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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.bean.ControllerStatus;
import com.mohaine.brewcontroller.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.net.ControllerHardwareJson;

public class MockHardwareServer {

	private List<HtmlService> services = new ArrayList<HtmlService>();
	private SocketListener socketListener;

	public interface HtmlService {

		public String getPath();

		public void process(HTTPRequest request, HTTPResponse response) throws Exception;

	}

	public static void main(String[] args) {

		MockHardware mock = new MockHardware();
		ControllerStatus status = new ControllerStatus();
		status.setSteps(new ArrayList<HeaterStep>());

		status.setMode(Mode.OFF);
		mock.setStatus(status);

		MockHardwareServer server = new MockHardwareServer();
		server.addHtmlService(new VersionService());
		server.addHtmlService(new LayoutService(mock));
		server.addHtmlService(new StatusService(mock));

		server.listen(ControllerHardwareJson.DEFAULT_PORT);
		while (true) {
			try {
				Thread.sleep(2000000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public MockHardwareServer() {
	}

	public boolean listen(int port) {
		try {
			ServerSocket socket = new ServerSocket(port);
			socketListener = new SocketListener(socket);
			Thread thread = new Thread(socketListener);
			thread.setDaemon(true);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		} catch (Throwable t) {
			return false;
		}

		return true;
	}

	private class SocketListener implements Runnable {
		ServerSocket serverSocket = null;
		boolean run = true;

		public SocketListener(ServerSocket socket) {
			this.serverSocket = socket;
		}

		public void run() {

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
	}

	public void addHtmlService(HtmlService processor) {
		services.add(processor);
	}

}