package com.google.code.infusion.datastore;

public class KeyFactory {

  public static Key createKey(String kind, String name) {
    return new Key(kind, name);
  }
  
  public static String keyToString(Key key) {
    return key.getKind() + "." + key.getName();
  }
  
  public static Key stringToKey(String s) {
    int cut = s.indexOf('.');
    return createKey(s.substring(0, cut), s.substring(cut + 1));
  }
}
