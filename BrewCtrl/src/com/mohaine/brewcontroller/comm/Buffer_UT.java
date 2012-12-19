package com.mohaine.brewcontroller.comm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.junit.Test;

public class Buffer_UT {

	@Test
	public void testBuffer() throws Exception {

		Buffer buffer = new Buffer(10);

		assertEquals(0, buffer.available());

		for (int i = 0; i < 10; i++) {
			buffer.write((byte) i);
			assertEquals(i + 1, buffer.available());
		}

		try {
			buffer.write((byte) 100);
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

			buffer.write((byte) 123);
			assertEquals(123, buffer.read());
			assertEquals(0, buffer.available());

			int count = r.nextInt(10);

			for (int i = 0; i < count; i++) {
				buffer.write((byte) i);
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

			buffer.write((byte) 123);
			assertEquals(123, buffer.read());
			assertEquals(0, buffer.available());
		}
	}

	@Test
	public void testBuffer_Streams() throws Exception {
		Random r = new Random();

		Buffer buffer = new Buffer(10);
		assertEquals(0, buffer.available());

		InputStream inputStream = buffer.getInputStream();
		OutputStream ouputStream = buffer.getOuputStream();

		for (int outer = 0; outer < 100000; outer++) {

			final int testDataLength = r.nextInt(10) + 1;

			byte[] expect = new byte[testDataLength];

			r.nextBytes(expect);
			ouputStream.write(expect, 0, testDataLength);

			assertEquals(testDataLength, buffer.available());
			assertEquals(testDataLength, inputStream.available());

			byte[] result = new byte[testDataLength];
			int read = inputStream.read(result);

			assertEquals(testDataLength, read);
			for (int i = 0; i < testDataLength; i++) {
				assertEquals(expect[i], result[i]);
			}

		}

	}

}
