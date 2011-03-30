package com.google.code.infusion.datastore;

import java.util.TreeMap;

public class TableMetaData {
	private String tableId;
	private TreeMap<String,ColumnType<?>> columns = new TreeMap<String,ColumnType<?>>();
	
	TableMetaData(String tableId) {
		this.tableId = tableId;
	}
	
	public void setColumnType(String name, ColumnType<?> type) {
		columns.put(name, type);
	}
	
}
