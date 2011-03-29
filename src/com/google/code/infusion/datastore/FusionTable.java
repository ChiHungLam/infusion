package com.google.code.infusion.datastore;

import java.util.ArrayList;
import java.util.List;

import com.google.code.infusion.util.ChainedCallback;
import com.google.code.infusion.util.CsvParser;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FusionTable {
	private FusionTableService service;
	private String tableId;
	private List<Column> columns;
	
	FusionTable(FusionTableService service, String tableId, List<Column> columns) {
		this.service = service;
		this.tableId = tableId;
		this.columns = columns;
		
		System.out.println("Table created: " + tableId + ": " + columns);
	}
	
	
	
	public PreparedQueryAsync prepareQuery(Query query) {
		return new FusionTableQuery(query);
	}

	

	class FusionTableQuery implements PreparedQueryAsync {
		
		Query query;
		FusionTableQuery(Query query) {
			this.query = query;
		}
		
		
		@Override
		public void asList(final AsyncCallback<List<Entity>> callback) {
			service.execSql("SELECT * FROM " + tableId, new ChainedCallback<String[]>(callback) {
				@Override
				public void onSuccess(String[] result) {
					String[] names = null;
					ArrayList<Entity> entities = new ArrayList<Entity>();
					for (String line: result) {
						String[] parts = CsvParser.parse(line);
						if (names == null) {
							names = parts;
						} else {
							Entity entity = new Entity(null);
							for (int i = 0; i < parts.length; i++) {
								entity.setProperty(names[i], parts[i]);
							}
							entities.add(entity);
						}
					}
					callback.onSuccess(entities);
				}
			});
		}
		
	}
	
}
