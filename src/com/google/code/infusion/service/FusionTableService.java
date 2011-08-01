package com.google.code.infusion.service;

import java.util.TimerTask;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.json.JsonObject;

import com.google.code.infusion.util.ChainedCallback;
import com.google.code.infusion.util.HttpRequestBuilder;
import com.google.code.infusion.util.HttpResponse;
import com.google.code.infusion.util.OAuthToken;
import com.google.code.infusion.util.Timer;
import com.google.code.infusion.util.Util;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FusionTableService {

  private static String BASE_URL = "https://www.google.com/fusiontables/api/query";
  public static String SCOPE = BASE_URL;
  private static final int REQUEST_TIME_DISTANCE = 500;
  private static final int MAX_PARALLEL_REQUESTS = 2;
  
  private OAuthToken token;
  private Timer timer = new Timer();
  private long lastRequest;
  private long lastScheduledRequest;
  private int pendingRequests;
  
  public void setAccessToken(OAuthToken token) {
    this.token = token;
  }

  /**
   * Insert the data from the given table into the table with the given
   * table id.
   * 
   * @param tableId to insert into 
   * @param data data to insert
   * @param callback will be called when the data is inserted.
   */
  public void insert(final String tableId, final Table data, final AsyncCallback<Table> callback) {
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for(JsonArray row: data) {
      if (count == 1) {
        sb.append(";\n");
      }
      sb.append("INSERT INTO ");
      sb.append(tableId);
      sb.append('(');
      StringBuilder values = new StringBuilder();
      for (int i = 0; i < Math.min(data.getColCount(), row.length()); i++) {
        String value = row.getAsString(i);
        if (value == null) {
          value = "";
        }
        if (i == 0 || !value.equals("")) {
          if (i > 0) {
            sb.append(", ");
            values.append(", ");
          } 
          sb.append(Util.singleQuote(data.getCol(i)));
          values.append(Util.quote(value, '\'', true));
        }
      }
      sb.append(") VALUES (");
      sb.append(values.toString());
      sb.append(')');
      if (count > 0) {
        sb.append(";\n");
      }
      count++;
    }
    query(sb.toString(), callback);
  }

  /**
   * Extract the error message from the title element of the given
   * HTML fragment. If not found, the whole string is returned.
   */
  private static String extractErrorMessage(String error) {
    int start = error.indexOf("<TITLE>");
    int end = error.indexOf("</TITLE>");
    if (start != -1 && end != -1) {
      error = Util.xmlDecode(error.substring(start + 7, end));
    } else {
      start = error.indexOf("<title>");
      end = error.indexOf("</title>");
      if (start != -1 && end != -1) {
        error = Util.xmlDecode(error.substring(start + 7, end));
      }
    }
    return error + " \n(Note: errors are often caused by missing authentication.)";
  }
  

  /**
   * Sends the given SQL command.
   * TODO(Use a queue instead, FIFO write, LIFO read(?))
   */
  public void query(final String sql, final AsyncCallback<Table> callback) {
    boolean tooMany = pendingRequests > MAX_PARALLEL_REQUESTS;
    long now = System.currentTimeMillis();
    if (now - lastRequest < REQUEST_TIME_DISTANCE || tooMany) {
      lastScheduledRequest = Math.max(now  + (tooMany ? REQUEST_TIME_DISTANCE : 1), 
          lastScheduledRequest + REQUEST_TIME_DISTANCE);
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          query(sql, callback);
        }}, lastScheduledRequest - now);
      return;
    }
    lastRequest = Math.max(lastRequest, System.currentTimeMillis());
    pendingRequests++;
    
    String lSql = (sql.length() > 10 ? sql.substring(0, 10) : sql).toLowerCase();
    String url = BASE_URL + "?jsonCallback=callback";
    String data = "sql=" + Util.urlEncode(sql);
    String method;
    if (lSql.startsWith("select") || lSql.startsWith("show") || lSql.startsWith("describe")) {
      method = HttpRequestBuilder.GET;
      url = url + "&" + data;
      data = null;
    } else {
      method = HttpRequestBuilder.POST;
    }
    
    HttpRequestBuilder request = new HttpRequestBuilder(method, url);
    request.setOAuthToken(token);
    request.setData(data);
    request.send(new AsyncCallback<HttpResponse>() {
      public void onSuccess(HttpResponse response) {
        pendingRequests--;
        String data = response.getData();
        if (data.startsWith("callback")) {
          int start = data.indexOf('(');
          int end = data.lastIndexOf(')');
          JsonObject jso = JsonObject.parse(data.substring(start + 1, end));
          callback.onSuccess(new Table(jso.getObject("table")));
        } else if (!data.trim().startsWith("<")) {
          callback.onSuccess(new Table(JsonObject.parse(
              "{'cols':['result'],'rows':[[" + 
              Util.quote(data.trim(), '"', true) +"]]}")));
        } else {
          callback.onFailure(new RuntimeException(extractErrorMessage(data) + " Query: " + sql));
        }
      }

      @Override
      public void onFailure(Throwable error) {
        pendingRequests++;
        callback.onFailure(new RuntimeException(extractErrorMessage(error.toString()) + " Query: " + sql));
      }
    });
  }
  
  public PreparedQuery prepare(Query query) {
    return new PreparedQuery(this, query.toString());
  }

  public void describe(String tableId, AsyncCallback<Table> callback) {
    query("DESCRIBE " + Util.singleQuote(tableId), callback);
  }
  
  
  
  public void update(String tableId, String rowId, Table data, final AsyncCallback<Void> callback) {
    if (data.getRowCount() != 1) {
      throw new IllegalArgumentException("Table must have exactly one row for update.");
    }
    StringBuilder sb = new StringBuilder("UPDATE ");
    sb.append(Util.singleQuote(tableId)).append(" SET ");
    JsonArray row = data.iterator().next();
    for (int i = 0; i < data.getRowCount(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(Util.singleQuote(data.getCol(i)));
      sb.append("=");
      sb.append(Util.quote(row.getAsString(i), '\'', true));
    }
    sb.append(" WHERE ROWID=");
    sb.append(Util.quote(rowId, '\'', true));
    query(sb.toString(), new ChainedCallback<Table>(callback) {

      @Override
      public void onSuccess(Table result) {
        callback.onSuccess(null);
      }
    });
  }

  public void createTable(String name, Table table, final AsyncCallback<String> callback) {
    StringBuilder sb = new StringBuilder("CREATE TABLE ");
    sb.append(Util.singleQuote(name));
    sb.append("(");
    boolean first = true;
    for (JsonArray row: table) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }
      sb.append(Util.quote(row.getString(0), '\'', true));
      sb.append(':');
      sb.append(row.getString(1));
    }
    sb.append(')');
    query(sb.toString(), new ChainedCallback<Table>(callback) {

      @Override
      public void onSuccess(Table result) {
        callback.onSuccess(result.iterator().next().getAsString(0));
      }
    });
    
  }
}
