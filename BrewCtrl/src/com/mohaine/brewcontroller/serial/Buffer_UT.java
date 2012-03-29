package com.mohaine.brewcontroller.serial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

public class Buffer_UT {

	@Test
	public void testBuffer() throws Exception {

		Buffer buffer = new Buffer(10);

		assertEquals(0, buffer.available());

		for (int i = 0; i < 10; i++) {
			buffer.write(i);
			assertEquals(i + 1, buffer.available());
		}

		try {
			buffer.write(100);
			fail("Should have Overflow");
		} catch (Exception e) {
			// ignore
		}

		for (int i = 0; i < 10; i++) {
			assertEquals(i, buffer.read());
		}

		try {
			buffer.read();
			fail("Should have read past data");
		} catch (Exception e) {
			// ignore
		}
	}

	@Test
	public void testBuffer_Offset() throws Exception {

		Buffer buffer = new Buffer(10);

		assertEquals(0, buffer.available());

		Random r = new Random();

		for (int outer = 0; outer < 10; outer++) {

			buffer.write(123);
			assertEquals(123, buffer.read());
			assertEquals(0, buffer.available());

			int count = r.nextInt(10);

			for (int i = 0; i < count; i++) {
				buffer.write(i);
			}
			for (int i = 0; i < count; i++) {
				assertEquals(i, buffer.read());
			}
			assertEquals(0, buffer.available());

		}
	}

	@Test
	public void testBuffer_ReadWrite() throws Exception {

		Buffer buffer = new Buffer(10);
		assertEquals(0, buffer.available());

		for (int outer = 0; outer < 10; outer++) {

			buffer.write(123);
			assertEquals(123, buffer.read());
			assertEquals(0, buffer.available());
		}
	}
}
