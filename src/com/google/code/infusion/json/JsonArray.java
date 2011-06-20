package com.google.code.infusion.json;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;


public class JsonArray  {	

	ArrayList<Entry> data = new ArrayList<Entry>();
	
	protected JsonArray() {
		
	}
	
	public final JsonType getType(int index) {
		Entry o = data.get(index);
		return o == null ? JsonType.NULL : o.type;
	}
	
	public final JsonArray getArray(int index) {
		return (JsonArray) data.get(index).value;
	}

	public final boolean getBoolean(int index) {
		return (Boolean) data.get(index).value;
	}

	public final double getNumber(int index) {
		return (Double) data.get(index).value;
	}


	public final JsonObject getObject(int i) {
		return (JsonObject) data.get(i).value;
	}
	
	
	public final String getString(int index) {
		return (String) data.get(index).value;
	}	
	
	public final int length() {
		return data.size();
	}

	public String serialize() {
		if (data.size() == 0) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		if (data.size() > 0) {
			sb.append(data.get(0).serialize());
			for (int i = 1; i < data.size(); i++) {
				sb.append(',');
				sb.append(data.get(i).serialize());
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public final void setObject(int i, JsonObject json) {
		set(i, new Entry(JsonType.OBJECT, json));
	}
	
	private void set(int i, Entry entry) {
		while (data.size() <= i) {
			data.add(Entry.NULL);
		}
		data.set(i, entry);
	}

	public static JsonArray create() {
		return new JsonArray();
	}

	public static JsonArray parse(String json) {
		System.out.println("parsing: '" + json+"'");
		try {
			StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(json));
			tokenizer.quoteChar('"');
			tokenizer.quoteChar('\'');
			tokenizer.nextToken();
			JsonArray result = parse(tokenizer);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setBoolean(int i, boolean value) {
		set(i, new Entry(JsonType.BOOLEAN, value));
	}

	public void setNumber(int i, double value) {
		set(i, new Entry(JsonType.NUMBER, value));
	}

	public void setString(int i, String value) {
		set(i, new Entry(JsonType.STRING, value));
	}

	public static JsonArray parse(StreamTokenizer tokenizer) throws IOException {
		JsonArray result = JsonArray.create();
		if (tokenizer.ttype != '[') {
			throw new RuntimeException("'[' expected");
		}
		tokenizer.nextToken();
		int index = 0;
		while (tokenizer.ttype != ']') {
			if (tokenizer.ttype == ',') {
				index++;
				tokenizer.nextToken();
				continue;
			}
			result.set(index++, Entry.parse(tokenizer));
			tokenizer.nextToken();
			if (tokenizer.ttype == ',') {
				tokenizer.nextToken();
			} else if (tokenizer.ttype != ']') {
				throw new RuntimeException("',' or ']' expected");
			}
		}
		
		return result;
	}

  public void setArray(int i, JsonArray value) {
    set(i, new Entry(JsonType.ARRAY, value));
  }


}
