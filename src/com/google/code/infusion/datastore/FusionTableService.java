package com.google.code.infusion.datastore;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.code.infusion.util.ChainedCallback;
import com.google.code.infusion.util.CsvParser;
import com.google.code.infusion.util.HttpRequest;
import com.google.code.infusion.util.HttpRequest.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class FusionTableService {

	private String authToken;
	private HashMap<String, TableMetaData> tables = new HashMap<String,TableMetaData>();

	public FusionTableService(String authToken) {
		this.authToken = authToken;
	}
	
	public void getTableMetaData(final String tableId, final AsyncCallback<TableMetaData> callback) {
		TableMetaData table = tables.get(tableId);
		if (table != null) {
			callback.onSuccess(table);
			return;
		}
		
		execSql("DESCRIBE " + tableId, new ChainedCallback<String[]>(callback) {
			@Override
			public void onSuccess(String[] rows) {

				TableMetaData table = new TableMetaData(tableId);

				for (int i = 1; i < rows.length; i++) {
					String[] parts = CsvParser.parse(rows[i]);
					table.setColumnType(parts[1], ColumnType.STRING);
				}
				
				tables.put(tableId, table);
				callback.onSuccess(table);
			}
		});
		
	}
	
	
	/* package-visible */
	void execSql(String command, final AsyncCallback<String[]> callback) {
		HttpRequest request;
		try {
			request = new HttpRequest("https://www.google.com/fusiontables/api/query?sql=" + 
				URLEncoder.encode(command, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		request.addHeader("Authorization", "GoogleLogin auth="+authToken);
		request.request(new ChainedCallback<HttpRequest.Response>(callback) {
			@Override
			public void onSuccess(Response result) {
				System.out.println("SQL result: " + result.getData());
				
				callback.onSuccess(result.getData().split("\n"));
			}
		});
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
			execSql("SELECT * FROM " + query.getKind(), new ChainedCallback<String[]>(callback) {
				@Override
				public void onSuccess(String[] result) {
					String[] names = null;
					ArrayList<Entity> entities = new ArrayList<Entity>();
					for (String line: result) {
						String[] parts = CsvParser.parse(line);
						if (names == null) {
							names = parts;
						} else {
							Entity entity = new Entity(query.getKind());
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
