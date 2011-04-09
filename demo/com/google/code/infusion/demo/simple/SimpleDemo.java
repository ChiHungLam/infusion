package com.google.code.infusion.demo.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.code.infusion.datastore.ColumnInfo;
import com.google.code.infusion.datastore.FusionTableService;
import com.google.code.infusion.datastore.TableInfo;
import com.google.code.infusion.util.ClientLogin;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SimpleDemo {
	static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	static FusionTableService service;
	
	public static void main(String[] args) throws IOException {
		
		System.out.print("Username (email): ");
		String user = reader.readLine();
		if (user == null || user.length() == 0) {
			return;
		}

		System.out.print("Password (email): ");
		String password = reader.readLine();
		if (password == null || password.length() == 0) {
			return;
		}
		System.out.println("\f");

		ClientLogin.requestAuthToken(user, password, new AsyncCallback<String>() {
			public void onSuccess(String result) {
				runSession(result);
			}
		
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				return;
			}
		});
	}
	
	
	private static void runSession(String authToken) {
		System.out.println("Authenticated Sucessfully; Auth token: " + authToken);
		service = new FusionTableService(authToken);
		showPrompt();
		while (true) {
			try {
				String cmd = reader.readLine();
				if (cmd == null || cmd.length() == 0) {
					break;
				} else if ("?".equals(cmd) || "help".equals(cmd)) {
					System.out.println("show tables:         List available tables");
					System.out.println("describe <table id>: Show table structure");
				} else if ("show tables".equals(cmd)) {
					showTables();
				} else if (cmd.startsWith("describe ")) {
					describe(cmd.substring(9).trim());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void describe(String tableId) {
		service.describe(tableId, new AsyncCallback<List<ColumnInfo>>() {
			public void onFailure(Throwable caught) {
				showError(caught);
			}

			public void onSuccess(List<ColumnInfo> result) {
				System.out.println("Columns:");
				for(ColumnInfo ci: result) {
					System.out.println(ci);
				}
				showPrompt();		
			}
		});
	}
	
	private static void showTables() {	
		service.showTables(new AsyncCallback<List<TableInfo>>() {
			public void onFailure(Throwable caught) {
				showError(caught);
			}

			public void onSuccess(List<TableInfo> result) {
				System.out.println("Tables available:");
				for(TableInfo td: result) {
					System.out.println(td);
				}
				showPrompt();			
			}
		});
	}
	
	private static void showPrompt() {
		System.out.print("Command ('help' for help)> ");
	}


	private static void showError(Throwable caught) {
		caught.printStackTrace();
		showPrompt();
	}
}
