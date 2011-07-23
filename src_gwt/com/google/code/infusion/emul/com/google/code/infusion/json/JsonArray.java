package com.google.code.infusion.json;

import com.google.gwt.core.client.JavaScriptObject;

public class JsonArray extends JavaScriptObject {

	protected JsonArray() {
		
	}
	
	public static JsonArray create() {
		return (JsonArray) JavaScriptObject.createArray();
	}
	

	public static native JsonArray parse(String json) /*-{
		return eval('(' + json + ')');
	}-*/;
	
	
	public final JsonType getType(int index) {
		return JsonType.values()[getTypeImpl(index)];
	}
	
	public final native JsonArray getArray(int index) /*-{
		return this[index];
	}-*/;	

	public final native boolean getBoolean(int index) /*-{
		return this[index];
	}-*/;

	public final native double getNumber(int index) /*-{
		return this[index];
	}-*/;

	public final native JsonObject getObject(int index) /*-{
		return this[index];
	}-*/;

	public final native String getString(int index) /*-{
		return this[index];
	}-*/;

	public final native String getAsString(int index) /*-{
	  return  this[index] ? "" + this[index] : "";
	}-*/;
	
    public final native void setArray(int index, JsonArray value) /*-{
      this[index] = value;
    }-*/;

    public final native void setBoolean(int index, boolean value) /*-{
   		this[index] = value;
	}-*/;

	public final native void setNumber(int index, double value) /*-{
   		this[index] = value;
	}-*/;

	public final native void setString(int index, String value) /*-{
	   	this[index] = value;
	}-*/;

	public final native void setObject(int index, JsonObject value) /*-{
   		this[index] = value;
	}-*/;

	public final native int length() /*-{
		return this.length;
	}-*/;
	
	
	public final String serialize() {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < length(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			switch(getType(i)) {
			case ARRAY:
				sb.append(getArray(i).serialize());
				break;
			case BOOLEAN:
				sb.append(getBoolean(i) ? "true" : "false");
				break;
			case NULL:
				break;
			case NUMBER:
				sb.append("" + getNumber(i));
				break;
			case OBJECT:
				sb.append(getObject(i).serialize());
				break;
			case STRING:
				sb.append('"');
				sb.append(JsonObject.escape(getString(i)));
				sb.append('"');
				break;
			default:
				throw new RuntimeException("Unsupported Type:" + getType(i));
			}
		}
		sb.append(']');
		return sb.toString();
	}

	
	private final native int getTypeImpl(int index) /*-{
		var o = this[index];
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
}
