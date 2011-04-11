package com.google.code.infusion.datastore;

public class ColumnInfo {
	private String name;
	private ColumnType<?> type;
	
	public ColumnInfo(String name, ColumnType<?> type) {
		this.name = name;
		this.type = type;
	}
	
	String getName() {
		return name;
	}
	
	ColumnType<?> getType() {
		return type;
	}
	
	public String toString() {
	  return name + ": " + type;
	}
}
