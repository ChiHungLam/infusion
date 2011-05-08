package com.google.code.infusion.demo.simple;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.code.infusion.datastore.ColumnInfo;
import com.google.code.infusion.datastore.ColumnType;
import com.google.code.infusion.datastore.Entity;
import com.google.code.infusion.datastore.FusionTableService;
import com.google.code.infusion.datastore.Key;
import com.google.code.infusion.datastore.TableDescription;
import com.google.code.infusion.datastore.TableInfo;
import com.google.code.infusion.importer.BibtexParser;
import com.google.code.infusion.importer.CsvParser;
import com.google.code.infusion.util.AsyncCallback;
import com.google.code.infusion.util.ClientLogin;

public class SimpleDemo {
  static BufferedReader reader = new BufferedReader(new InputStreamReader(
      System.in));
  static FusionTableService service;

  public static void main(String[] args) throws IOException {

    System.out.print("Username (email): ");
    String user = reader.readLine();
    if (user == null || user.length() == 0) {
      return;
    }

    System.out.print("Password: ");
    String password = reader.readLine();
    if (password == null || password.length() == 0) {
      return;
    }
    for (int i = 0; i < 50; i++) {
      System.out.println();
    }

    ClientLogin.requestAuthToken(ClientLogin.ACCOUNT_TYPE_GOOGLE, user,
        password, "fusiontables", "GoogleCodeProjectInfusion-Infusion-0.1",
        new AsyncCallback<String>() {
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
    showHelp();
    while (true) {
      try {
        String cmd = reader.readLine();
        if ("exit".equals(cmd) || "quit".equals(cmd)) {
          break;
        } else if ("?".equals(cmd) || "help".equals(cmd)) {
          showHelp();
        } else if ("show tables".equals(cmd)) {
          showTables();
        } else if (cmd.startsWith("describe ")) {
          describe(cmd.substring(9).trim());
        } else if (cmd.startsWith("import ")) {
          importFile(cmd.substring(7));
        } else if (cmd.startsWith("drop table ")) {
          dropTable(cmd.substring(11));
        } else {
          System.out.println("Unrecognized command or missing parameter: " + cmd);
          showPrompt();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    System.out.println("Good bye!");
  }

  private static void dropTable(String tableId) {
    service.dropTable(tableId, new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        System.out.println("Table dropped");
        showPrompt();
      }

      @Override
      public void onFailure(Throwable error) {
        showError(error);
      }
      
    });
  }

  private static void importFile(String fileName) throws IOException {
    Reader reader = new InputStreamReader(new FileInputStream(fileName), "utf-8");
    char[] buf = new char[32768];
    StringBuilder sb = new StringBuilder();
    while (true) {
      int count = reader.read(buf);
      if (count <= 0) {
        break;
      }
      sb.append(buf, 0, count);
    }
    String data = sb.toString();
    
    Iterator<Map<String,String>> parser;
    
    if (fileName.endsWith(".bib")) {
      parser = new BibtexParser(data);
    } else {
      parser = new CsvParser(data, true);
    }
    
    String name = fileName;
    int cut = name.lastIndexOf('/');
    if (cut != -1) {
      name = name.substring(cut + 1);
    }
    cut = name.lastIndexOf('.');
    if (cut != -1) {
      name = name.substring(0, cut);
    }
    
    HashSet<String> fields = new HashSet<String>();
    final ArrayList<Map<String,String>> entries = new ArrayList<Map<String,String>>();
    while (parser.hasNext()) {
      Map<String,String> entry = parser.next();
      fields.addAll(entry.keySet());
      entries.add(entry);
    }
    
    ArrayList<ColumnInfo> columns = new ArrayList<ColumnInfo>();
    for (String field: fields) {
      columns.add(new ColumnInfo(field, ColumnType.STRING));
    }
    
    service.createTable(name, columns, new AsyncCallback<String>() {
      public void onSuccess(String tableId) {
        ArrayList<Entity> entities = new ArrayList<Entity>();
        for (Map<String,String> map: entries) {
          Entity entity = new Entity(tableId);
          for (Map.Entry<String, String> e : map.entrySet()) {
            entity.setProperty(e.getKey(), e.getValue());
          }
          entities.add(entity);
        }
        service.put(entities, new AsyncCallback<List<Key>>() {

          @Override
          public void onSuccess(List<Key> result) {
            System.out.println("" + result.size() + " entities updated / inserted.");
            showPrompt();
          }

          @Override
          public void onFailure(Throwable error) {
            showError(error);
          }
        });
      }
      public void onFailure(Throwable error) {
        showError(error);
      }});
  }

  private static void showHelp() {
    System.out.println();
    System.out.println("describe <table id>:   Show table structure");
    System.out.println("drop table <table id>: Drop (delete) the table");
    System.out.println("help:                  Show this help screen");
    System.out.println("exit:                  Quit FT demo");
    System.out.println("show tables:           List available tables");
    showPrompt();
  }

  private static void describe(String tableId) {
    service.describe(tableId, new AsyncCallback<TableDescription>() {
      public void onFailure(Throwable caught) {
        showError(caught);
      }

      @Override
      public void onSuccess(TableDescription result) {
        System.out.println("Columns:");
        for (ColumnInfo ci : result) {
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
        for (TableInfo td : result) {
          System.out.println(td);
        }
        showPrompt();
      }
    });
  }

  private static void showPrompt() {
    System.out.print("\nCommand? ");
    System.out.flush();
  }

  private static void showError(Throwable caught) {
    caught.printStackTrace();
    showPrompt();
  }
}
