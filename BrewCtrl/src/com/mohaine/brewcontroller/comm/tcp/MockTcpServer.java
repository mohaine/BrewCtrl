package com.mohaine.brewcontroller.comm.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.comm.ControlPointReaderListUpdater;
import com.mohaine.brewcontroller.comm.MessageEnvelope;
import com.mohaine.brewcontroller.comm.MessageProcessor;
import com.mohaine.brewcontroller.comm.MessageReader;
import com.mohaine.brewcontroller.comm.msg.ControlMessageReaderWriter;
import com.mohaine.brewcontroller.comm.msg.ControlPointReaderWriter;
import com.mohaine.brewcontroller.comm.msg.SensorMessageReaderWriter;

public class MockTcpServer {

	private final HardwareControl control = new HardwareControl();

	private MessageProcessor processor;
	private ControlMessageReaderWriter controlMsgWriter = new ControlMessageReaderWriter();
	private SensorMessageReaderWriter sensorMessageWriter = new SensorMessageReaderWriter();
	private List<HardwareSensor> sensors = new ArrayList<HardwareSensor>();
	ControlPointReaderWriter controlPointWriter = new ControlPointReaderWriter();

	private boolean run ;

	public static void main(String[] args) throws Exception {
		new MockTcpServer().startListening();
	}

	private void startListening() throws UnknownHostException, IOException {
		run = true;
		ServerSocket ss = new ServerSocket(TcpComm.DEFAULT_PORT);
		try {
			while (run) {
				Socket accept = ss.accept();
				Thread thread = new Thread(new ConnectionThread(accept));
				thread.setDaemon(true);
				thread.start();
			}
		} finally {
			ss.close();
		}
	}

	public MockTcpServer() {
		control.setControlPoints(new ArrayList<ControlPoint>());

		HardwareSensor sensor1 = new HardwareSensor();
		sensor1.setAddress("0000000000000001");
		sensor1.setReading(true);
		sensor1.setTempatureC(25);
		sensors.add(sensor1);
		HardwareSensor sensor2 = new HardwareSensor();
		sensor2.setAddress("0000000000000002");
		sensor2.setReading(true);
		sensor2.setTempatureC(26);
		sensors.add(sensor2);

		ArrayList<MessageReader> readers = new ArrayList<MessageReader>();
		readers.add(controlMsgWriter);
		readers.add(createCpReader());
		controlMsgWriter.setControl(control);

		processor = new MessageProcessor(false, readers);

	}

	private ControlPointReaderWriter createCpReader() {
		ControlPointReaderWriter controlPointReader = new ControlPointReaderWriter();
		controlPointReader.setControlPoint(new ControlPoint());
		controlPointReader.setListener(new ControlPointReaderListUpdater(control.getControlPoints()));
		return controlPointReader;
	}

	private final class ConnectionThread implements Runnable {
		private Socket socket;

		public ConnectionThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			InputStream inputStream = null;
			OutputStream outputStream = null;

			try {
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				while (run) {
					while (inputStream.available() > 0) {

						boolean changes = processor.readStream(inputStream);
						if (changes) {
							byte[] buffer = new byte[1260];
							int offset = MessageEnvelope.writeMessage(buffer, 0, controlMsgWriter);

							for (HardwareSensor sensor : sensors) {
								sensorMessageWriter.setSensor(sensor);
								offset = MessageEnvelope.writeMessage(buffer, offset, sensorMessageWriter);
							}

							List<ControlPoint> controlPoints = control.getControlPoints();
							synchronized (controlPoints) {
								for (ControlPoint controlPoint : controlPoints) {
									synchronized (controlPoint) {
										controlPointWriter.setControlPoint(controlPoint);
										offset = MessageEnvelope.writeMessage(buffer, offset, controlPointWriter);
									}
								}
							}
							outputStream.write(buffer, 0, offset);
							outputStream.flush();

						}

						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// ignore
					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					inputStream.close();
				} catch (Exception e) {
					// Ignore
				}
				try {
					outputStream.close();
				} catch (Exception e) {
					// Ignore
				}
				try {
					socket.close();
				} catch (Exception e) {
					// Ignore
				}
			}
		}
	}
}
