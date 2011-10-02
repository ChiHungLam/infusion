package com.google.code.infusion.json;

import java.io.IOException;
import java.io.StreamTokenizer;

/**
 * Helper to simplify data management in the pure Java implementation
 * of JsonArray and JsonObject.
 */
class Entry {
	static final Entry NULL = new Entry(Json.Type.NULL, null);
	
	final Json.Type type;
	final Object value;
	
	Entry(Json.Type type, Object value) {
		this.type = type;
		this.value = value;
	}

	public static Entry parse(StreamTokenizer tokenizer) throws IOException {
		switch(tokenizer.ttype) {
		case  '[':
		case '{':
			return new Entry(Json.Type.OBJECT, Json.parse(tokenizer));
		case '"':
		case '\'':
			return new Entry(Json.Type.STRING, tokenizer.sval);
		case StreamTokenizer.TT_WORD:
			if (tokenizer.sval.equals("true")) {
				return new Entry(Json.Type.BOOLEAN, Boolean.TRUE);
			}
			if (tokenizer.sval.equals("false")) {
				return new Entry(Json.Type.BOOLEAN, Boolean.FALSE);
			}
			throw new RuntimeException("Expected true or false; actual: " + tokenizer.sval);
		case StreamTokenizer.TT_NUMBER:
			return new Entry(Json.Type.NUMBER, tokenizer.nval);
		default:
			throw new RuntimeException("Unexpected token: " + tokenizer.toString());
		}
	}

	public String serialize() {
		switch(type) {
    case OBJECT:
			return ((Json) value).serialize();
		case STRING:
			return '"' + Json.escape((String) value) + '"';
		default:
			return "" + value;
		}
	}
	
	public String toString() {
	  return "" + value;
	}
}
