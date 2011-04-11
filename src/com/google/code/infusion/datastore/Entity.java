package com.google.code.infusion.datastore;

import java.util.Map;
import java.util.TreeMap;

public class Entity {

  Key key;
  TreeMap<String, Object> properties = new TreeMap<String, Object>();

  public Entity(String tableId) {
    this(new Key());
    key.kind = tableId;
  }

  Entity(Key key) {
    this.key = key;
  }

  public String toString() {
    return "" + key + ": " + properties;
  }

  public void setProperty(String name, Object value) {
    properties.put(name, value);
  }

  public Key getKey() {
    return key;
  }

  public Map<String, Object> getProperties() {
    return properties; // Is a defensive copy or immutability needed here?
  }

  public Object getProperty(String name) {
    return properties.get(name);
  }

}
