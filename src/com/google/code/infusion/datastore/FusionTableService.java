package com.google.code.infusion.datastore;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.code.infusion.util.ChainedCallback;
import com.google.code.infusion.util.CsvParser;
import com.google.code.infusion.util.HttpRequest;
import com.google.code.infusion.util.HttpRequest.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class FusionTableService {

	private String authToken;
	private HashMap<String, FusionTable> tables = new HashMap<String,FusionTable>();

	public FusionTableService(String authToken) {
		this.authToken = authToken;
	}
	
	public void getTable(final String tableId, final AsyncCallback<FusionTable> callback) {
		FusionTable table = tables.get(tableId);
		if (table != null) {
			callback.onSuccess(table);
			return;
		}
		
		execSql("DESCRIBE " + tableId, new ChainedCallback<String[]>(callback) {
			@Override
			public void onSuccess(String[] rows) {
				ArrayList<Column> columns = new ArrayList<Column>();
				
				for (int i = 1; i < rows.length; i++) {
					String[] parts = CsvParser.parse(rows[i]);
					columns.add(new Column(parts[1], ColumnType.STRING));
				}
				
				FusionTable table = new FusionTable(FusionTableService.this, tableId, columns);
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
	
	
	
	
}
