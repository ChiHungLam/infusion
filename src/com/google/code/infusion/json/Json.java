package com.google.code.infusion.json;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Json {
  public enum Type{
    BOOLEAN,   // 0
    NULL,      // 1
    NUMBER,    // 2
    OBJECT,    // 3
    STRING     // 4
  }
  
  HashMap<String,Entry> map;
  ArrayList<Entry> array;

  public static Json createArray() {
    Json json = new Json();
    json.array = new ArrayList<Entry>();
    return json;
  }

  public static Json createObject() {
    Json json = new Json();
    json.map = new HashMap<String,Entry>();
    return json;
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

  public final Type getType(int index) {
    Entry o = array.get(index);
    return o == null ? Type.NULL : o.type;
  }
  
  protected Json() {
  }

  private final Entry get(String key) {
    Entry e = map.get(key);
    return e == null ? Entry.NULL : e;
  }

  public final String getAsString(int index) {
    Entry e = array.get(index);
    return e == null ? "" : e.toString();
  }

  public final String getAsString(String key) {
    Entry e = map.get(key);
    return e == null ? "" : e.toString();
  }

  public final boolean getBoolean(int index) {
    return (Boolean) array.get(index).value;
  }

  public final boolean getBoolean(String key) {
    return (Boolean) get(key).value;
  }

  public final Json getJson(int index) {
    return (Json) array.get(index).value;
  }

  public final Json getJson(String key) {
    return (Json) map.get(key).value;
  }

  public final double getNumber(int index) {
    return (Double) array.get(index).value;
  }

  public final double getNumber(String key) {
    return (Double) get(key).value;
  }

  public final String getString(int index) {
    return (String) array.get(index).value;
  } 
  
  public final String getString(String key) {
    return (String) get(key).value;
  }

  public final Type getType(String key) {
    Entry o = map.get(key);
    return o == null ? Type.NULL : o.type;
  }

  public final String[] getKeys() {
    String [] result = new String[map.size()];
    map.keySet().toArray(result);
    return result;
  }

  boolean isArray() {
    return array != null;
  }

  public final int length() {
    return array.size();
  }

  public static Json parse(String json) {
    try {
      StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(json));
      tokenizer.quoteChar('"');
      tokenizer.quoteChar('\'');
      tokenizer.nextToken();
      Json result = parse(tokenizer);
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  static Json parse(StreamTokenizer tokenizer) throws IOException {
    Json result;
    if (tokenizer.ttype == '{') {
      tokenizer.nextToken();
      result = createObject();
      while (tokenizer.ttype != '}') {
        String name = tokenizer.sval;
        tokenizer.nextToken();
        if (tokenizer.ttype != ':') {
          throw new RuntimeException(": expected");
        }
        tokenizer.nextToken();
        result.map.put(name, Entry.parse(tokenizer));
        tokenizer.nextToken();
        if(tokenizer.ttype == ',') {
          tokenizer.nextToken();
        } else if (tokenizer.ttype != '}') {
          throw new RuntimeException("}Êor , expected; actual: " + tokenizer.ttype);
        }
      } 
    } else if (tokenizer.ttype == '[') {
      result = createArray();
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
    } else {
      throw new RuntimeException("'[' or '{' expected");
    }
    return result;
  }

  public final String serialize() {
    StringBuilder sb = new StringBuilder();
    if (isArray()) {
      if (array.size() == 0) {
        return "[]";
      }
      sb.append('[');
      if (array.size() > 0) {
        sb.append(array.get(0).serialize());
        for (int i = 1; i < array.size(); i++) {
          sb.append(',');
          sb.append(array.get(i).serialize());
        }
      }
      sb.append(']');
    } else {
      sb.append('{');
      for (Map.Entry<String, Entry> e : map.entrySet()) {
        sb.append('"');
        sb.append(escape(e.getKey()));
        sb.append("\":");
        sb.append(e.getValue().serialize());
        sb.append(',');
      }
      sb.append('}');
    }
    return sb.toString();
  }

  private void set(int i, Entry entry) {
    if (array == null) {
      throw new RuntimeException("Json must be array for index based access.");
    }
    while (array.size() <= i) {
      array.add(Entry.NULL);
    }
    array.set(i, entry);
  }

  public void setBoolean(int i, boolean value) {
    set(i, new Entry(Type.BOOLEAN, value));
  }

  public void setBoolean(String name, boolean value) {
    map.put(name, new Entry(Type.BOOLEAN, value));
  }

  public void setJson(int i, Json value) {
    set(i, new Entry(Type.OBJECT, value));
  }

  public void setJson(String string, Json value) {
    map.put(string, new Entry(Type.OBJECT, value));
  }

  public void setNumber(int i, double value) {
    set(i, new Entry(Type.NUMBER, value));
  }

  public void setNumber(String name, double value) {
    map.put(name, new Entry(Type.NUMBER, value));
  } 

  public void setString(int i, String value) {
    set(i, new Entry(Type.STRING, value));
  }

  public final void setString(String name, String value) {
    map.put(name, new Entry(Type.STRING, value));
  }

}
