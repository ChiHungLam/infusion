package com.google.code.infusion.datastore;

public class Key {

	String kind;
	String name;

	Key(String kind, String name) {
	  this.kind = kind;
	  this.name = name;
	}
	
	Key(String kind) {
	  this(kind, null);
	}
	
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
