package com.google.code.infusion.datastore;

public class TableInfo {
	private String name;
	private String id;

	TableInfo(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
	
	public String toString() {
		return "table name: '" + name + "' id: '" + id + "'";
	}
}
