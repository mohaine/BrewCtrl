package com.mohaine.brewcontroller.serial.msg;

import static com.mohaine.brewcontroller.serial.msg.SensorMessageReaderWriter.readAddress;
import static com.mohaine.brewcontroller.serial.msg.SensorMessageReaderWriter.writeAddress;

import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.serial.BinaryMessage;
import com.mohaine.brewcontroller.serial.ByteUtils;
import com.mohaine.brewcontroller.serial.MessageReader;
import com.mohaine.brewcontroller.serial.MessageWriter;
import com.mohaine.brewcontroller.serial.ReadListener;
import com.mohaine.brewcontroller.serial.SerialConstants;

public class ControlPointReaderWriter extends BinaryMessage implements MessageReader, MessageWriter {
	private static final int HAS_DUTY_MASK = 0x02;
	private static final int AUTO_MASK = 0x01;
	private ControlPoint controlPoint;
	private ByteUtils byteUtils = new ByteUtils();
	private ReadListener<ControlPointReaderWriter> listener;

	public ControlPointReaderWriter() {
		super(SerialConstants.CONTROL_POINT_MSG, 15);
	}

	@Override
	public void readFrom(byte[] buffer, int offset) {
		controlPoint.setControlPin(buffer[offset++]);
		controlPoint.setDuty(buffer[offset++]);

		int booleanValues = buffer[offset++];
		controlPoint.setAutomaticControl((booleanValues & AUTO_MASK) != 0);
		controlPoint.setHasDuty((booleanValues & HAS_DUTY_MASK) != 0);

		controlPoint.setTargetTemp(byteUtils.getFloat(buffer, offset));
		offset += 4;

		controlPoint.setTempSensorAddress(readAddress(buffer, offset));
		offset += 8;

		if (listener != null) {
			listener.onRead(this);
		}
		
//		System.out.println(controlPoint.getControlPin() + " Duty: " + controlPoint.getDuty());
	}

	@Override
	public void writeTo(byte[] buffer, int offset) {
		buffer[offset++] = controlPoint.getControlPin();
		buffer[offset++] = (byte) controlPoint.getDuty();

		int booleanValues = 0x00;

		if (controlPoint.isAutomaticControl()) {
			booleanValues = booleanValues | AUTO_MASK;
		}
		if (controlPoint.isHasDuty()) {
			booleanValues = booleanValues | HAS_DUTY_MASK;
		}

		buffer[offset++] = (byte) booleanValues;

		byteUtils.putFloat(buffer, offset, (float) controlPoint.getTargetTemp());
		offset += 4;
		writeAddress(buffer, offset, controlPoint.getTempSensorAddress());
		offset += 8;

	}

	public void setListener(ReadListener<ControlPointReaderWriter> listener) {
		this.listener = listener;
	}

	public ControlPoint getControlPoint() {
		return controlPoint;
	}

	public void setControlPoint(ControlPoint controlPoint) {
		this.controlPoint = controlPoint;
	}

}