package com.google.code.infusion.json;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.print.DocFlavor.STRING;


public class JsonObject {

	HashMap<String,Entry> data = new HashMap<String,Entry>();
	
	protected JsonObject() {
	}
	
	public static JsonObject parse(String json) {
		try {
			StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(json));
			tokenizer.quoteChar('"');
			tokenizer.quoteChar('\'');
			tokenizer.nextToken();
			JsonObject result = parse(tokenizer);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	static JsonObject parse(StreamTokenizer tokenizer) throws IOException {
		if (tokenizer.ttype != '{') {
			throw new RuntimeException("{ expected; observed: " + tokenizer.ttype + " / " + tokenizer.sval);
		}
		tokenizer.nextToken();
		JsonObject result = new JsonObject();
		while (tokenizer.ttype != '}') {
			String name = tokenizer.sval;
			tokenizer.nextToken();
			if (tokenizer.ttype != ':') {
				throw new RuntimeException(": expected");
			}
			tokenizer.nextToken();
			result.data.put(name, Entry.parse(tokenizer));
			tokenizer.nextToken();
			if(tokenizer.ttype == ',') {
				tokenizer.nextToken();
			} else if (tokenizer.ttype != '}') {
				throw new RuntimeException("}Êor , expected; actual: " + tokenizer.ttype);
			}
		}
		return result;
	}
	
	public final void setString(String name, String value) {
		data.put(name, new Entry(JsonType.STRING, value));
	}

	public void setNumber(String name, double value) {
		data.put(name, new Entry(JsonType.NUMBER, value));
	}	

	public void setBoolean(String name, boolean value) {
		data.put(name, new Entry(JsonType.BOOLEAN, value));
	}	

	public final String[] getKeys() {
		String [] result = new String[data.size()];
		data.keySet().toArray(result);
		return result;
	}
	
	public final JsonType getType(String key) {
		Entry o = data.get(key);
		return o == null ? JsonType.NULL : o.type;
	}
	
	private final Entry get(String key) {
		Entry e = data.get(key);
		return e == null ? Entry.NULL : e;
	}
	
	public final String getAsString(String key) {
	  Entry e = data.get(key);
	  return e == null ? "" : e.toString();
	}
	
	
	public final JsonArray getArray(String key) {
		return (JsonArray) get(key).value;
	}

	public final boolean getBoolean(String key) {
		return (Boolean) get(key).value;
	}

	public final double getNumber(String key) {
		return (Double) get(key).value;
	}

	public final String getString(String key) {
		return (String) get(key).value;
	}

	public JsonObject getObject(String key) {
		return (JsonObject) get(key).value;	
	}

	public final String serialize() {
		StringBuilder sb = new StringBuilder("{");
		for (Map.Entry<String, Entry> e : data.entrySet()) {
			sb.append('"');
			sb.append(escape(e.getKey()));
			sb.append("\":");
			sb.append(e.getValue().serialize());
			sb.append(',');
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static String escape(String s) {
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch(c) {
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '"':
				sb.append("\\\"");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static JsonObject create() {
		return new JsonObject();
	}

	public void setArray(String string, JsonArray value) {
		data.put(string, new Entry(JsonType.ARRAY, value));
	}

	public void setObject(String key, Object value) {
		data.put(key, new Entry(JsonType.OBJECT, value));
	}


}
