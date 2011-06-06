package com.google.code.infusion.json;

import java.io.IOException;
import java.io.StreamTokenizer;

/**
 * Helper to simplify data management in the pure Java implementation
 * of JsonArray and JsonObject.
 */
class Entry {
	static final Entry NULL = new Entry(JsonType.NULL, null);
	
	final JsonType type;
	final Object value;
	
	Entry(JsonType type, Object value) {
		this.type = type;
		this.value = value;
	}

	public static Entry parse(StreamTokenizer tokenizer) throws IOException {
		switch(tokenizer.ttype) {
		case  '[':
			return new Entry(JsonType.ARRAY, JsonArray.parse(tokenizer));
		case '{':
			return new Entry(JsonType.OBJECT, JsonObject.parse(tokenizer));
		case '"':
		case '\'':
			return new Entry(JsonType.STRING, tokenizer.sval);
		case StreamTokenizer.TT_WORD:
			if (tokenizer.sval.equals("true")) {
				return new Entry(JsonType.BOOLEAN, Boolean.TRUE);
			}
			if (tokenizer.sval.equals("false")) {
				return new Entry(JsonType.BOOLEAN, Boolean.FALSE);
			}
			throw new RuntimeException("Expected true or false; actual: " + tokenizer.sval);
		case StreamTokenizer.TT_NUMBER:
			return new Entry(JsonType.NUMBER, tokenizer.nval);
		default:
			throw new RuntimeException("Unexpected token: " + tokenizer.toString());
		}
	}

	public String serialize() {
		switch(type) {
		case ARRAY:
			return ((JsonArray) value).serialize();
		case OBJECT:
			return ((JsonObject) value).serialize();
		case STRING:
			return '"' + JsonObject.escape((String) value) + '"';
		default:
			return "" + value;
		}
	}
}
