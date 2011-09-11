package com.google.code.infusion.json;

import com.google.code.infusion.json.JsonObject;

import junit.framework.TestCase;

public class JsonObjectTest extends TestCase {

	public void testJsonObject() {
		JsonObject jso = JsonObject.create();
		jso.setString("testKey", "testValue");
		
		jso = JsonObject.parse(jso.serialize());
		
		assertEquals("testValue", jso.getString("testKey"));
	}
	
}
