package com.mohaine.brewcontroller.comm.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.comm.SerialConnection;

public class TcpComm implements SerialConnection {

	public static int DEFAULT_PORT = 2739;
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;

	@Inject
	public TcpComm(ConfigurationLoader configurationLoader) {

	}

	@Override
	public String reconnectIfNeeded() {
		if (socket == null) {
			try {
				// socket = new Socket(InetAddress.getLocalHost(),
				// DEFAULT_PORT);
				socket = new Socket("localhost", DEFAULT_PORT);
				// socket = new Socket("smaug", DEFAULT_PORT);

				outputStream = socket.getOutputStream();
				inputStream = socket.getInputStream();

			} catch (Exception e) {
				return e.getMessage();
			}
		}
		return null;
	}

	@Override
	public void disconnect() {
		if (socket != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				// ignore
			}
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}
			try {
				socket.close();
			} catch (IOException e) {
				// ignore
			}
			outputStream = null;
			inputStream = null;
			socket = null;
		}

	}

	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public int getMaxWriteSize() {
		return 1024;
	}

}
