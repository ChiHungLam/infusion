package com.google.code.infusion.json;

import com.google.code.infusion.json.Json;

import junit.framework.TestCase;

public class JsonTest extends TestCase {

	public void testJsonObject() {
		Json json = Json.createObject();
		json.setString("testKey", "testValue");
		
		json = Json.parse(json.serialize());
		
		assertEquals("testValue", json.getString("testKey"));
	}
	
}
