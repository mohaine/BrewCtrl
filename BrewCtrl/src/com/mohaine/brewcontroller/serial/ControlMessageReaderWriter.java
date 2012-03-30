package com.mohaine.brewcontroller.serial;

import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HeaterMode;

public class ControlMessageReaderWriter extends BinaryMessage implements MessageWriter, MessageReader {
	private HardwareControl control;
	private ByteUtils byteUtils = new ByteUtils();

	public ControlMessageReaderWriter() {
		super(SerialConstants.HARDWARE_CONTROL, 3);
	}

	@Override
	public void readFrom(byte[] buffer, int offset) {
		control.setControlId(byteUtils.getShort(buffer, offset));
		offset += 2;
		control.setMode(buffer[offset++] == 1 ? HeaterMode.ON : HeaterMode.OFF);

		//		System.out.println("Read : " + control.getControlId());

	}

	@Override
	public void writeTo(byte[] buffer, int offset) {
//		System.out.println("Write: " + control.getControlId());
		
		byteUtils.putShort(buffer, offset, (short) control.getControlId());
		offset += 2;

		buffer[offset++] = (byte) (control.getMode() == HeaterMode.ON ? 1 : 0);

		// TODO NEW
		// List<ControlPoint> controlPoints = control.getControlPoints();
		// for (ControlPoint controlPoint : controlPoints) {
		//
		//
		// controlPoint.get
		// boolean automaticControl = controlPoint.isAutomaticControl();
		//
		//
		// }
		//
		// boolean mashOn = control.isMashOn();
		// byte boilDuty = control.getBoilDuty();
		// String hltSensor = control.getHltSensor();
		// String tunSensor = control.getTunSensor();
		// float hltTargetTemp = (float) control.getHltTargetTemp();
		// float tunTargetTemp = (float) control.getTunTargetTemp();
		//
		//
		//
		//
		// buffer[offset++] = (byte) boilDuty;
		// buffer[offset++] = (byte) (mashOn ? 1 : 0);
		//
		// writeAddress(buffer, offset, hltSensor);
		// offset += 8;
		// writeAddress(buffer, offset, tunSensor);
		// offset += 8;
		//
		// byteUtils.putFloat(buffer, offset, hltTargetTemp);
		// offset += 4;
		// byteUtils.putFloat(buffer, offset, tunTargetTemp);
		// offset += 4;

		// if (offset != getLength()) {
		// throw new RuntimeException("Invalid Length");
		// }

	}

	public HardwareControl getControl() {
		return control;
	}

	public void setControl(HardwareControl control) {
		this.control = control;
	}

}