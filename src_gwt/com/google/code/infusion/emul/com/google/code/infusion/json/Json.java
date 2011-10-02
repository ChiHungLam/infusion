package com.google.code.infusion.json;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class Json extends JavaScriptObject {

  public enum Type{
    BOOLEAN,   // 0
    NULL,      // 1
    NUMBER,    // 2
    OBJECT,    // 3
    STRING     // 4
  }
  
  public static Json createArray() {
    return (Json) JavaScriptObject.createArray();
  }

  public static Json createObject() {
    return (Json) JavaScriptObject.createObject();
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

  protected Json() {
  }

  public final native String getAsString(int index) /*-{
    return  this[index] ? "" + this[index] : "";
  }-*/;

  public final native String getAsString(String key) /*-{
    return this[key] ? "" + this[key] : "";
  }-*/;

  public final native boolean getBoolean(int index) /*-{
    return this[index];
  }-*/;

  public final native boolean getBoolean(String key) /*-{
    return this[key];
  }-*/;

  public final native Json getJson(int index) /*-{
    return this[index];
  }-*/; 

  public final native Json getJson(String key) /*-{
    return this[key];
  }-*/;

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

  public final native double getNumber(int index) /*-{
    return this[index];
  }-*/;

  public final native double getNumber(String key) /*-{
    return this[key];
  }-*/;
  
  public final native String getString(int index) /*-{
    return this[index];
  }-*/;

  public final native String getString(String key) /*-{
    return this[key];
  }-*/;


  public final Type getType(int index) {
    return Type.values()[getTypeImpl(index)];
  }

  public final Type getType(String key) {
    return Type.values()[getTypeImpl(key)];
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
    return 4;
  }-*/;
  
  private final native boolean isArray() /*-{
    return this.length != null;
  }-*/;
  

  private final native int getTypeImpl(String key) /*-{
    var o = this[key];
    if (o == null) {
      return 1;
    }
    var type = typeof o;
    if (type == "boolean") {
      return 0;
    }
    if (type == "number") {
      return 2;
    }
    if (type == "string") {
      return 4;
    }
    return 3;
  }-*/;

  public final native int length() /*-{
    return this.length;
  }-*/;

  public static native Json parse(String json) /*-{
    return eval('(' + json + ')');
  }-*/;

  public final String serialize() {
    StringBuilder sb = new StringBuilder();
    if (isArray()) {
      sb.append('[');
      for (int i = 0; i < length(); i++) {
        if (i > 0) {
          sb.append(',');
        }
        switch(getType(i)) {
        case BOOLEAN:
          sb.append(getBoolean(i) ? "true" : "false");
          break;
        case NULL:
          break;
        case NUMBER:
          sb.append("" + getNumber(i));
          break;
        case OBJECT:
          sb.append(getJson(i).serialize());
          break;
        case STRING:
          sb.append('"');
          sb.append(escape(getString(i)));
          sb.append('"');
          break;
        default:
          throw new RuntimeException("Unsupported Type:" + getType(i));
        }
      }
      sb.append(']');
    } else {
      sb.append('{');
      for (String key: getKeys()) {
        sb.append('"');
        sb.append(escape(key));
        sb.append("\":");
        switch(getType(key)) {
        case BOOLEAN:
          sb.append(getBoolean(key) ? "true" : "false");
          break;
        case NUMBER:
          sb.append("" + getNumber(key));
          break;
        case OBJECT:
          sb.append(getJson(key).serialize());
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
    }
    return sb.toString();
  }

  public final native void setBoolean(int index, boolean value) /*-{
    this[index] = value;
  }-*/;

  public final native void setBoolean(String key, boolean value) /*-{
    this[key] = value;
  }-*/;
  
  public final native void setJson(int index, Json value) /*-{
    this[index] = value;
  }-*/;
  
  public final native void setJson(String key, Json value) /*-{
    this[key] = value;
  }-*/;

  public final native void setNumber(int index, double value) /*-{
    this[index] = value;
  }-*/;

  public final native void setNumber(String key, double value) /*-{
    this[key] = value;
  }-*/;

  public final native void setString(int index, String value) /*-{
    this[index] = value;
  }-*/;

  public final native void setString(String key, String value) /*-{
    this[key] = value;
  }-*/;
}