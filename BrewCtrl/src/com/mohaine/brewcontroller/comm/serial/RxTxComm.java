package com.mohaine.brewcontroller.comm.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.comm.SerialConnection;
import com.mohaine.brewcontroller.comm.SerialHardwareComm;

public class RxTxComm implements SerialConnection {

	private final String COMM_PORTS = "/dev/ttyUSB0, /dev/ttyUSB1, /dev/ttyUSB2, /dev/ttyUSB3, /dev/ttyACM0";

	private InputStream inputStream;
	private OutputStream outputStream;

	private SerialPort serialPort;

	private String cfgCommPorts;

	@Inject
	public RxTxComm(ConfigurationLoader configurationLoader) {
		cfgCommPorts = configurationLoader.getConfiguration().getCommPorts();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mohaine.brewcontroller.serial.SerialConnection#reconnectIfNeeded()
	 */
	@Override
	public String reconnectIfNeeded() {
		if (inputStream == null) {
			try {

				if (System.getProperty("gnu.io.rxtx.SerialPorts") == null) {
					StringBuffer sb = new StringBuffer();
					addComValues(sb, COMM_PORTS.split(","));

					if (cfgCommPorts != null) {
						addComValues(sb, cfgCommPorts.split(","));
					}

					if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
						for (int i = 3; i < 10; i++) {
							addComValues(sb, "COM" + i);
						}
					}

					System.setProperty("gnu.io.rxtx.SerialPorts", sb.toString());
				}
				Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
				while (portList.hasMoreElements()) {
					CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
					if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
						System.out.println("Connected to com port " + portId.getName());
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

	private void addComValues(StringBuffer sb, String... strings) {
		for (String string : strings) {
			string = string.trim();
			if (string.length() > 0) {
				if (sb.length() > 0) {
					sb.append(File.pathSeparator);
				}
				sb.append(string);
				System.out.println(" Allow Com Port: " + string);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mohaine.brewcontroller.serial.SerialConnection#disconnect()
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mohaine.brewcontroller.serial.SerialConnection#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mohaine.brewcontroller.serial.SerialConnection#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public int getMaxWriteSize() {
		return 126;
	}
}
