package com.google.code.infusion.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.code.infusion.util.AsyncCallback;
import com.google.code.infusion.util.ChainedCallback;
import com.google.code.infusion.util.HttpRequest;
import com.google.code.infusion.util.HttpResponse;
import com.google.code.infusion.util.Util;

public class FusionTableService {

  private String authToken;
  private HashMap<String, List<ColumnInfo>> tables = new HashMap<String, List<ColumnInfo>>();

  public FusionTableService(String authToken) {
    this.authToken = authToken;
  }


  public void createTable(String name, List<ColumnInfo> columns,
      final AsyncCallback<String> callback) {
    String query = "CREATE TABLE " + Util.singleQuote(name) + " ("
        + toString(columns) + ")";
    postSql(query, new ChainedCallback<String[]>(callback) {

      @Override
      public void onSuccess(String[] result) {
        callback.onSuccess(result[1]);
      }
    });

  }

  private void describeAsMap(String tableId, final AsyncCallback<Map<String,ColumnType<?>>> callback) {
    describe(tableId, new ChainedCallback<List<ColumnInfo>>(callback) {
      @Override
      public void onSuccess(List<ColumnInfo> result) {
        Map<String,ColumnType<?>> map = new HashMap<String,ColumnType<?>>();
        for (ColumnInfo c: result) {
          map.put(c.getName(), c.getType());
        }
        callback.onSuccess(map);
      }
    });
  }
  
  
  private void postInsertStatement(final List<Entity> insert, final AsyncCallback<String[]> callback) {
    describeAsMap(insert.get(0).getKey().getKind(), new ChainedCallback<Map<String,ColumnType<?>>>(callback) {
      @Override
      public void onSuccess(Map<String, ColumnType<?>> columns) {
        StringBuilder sb = new StringBuilder();
        for (Entity entity : insert) {
          sb.append("INSERT INTO ");
          sb.append(Util.doubleQuote(entity.getKey().getKind()));
          sb.append(" (");
          StringBuilder values = new StringBuilder();
          for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
            if (values.length() != 0) {
              sb.append(", ");
              values.append(", ");
            }
            sb.append(Util.singleQuote(entry.getKey()));
            ColumnType type = columns.get(entry.getKey());
            if (type == null) {
              callback.onFailure(new RuntimeException("Unknown field '" + entry.getKey() + "'"));
              return;
            }
            values.append(Util.singleQuote(type.toString(entry.getValue())));
          }
          sb.append(") VALUES (");
          sb.append(values.toString());
          sb.append(")");
          if (insert.size() > 1) {
            sb.append(";\n");
          }
        }
        postSql(sb.toString(), callback); 
      }
    });
  }


  private void postUpdateStatement(final Entity entity, final AsyncCallback<String[]> callback) {
    describeAsMap(entity.getKey().getKind(), new ChainedCallback<Map<String,ColumnType<?>>>(callback) {
      @Override
      public void onSuccess(Map<String, ColumnType<?>> columns) {
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(entity.getKey().getKind());
        sb.append(" SET ");
        boolean first = true;
        for(Map.Entry<String,Object> entry: entity.getProperties().entrySet()) {
          if (first) {
            first = false;
          } else {
            sb.append(", ");
          } 
          sb.append(Util.singleQuote(entry.getKey()));
          sb.append(" = ");
          ColumnType type = columns.get(entry.getKey());
          if (type == null) {
            callback.onFailure(new RuntimeException("Unknown field '" + entry.getKey() + "'"));
            return;
          }
          sb.append(Util.singleQuote(type.toString(entry.getValue())));
        }
        sb.append(" WHERE ROWID = " + Util.singleQuote(entity.getKey().getName()));
        postSql(sb.toString(), callback);
      }
    });
  }

  
  public void put(final Iterable<Entity> entities,
      final AsyncCallback<List<Key>> callback) {
    List<Entity> updates = new ArrayList<Entity>();
    HashMap<String, List<Entity>> insertMap = new HashMap<String, List<Entity>>();
    List<List<Entity>> insertList = new ArrayList<List<Entity>>();
    final PutStatus putStatus = new PutStatus();

    for (Entity entity : entities) {
      Key key = entity.getKey();
      putStatus.pending++;
      if (key.isComplete()) {
        updates.add(entity);
      } else {
        String tableId = key.kind;
        List<Entity> insert = insertMap.get(tableId);
        if (insert == null || insert.size() >= 333) {
          insert = new ArrayList<Entity>();
          insertMap.put(tableId, insert);
          insertList.add(insert);
        }
        insert.add(entity);
      }
    }

    for (final Entity entity : updates) {
      if (putStatus.error) {
        break;
      }
      postUpdateStatement(entity, new AsyncCallback<String[]>() {
        @Override
        public void onSuccess(String[] result) {
          putStatus.pending--;
          if (putStatus.pending == 0 && !putStatus.error) {
            callback.onSuccess(buildKeyList(entities));
          }
        }
        @Override
        public void onFailure(Throwable error) {
          if (!putStatus.error) {
            putStatus.error = true;
            callback.onFailure(error);
          }
        }
      });
    }
    
    for (final List<Entity> insert : insertList) {
      if (putStatus.error) {
        break;
      }
      postInsertStatement(insert, new AsyncCallback<String[]>() {
        @Override
        public void onSuccess(String[] result) {
          for (int i = 0; i < insert.size(); i++) {
            Key key = insert.get(i).getKey();
            key.name = result[i + 1];
          }
          putStatus.pending -= insert.size();
          if (putStatus.pending == 0 && !putStatus.error) {
            callback.onSuccess(buildKeyList(entities));
          }
        }
        @Override
        public void onFailure(Throwable error) {
          if (!putStatus.error) {
            putStatus.error = true;
            callback.onFailure(error);
          }
        }
      });
    }
  }

  public void showTables(final AsyncCallback<List<TableInfo>> callback) {
    getSql("SHOW TABLES", new ChainedCallback<String[]>(callback) {

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

    getSql("DESCRIBE " + Util.doubleQuote(tableId), new ChainedCallback<String[]>(callback) {
      public void onSuccess(String[] rows) {
        List<ColumnInfo> table = new ArrayList<ColumnInfo>();
        for (int i = 1; i < rows.length; i++) {
          String[] parts = Util.parseCsv(rows[i]);
          ColumnType<?> type;
          String typeName = parts[2].toLowerCase();
          if ("string".equals(typeName)) {
            type = ColumnType.STRING;
          } else if ("datetime".equals(typeName)) {
            type = ColumnType.DATETIME;
          } else if ("number".equals(typeName)) {
            type = ColumnType.NUMBER;
          } else if ("location".equals(typeName)) {
            type = ColumnType.LOCATION;
          } else {
            onFailure(new RuntimeException("Unrecognized column type: " + typeName));
            break;
          }
          table.add(new ColumnInfo(parts[1], type));
        }
        tables.put(tableId, table);
        callback.onSuccess(table);
      }
    });
  }

  private void getSql(String command, final AsyncCallback<String[]> callback) {
    HttpRequest request = new HttpRequest(HttpRequest.GET,
        "https://www.google.com/fusiontables/api/query?sql="
            + Util.urlEncode(command));
    request.setHeader("Authorization", "GoogleLogin auth=" + authToken);
    request.send(new ChainedCallback<HttpResponse>(callback) {
      public void onSuccess(HttpResponse response) {
        System.out.println("SQL result: " + response.getData());
        callback.onSuccess(response.getData().split("\n"));
      }
    });
  }

  private void postSql(String command, final AsyncCallback<String[]> callback) {
    HttpRequest request = new HttpRequest(HttpRequest.POST,
        "https://www.google.com/fusiontables/api/query");
    request.setHeader("Authorization", "GoogleLogin auth=" + authToken);
    
    System.out.println("Command:");
    System.out.println(command);
    
    request.setData("sql=" + Util.urlEncode(command));
    request.send(new ChainedCallback<HttpResponse>(callback) {
      public void onSuccess(HttpResponse response) {
        // System.out.println("SQL result: " + response.getData());
        callback.onSuccess(response.getData().split("\n"));
      }
    });
  }

  public PreparedQueryAsync prepareQuery(Query query) {
    return new FusionTableQuery(query);
  }

  static class PutStatus {
    int pending;
    boolean error;
  }

  private static String toString(List<ColumnInfo> columns) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < columns.size(); i++) {
      if (i != 0) {
        sb.append(", ");
      }
      ColumnInfo col = columns.get(i);
      sb.append(Util.singleQuote(col.getName()));
      sb.append(": ");
      sb.append(col.getType().getName());
    }
    return sb.toString();
  }
  
    
  
  private static List<Key> buildKeyList(Iterable<Entity> entities) {
    ArrayList<Key> keys = new ArrayList<Key>();
    for (Entity entity : entities) {
      keys.add(entity.getKey());
    }
    return keys;
  }

  
  class FusionTableQuery implements PreparedQueryAsync {
    Query query;

    FusionTableQuery(Query query) {
      this.query = query;
    }

    public void asList(final FetchOptions fetchOptions, final AsyncCallback<List<Entity>> callback) {
      describe(query.getKind(), new ChainedCallback<List<ColumnInfo>>(callback) {
        @Override
        public void onSuccess(List<ColumnInfo> result) {
          asList(fetchOptions, result, callback);
        }
      });
    }
    
    
    public void asList(FetchOptions fetchOptions, final List<ColumnInfo> columns, final AsyncCallback<List<Entity>> callback) {
      StringBuilder sb = new StringBuilder("SELECT rowid");
      for (int i = 0; i < columns.size(); i++) {
        sb.append(',');
        sb.append(Util.singleQuote(columns.get(i).getName()));
      }
      sb.append(' ');
      sb.append(query.toString());
      if (fetchOptions.offset > 0) {
        sb.append(" OFFSET ");
        sb.append(fetchOptions.offset);
      }
      if (fetchOptions.limit < Integer.MAX_VALUE) {
        sb.append(" LIMIT ");
        sb.append(fetchOptions.limit);
      }
      
      getSql(sb.toString(), new ChainedCallback<String[]>(callback) {
          public void onSuccess(String[] result) {
              ArrayList<Entity> entities = new ArrayList<Entity>();
              for (int i = 1; i < result.length; i++) {
                String[] parts = Util.parseCsv(result[i]);
                Key key = new Key();
                key.kind = query.getKind();
                key.name = parts[0];
                Entity entity = new Entity(key);
                for (int j = 0; j < parts.length; j++) {
                  entity.setProperty(columns.get(j).getName(), 
                      columns.get(j).getType().parse(parts[j]));
                }
                entities.add(entity);
              }
              callback.onSuccess(entities);
            }
          });
    }
  }

  public void dropTable(String tableId, final AsyncCallback<Void> callback) {
    postSql("DROP TABLE " + Util.doubleQuote(tableId), new ChainedCallback<String[]>(callback) {

      @Override
      public void onSuccess(String[] result) {
        callback.onSuccess(null);
      }
      
    });
  }
}
