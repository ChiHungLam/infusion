package com.google.code.infusion.service;


import java.util.Iterator;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.json.JsonObject;
import com.google.code.infusion.util.ChainedCallback;

import com.google.code.infusion.util.HttpRequestBuilder;
import com.google.code.infusion.util.HttpResponse;
import com.google.code.infusion.util.OAuthToken;
import com.google.code.infusion.util.Util;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FusionTableService {

  private static String BASE_URL = "https://www.google.com/fusiontables/api/query";
  public static String SCOPE = BASE_URL;
  
  private OAuthToken token;

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
  public void insert(String tableId, Table data, boolean returnIds, AsyncCallback<Table> callback) {
    insert(tableId, data.getCols(), data.iterator(), null, returnIds, callback);
  }
  
  private void insert(final String tableId, final JsonArray cols, final Iterator<JsonArray> rows, 
      final Table result, final boolean returnIds, final AsyncCallback<Table> callback) {
    StringBuilder sb = new StringBuilder();
    int count = 0;
    while(rows.hasNext() && count < 256) {
      if (count == 1) {
        sb.append(';');
      }
      JsonArray row = rows.next();
      sb.append("INSERT INTO ");
      sb.append(tableId);
      sb.append('(');
      StringBuilder values = new StringBuilder();
      for (int i = 0; i < row.length(); i++) {
        String value = row.getString(i);
        if (value == null) {
          value = "";
        }
        if (i == 0 || !value.equals("")) {
          if (i > 0) {
            sb.append(", ");
            values.append(", ");
          } 
          sb.append(Util.singleQuote(cols.getString(i)));
          values.append(Util.quote(value, '\'', true));
        }
      }
      sb.append(") VALUES (");
      sb.append(values);
      sb.append(')');
      if (count > 0) {
        sb.append(';');
      }
      count++;
    }
//    System.out.println("Sending statement: " + sb);
    query(sb.toString(), new ChainedCallback<Table>(callback) {
      @Override
      public void onSuccess(Table newResult) {
        if (returnIds) {
          if (result != null) {
            for (JsonArray row: newResult) {
              result.addRow(row);
            }
            newResult = result;
          }
        } else {
          int count = result == null ? 0 : result.getRowCount();
          count += newResult.getRowCount();
          JsonArray cols = JsonArray.create();
          cols.setString(0, "count");
          JsonArray rows = JsonArray.create();
          JsonArray row0 = JsonArray.create();
          rows.setArray(0, row0);
          row0.setNumber(0, count);
          newResult = new Table(cols, rows);
        }
        if (rows.hasNext()) {
          insert(tableId, cols, rows, newResult, returnIds, callback);
        } else {
          callback.onSuccess(result);
        }
      }
    });
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
    }
    return error;
  }
  

  /**
   * Sends the given SQL command.
   */
  public void query(String sql, final AsyncCallback<Table> callback) {
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
          callback.onFailure(new RuntimeException(extractErrorMessage(data)));
        }
      }

      @Override
      public void onFailure(Throwable error) {
        callback.onFailure(new RuntimeException(extractErrorMessage(error.toString())));
      }
    });
  }

}
