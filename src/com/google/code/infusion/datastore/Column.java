package com.google.code.infusion.datastore;

public class Column {
	private String name;
	private ColumnType<?> type;
	
	Column(String name, ColumnType<?> type) {
		this.name = name;
		this.type = type;
	}
	
	
	public String toString() {
		return name + ": " + type;
	}
	
	public String getName() {
		return name;
	}
	
	
	public ColumnType<?> getType() {
		return type;
	}
	
}
