package com.mohaine.brewcontroller.comm.msg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.test.TestUtils;

public class ControlPointReaderWriter_UT {

	@Test
	public void testSize() throws Exception {
		ControlPointReaderWriter w = new ControlPointReaderWriter();
		w.setControlPoint(new ControlPoint());

		try {
			w.writeTo(new byte[w.getLength() - 1], 0);
			fail("Didn't fill buffer");
		} catch (Exception e) {
			// Exp
		}

		w.writeTo(new byte[w.getLength()], 0);
	}

	@Test
	public void testData() throws Exception {
		ControlPointReaderWriter w = new ControlPointReaderWriter();
		ControlPointReaderWriter r = new ControlPointReaderWriter();

		w.setControlPoint(new ControlPoint());
		r.setControlPoint(new ControlPoint());

		byte[] buffer = new byte[200];
		byte[] adddress = new byte[8];

		Random random = new Random();
		for (int i = 0; i < 10000; i++) {
			int offset = random.nextInt(20);

			ControlPoint controlPoint = w.getControlPoint();
			controlPoint.setAutomaticControl(random.nextBoolean());
			controlPoint.setControlPin(getRandomByte(random));
			controlPoint.setDuty(getRandomByte(random));
			controlPoint.setHasDuty(random.nextBoolean());
			controlPoint.setTargetTemp(random.nextFloat());
			controlPoint.setFullOnAmps(random.nextInt(200));

			random.nextBytes(adddress);
			controlPoint.setTempSensorAddress(SensorMessageReaderWriter.readAddress(adddress, 0));

			w.writeTo(buffer, offset);
			r.readFrom(buffer, offset);

			assertEquals(TestUtils.displayFields(w.getControlPoint()), TestUtils.displayFields(r.getControlPoint()));
		}

	}

	private byte getRandomByte(Random random) {
		byte[] rB = new byte[1];
		random.nextBytes(rB);
		byte controlPin = rB[0];
		return controlPin;
	}
}
