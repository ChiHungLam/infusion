package com.google.code.infusion.datastore;

public class ColumnInfo {
	private String name;
	private ColumnType<?> type;
	
	ColumnInfo(String name, ColumnType<?> type) {
		this.name = name;
		this.type = type;
	}
	
	String getName() {
		return name;
	}
	
	ColumnType<?> getType() {
		return type;
	}
}
