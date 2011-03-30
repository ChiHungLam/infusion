package com.google.code.infusion.datastore;


public class Key {

	String kind;
	String name;
	
	public boolean isComplete() {
	  return name != null;
	}
	
	public String getKind() {
	  return kind;
	}
		
	public String getName() {
	  return name;
	}
	
}
