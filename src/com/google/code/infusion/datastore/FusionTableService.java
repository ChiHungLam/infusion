package com.google.code.infusion.datastore;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.code.infusion.util.ChainedCallback;
import com.google.code.infusion.util.RequestBuilder;
import com.google.code.infusion.util.Util;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FusionTableService {

	private String authToken;
	private HashMap<String, List<ColumnInfo>> tables = new HashMap<String, List<ColumnInfo>>();

	public FusionTableService(String authToken) {
		this.authToken = authToken;
	}

	public void showTables(final AsyncCallback<List<TableInfo>> callback) {
		execSql("SHOW TABLES", new ChainedCallback<String[]>(callback) {

			public void onSuccess(String[] rows) {
				ArrayList<TableInfo> result = new ArrayList<TableInfo>();
				for (int i = 1; i < rows.length; i++) {
					String[] parts = Util.parseCsv(rows[i]);
					result.add(new TableInfo(parts[0], parts[1]));
				}
				callback.onSuccess(result);
			}
		});
	}
	
	
	public void describe(final String tableId,
			final AsyncCallback<List<ColumnInfo>> callback) {
		List<ColumnInfo> table = tables.get(tableId);
		if (table != null) {
			callback.onSuccess(table);
			return;
		}

		execSql("DESCRIBE " + tableId, new ChainedCallback<String[]>(callback) {
			public void onSuccess(String[] rows) {
				List<ColumnInfo> table = new ArrayList<ColumnInfo>();
				for (int i = 1; i < rows.length; i++) {
					String[] parts = Util.parseCsv(rows[i]);
					table.add(new ColumnInfo(parts[1], ColumnType.STRING));
				}
				tables.put(tableId, table);
				callback.onSuccess(table);
			}
		});
	}

	private void execSql(String command, final AsyncCallback<String[]> callback) {
		RequestBuilder rb;
		try {
			rb = new RequestBuilder(RequestBuilder.GET,
					"https://www.google.com/fusiontables/api/query?sql="
							+ URLEncoder.encode(command, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		rb.setHeader("Authorization", "GoogleLogin auth=" + authToken);	
		rb.sendRequest(null, new RequestCallback() {
			public void onResponseReceived(Request request,
					com.google.gwt.http.client.Response response) {
				System.out.println("SQL result: " + response.getText());
				callback.onSuccess(response.getText().split("\n"));
			}
			
			public void onError(Request request, Throwable exception) {
				callback.onFailure(exception);
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

		public void asList(final AsyncCallback<List<Entity>> callback) {
			execSql("SELECT * FROM " + query.toString(),
					new ChainedCallback<String[]>(callback) {
						public void onSuccess(String[] result) {
							String[] names = null;
							ArrayList<Entity> entities = new ArrayList<Entity>();
							for (String line : result) {
								String[] parts = Util.parseCsv(line);
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
