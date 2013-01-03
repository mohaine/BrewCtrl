package com.mohaine.brewcontroller;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mohaine.brewcontroller.client.TimeParser;

public class TimeParser_UT {

	@Test
	public void testFormat() {

		TimeParser tp = new TimeParser();

		assertEquals("0:00", tp.format(-100));
		assertEquals("0:00", tp.format(100));
		assertEquals("0:00", tp.format(999));

		tp.setZeroDescription("ABC");
		assertEquals("ABC", tp.format(-100));
		assertEquals("ABC", tp.format(0));
		assertEquals("0:00", tp.format(100));
		assertEquals("0:00", tp.format(999));

		assertEquals("0:01", tp.format(1000));

		assertEquals("0:01", tp.format(1100));
		assertEquals("0:59", tp.format(1000 * 59));
		assertEquals("1:00", tp.format(1000 * 60));
		assertEquals("1:01", tp.format(1000 * 61));
		assertEquals("60:01", tp.format(1000 * (60 * 60 + 1)));
	}

	@Test
	public void testParse() {

		TimeParser tp = new TimeParser();

		tp.setZeroDescription("ABC");
		assertEquals("ABC", tp.format(-100));

		assertEquals(0, tp.parse("ABC"));
		assertEquals(0, tp.parse("0:00"));
		assertEquals(1000, tp.parse("0:01"));
		assertEquals(1000, tp.parse("1"));

		assertEquals(1000, tp.parse("0:01"));
		assertEquals(1000 * 59, tp.parse("0:59"));
		assertEquals(1000 * 60, tp.parse("1:00"));
		assertEquals(1000 * 61, tp.parse("1:01"));
		assertEquals(1000 * (60 * 60 + 1), tp.parse("60:01"));

		assertEquals(1000 * (60 * 60 + 1), tp.parse("1:00:01"));

	}
}
