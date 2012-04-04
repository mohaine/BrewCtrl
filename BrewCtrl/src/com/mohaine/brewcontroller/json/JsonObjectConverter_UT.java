package com.mohaine.brewcontroller.json;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mohaine.brewcontroller.test.TestUtils;

public class JsonObjectConverter_UT {

	@Before
	public void setUp() throws Exception {
		TestObject.ID = 0;
		TestObjectParent.ID = 0;
	}

	@Test
	public void testSimple() throws Exception {
		String objString = "{\"name\":\"value\"}";
		JsonObjectConverter jc = new JsonObjectConverter();
		JsonUnknownObject jo = (JsonUnknownObject) jc.decode(objString);
		assertNotNull(jo);
		assertEquals("value", jo.getProperty("name"));
	}

	@Test
	public void testTypedEncodeReflection() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter();
		TestObject to = new TestObject();
		assertEquals("", jc.encode(to));

		jc.addHandler(ReflectionJsonHandler.build(TestObject.class));
		String js = jc.encode(to);
		assertEquals("{\"__type__\":\"TestObject\",\"id\":0,\"enumValue\":\"ONE\"}", js);

		TestObject reconstituedTo = (TestObject) jc.decode(js);
		assertEquals(to, reconstituedTo);

		for (int i = 0; i < 10; i++) {
			to = new TestObject();
			js = jc.encode(to);
			reconstituedTo = (TestObject) jc.decode(js);
			assertEquals(to, reconstituedTo);
		}
	}

	@Test
	public void testTypedEncodeReflectionNoType() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter(false);
		TestObject to = new TestObject();
		assertEquals("", jc.encode(to));

		jc.addHandler(ReflectionJsonHandler.build(TestObject.class));
		String js = jc.encode(to);
		assertEquals("{\"id\":0,\"enumValue\":\"ONE\"}", js);

		TestObject reconstituedTo = jc.decode(js, TestObject.class);
		assertEquals(to, reconstituedTo);

		for (int i = 0; i < 10; i++) {
			to = new TestObject();
			js = jc.encode(to);
			reconstituedTo = jc.decode(js, TestObject.class);
			assertEquals(to, reconstituedTo);
		}
	}

	@Test
	public void testTypedEncodeReflectionNoTypeMultipleLevel() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter(false);
		TestObject to = new TestObject();
		TestObjectParent top = new TestObjectParent();
		top.to = to;

		jc.addHandler(ReflectionJsonHandler.build(TestObject.class));
		jc.addHandler(ReflectionJsonHandler.build(TestObjectParent.class));

		String js = jc.encode(top);
		assertEquals("{\"idParent\":0,\"to\":{\"id\":0,\"enumValue\":\"ONE\"},\"toList\":null}", js);

		TestObjectParent reconstituedTo = jc.decode(js, TestObjectParent.class);
		assertEquals(top, reconstituedTo);
	}

	@Test
	public void testTypedEncodeReflectionNoTypeMultipleLevelLists() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter(false);
		TestObjectParent top = new TestObjectParent();

		top.toList = new ArrayList<TestObject>();
		top.toList.add(new TestObject());
		top.toList.add(new TestObject());

		jc.addHandler(ReflectionJsonHandler.build(TestObject.class));
		jc.addHandler(ReflectionJsonHandler.build(TestObjectParent.class));

		String js = jc.encode(top);
		assertEquals("{\"idParent\":0,\"to\":null,\"toList\":[{\"id\":0,\"enumValue\":\"ONE\"},{\"id\":1,\"enumValue\":\"TWO\"}]}", js);

		TestObjectParent reconstituedTo = jc.decode(js, TestObjectParent.class);
		assertEquals(top, reconstituedTo);
		assertEquals(js, jc.encode(reconstituedTo));

	}

	@Test
	public void testJsonString() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter(false);
		jc.addHandler(ReflectionJsonHandler.build(TestObjectJsonString.class));

		TestObjectJsonString orig = new TestObjectJsonString();
		testTestObjectJsonString(jc, orig);
		orig.strValue = "String{}Value";
		testTestObjectJsonString(jc, orig);
		orig.jsonStringValue = "{\"field\":1}";
		testTestObjectJsonString(jc, orig);
		orig.jsonStringValue = "{}";
		testTestObjectJsonString(jc, orig);
		orig.jsonStringValue = null;
		testTestObjectJsonString(jc, orig);

		orig.jsonStringValue = "";
		String js = jc.encode(orig);
		TestObjectJsonString reconstituedTo = jc.decode(js, TestObjectJsonString.class);
		assertEquals(orig.strValue, reconstituedTo.strValue);
		assertEquals(null, reconstituedTo.jsonStringValue);

	}

	private void testTestObjectJsonString(JsonObjectConverter jc, TestObjectJsonString orig) throws Exception {
		String js = jc.encode(orig);

		if (orig.jsonStringValue != null) {
			assertFalse(js.indexOf(orig.jsonStringValue) < 0);
		}

		TestObjectJsonString reconstituedTo = jc.decode(js, TestObjectJsonString.class);
		assertEquals(TestUtils.displayFields(orig), TestUtils.displayFields(reconstituedTo));
		assertEquals(js, jc.encode(reconstituedTo));

		assertEquals(orig.strValue, reconstituedTo.strValue);
		assertEquals(orig.jsonStringValue, reconstituedTo.jsonStringValue);
	}

	public static class TestObjectJsonString {
		String strValue;
		@JsonString
		String jsonStringValue;
	}

	public static class TestObject {
		private static int ID = 0;
		int id = ID++;

		enum TOEnum {
			ONE, TWO, THREE
		}

		public TestObject() {

		}

		TOEnum enumValue = TOEnum.values()[id % (TOEnum.values().length)];

		@Override
		public boolean equals(Object o) {
			TestObject other = (TestObject) o;
			boolean b = other.id == id;
			b = b && other.enumValue == enumValue;
			return b;
		}

		@Override
		public String toString() {
			return "TestObject [id=" + id + ", enumValue=" + enumValue + "]";
		}

	}

	public static class TestObjectParent {
		private static int ID = 0;
		int idParent = ID++;
		TestObject to;

		@ListType(TestObject.class)
		List<TestObject> toList = null;

		public TestObjectParent() {

		}

		@Override
		public boolean equals(Object o) {
			TestObjectParent other = (TestObjectParent) o;
			boolean b = other.idParent == idParent;
			b = b && equalsNullCheck(other.to, to);
			b = b && equalsNullCheck(other.toList, toList);
			return b;
		}

		@Override
		public String toString() {
			return "TestObjectParent [idParent=" + idParent + ", to=" + to + ", toList=" + toList + "]";
		}

	}

	public static boolean equalsNullCheck(Object obj1, Object obj2) {
		boolean value = true;
		if (obj1 == obj2) {
			value = true;
		} else if (obj1 == null || obj2 == null) {
			value = false;
		} else {
			value = obj1.equals(obj2);
		}
		return value;
	}
}
