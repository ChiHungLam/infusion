package com.google.code.infusion.datastore;

import java.util.TreeMap;

public class Entity {

  Key key;
  TreeMap<String, Object> properties = new TreeMap<String, Object>();

  public Entity(String tableId) {
    key = new Key();
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

}
