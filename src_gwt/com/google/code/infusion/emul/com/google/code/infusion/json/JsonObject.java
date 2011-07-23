package com.google.code.infusion.json;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class JsonObject extends JavaScriptObject {
	
	protected JsonObject() {
	}
	
	public final String[] getKeys() {
		JsArrayString jsKeys = getKeysImpl();
		int len = jsKeys.length();
		String[] keys = new String[len];
		for (int i = 0; i < jsKeys.length(); i++) {
			keys[i] = jsKeys.get(i);
		}
		return keys;
	}
	
	private final native JsArrayString getKeysImpl() /*-{
		var result = [];
		for (key in this) {
		  if (key != "__gwt_ObjectId") {
			result.push(key);
		  }
		}
		return result;
	}-*/;
	
	public static native JsonObject parse(String json) /*-{
		return eval('(' + json + ')');
	}-*/;
	
	public final JsonType getType(String key) {
		return JsonType.values()[getTypeImpl(key)];
	}
	
	public final native JsonArray getArray(String key) /*-{
		return this[key];
	}-*/;

	public final native boolean getBoolean(String key) /*-{
		return this[key];
	}-*/;

	public final native double getNumber(String key) /*-{
		return this[key];
	}-*/;

	public final native JsonObject getObject(String key) /*-{
		return this[key];
	}-*/;

	public final native String getString(String key) /*-{
		return this[key];
	}-*/;
	
    public final native String getAsString(String key) /*-{
       return this[key] ? "" + this[key] : "";
    }-*/;
	
	public final native void setString(String key, String value) /*-{
		this[key] = value;
	}-*/;

	public final native void setBoolean(String key, boolean value) /*-{
		this[key] = value;
	}-*/;

	public final native void setNumber(String key, double value) /*-{
		this[key] = value;
	}-*/;
	
	public final native void setArray(String key, JsonArray value) /*-{
		this[key] = value;
	}-*/;

	public final native void setObject(String key, JsonObject value) /*-{
		this[key] = value;
	}-*/;
	
	
	private final native int getTypeImpl(String key) /*-{
		var o = this[key];
		if (o == null) {
			return 2;
		}
		var type = typeof o;
		if (type == "boolean") {
			return 1;
		}
		if (type == "number") {
			return 3;
		}
		if (type == "string") {
			return 5;
		}
		return o instanceof Array ? 0 : 4;
	}-*/;
	
	public static JsonObject create() {
		return (JsonObject) JavaScriptObject.createObject();
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

	public final String serialize() {
		StringBuilder sb = new StringBuilder("{");
		for (String key: getKeys()) {
			sb.append('"');
			sb.append(escape(key));
			sb.append("\":");
			switch(getType(key)) {
			case ARRAY:
				sb.append(getArray(key).serialize());
				break;
			case BOOLEAN:
				sb.append(getBoolean(key) ? "true" : "false");
				break;
			case NUMBER:
				sb.append("" + getNumber(key));
				break;
			case OBJECT:
				sb.append(getObject(key).serialize());
				break;
			case STRING:
				sb.append('"');
				sb.append(escape(getString(key)));
				sb.append('"');
				break;
			default:
				throw new RuntimeException("Unsupported Type:" + getType(key));
			}
			sb.append(',');
		}
		sb.append('}');
		return sb.toString();
	}
}
