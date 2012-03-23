package com.mohaine.brewcontroller.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public class RxTxComm {

	private final String[] COMM_PORTS = { "/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3", "/dev/ttyACM0" };

	private InputStream inputStream;
	private OutputStream outputStream;

	private SerialPort serialPort;

	/**
	 * 
	 * @return Error message - null is success
	 */
	public String reconnectIfNeeded() {
		if (inputStream == null) {
			try {

				if (System.getProperty("gnu.io.rxtx.SerialPorts") == null) {

					StringBuffer sb = new StringBuffer();
					for (String string : COMM_PORTS) {
						if (sb.length() > 0) {
							sb.append(File.pathSeparator);
						}
						sb.append(string);
					}
					System.setProperty("gnu.io.rxtx.SerialPorts", sb.toString());
				}
				Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
				while (portList.hasMoreElements()) {
					CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
					if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
						SerialPort serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);

						inputStream = serialPort.getInputStream();
						outputStream = serialPort.getOutputStream();
						this.serialPort = serialPort;
						serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
						return SerialHardwareComm.STATUS_NO_COMM_READ;
					}
				}

				if (inputStream == null) {
					return SerialHardwareComm.STATUS_CONNECT_NO_PORT;
				}

			} catch (Exception e) {
				return SerialHardwareComm.STATUS_CONNECT_ERROR;
			}
		}

		return null;
	}

	public void disconnect() {

		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}
			inputStream = null;
		}
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				// ignore
			}
			outputStream = null;
		}
		if (serialPort != null) {
			serialPort.close();
		}
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
}
