package com.mohaine.brewcontroller.comm.msg;

import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.comm.BinaryMessage;
import com.mohaine.brewcontroller.comm.ByteUtils;
import com.mohaine.brewcontroller.comm.MessageReader;
import com.mohaine.brewcontroller.comm.MessageWriter;
import com.mohaine.brewcontroller.comm.ReadListener;
import com.mohaine.brewcontroller.comm.CommConstants;

public class SensorMessageReaderWriter extends BinaryMessage implements MessageReader, MessageWriter {
	private ByteUtils byteUtils = new ByteUtils();
	private HardwareSensor sensor;
	private ReadListener<SensorMessageReaderWriter> listener;

	public SensorMessageReaderWriter() {
		super(CommConstants.SENSOR_CONTROL, 13);
	}

	@Override
	public void readFrom(byte[] buffer, int offset) {
		sensor.setAddress(readAddress(buffer, offset));
		offset += 8;

		sensor.setReading(buffer[offset++] == CommConstants.TRUE);
		sensor.setTempatureC(byteUtils.getFloat(buffer, offset));
		offset += 4;

		if (listener != null) {
			listener.onRead(this);
		}
	}

	@Override
	public void writeTo(byte[] buffer, int offset) {
		writeAddress(buffer, offset, sensor.getAddress());
		offset += 8;
		buffer[offset++] = sensor.isReading() ? CommConstants.TRUE : CommConstants.FALSE;
		byteUtils.putFloat(buffer, offset, (float) sensor.getTempatureC());
		offset += 4;

	}

	public void setListener(ReadListener<SensorMessageReaderWriter> listener) {
		this.listener = listener;
	}

	private static final String HEXES = "0123456789abcdef";

	public static String readAddress(byte[] buffer, int offset) {
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < 8; j++) {

			byte b = buffer[offset++];
			sb.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));

		}
		String string = sb.toString();
		return string;
	}

	public static void writeAddress(byte[] buffer, int offset, String value) {
		for (int j = 0; j < 8; j++) {
			int parseInt;
			if (value != null && value.length() > j + 1) {
				int strOffset = j * 2;
				String substring = value.substring(strOffset, strOffset + 2);
				parseInt = Integer.parseInt(substring, 16);
			} else {
				parseInt = 0;
			}
			buffer[offset + j] = (byte) parseInt;
		}
	}

	public HardwareSensor getSensor() {
		return sensor;
	}

	public void setSensor(HardwareSensor sensor) {
		this.sensor = sensor;
	}

}