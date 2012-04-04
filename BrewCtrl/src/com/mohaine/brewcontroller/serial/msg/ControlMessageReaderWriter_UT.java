package com.mohaine.brewcontroller.serial.msg;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HeaterMode;
import com.mohaine.brewcontroller.test.TestUtils;

public class ControlMessageReaderWriter_UT {

	@Test
	public void testSize() throws Exception {
		ControlMessageReaderWriter w = new ControlMessageReaderWriter();
		w.setControl(new HardwareControl());

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
		ControlMessageReaderWriter w = new ControlMessageReaderWriter();
		ControlMessageReaderWriter r = new ControlMessageReaderWriter();

		w.setControl(new HardwareControl());
		r.setControl(new HardwareControl());

		byte[] buffer = new byte[200];

		Random random = new Random();
		for (int i = 0; i < 10000; i++) {
			int offset = random.nextInt(20);

			HardwareControl controlPoint = w.getControl();
			controlPoint.setControlId(getRandomByte(random));
			controlPoint.setMode(random.nextBoolean() ? HeaterMode.ON : HeaterMode.OFF);
			controlPoint.setMaxAmps(random.nextInt(200));

			w.writeTo(buffer, offset);
			r.readFrom(buffer, offset);

			assertEquals(TestUtils.displayFields(w.getControl()), TestUtils.displayFields(r.getControl()));
		}

	}

	private byte getRandomByte(Random random) {
		byte[] rB = new byte[1];
		random.nextBytes(rB);
		byte controlPin = rB[0];
		return controlPin;
	}
}
