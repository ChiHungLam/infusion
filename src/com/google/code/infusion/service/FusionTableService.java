package com.google.code.infusion.service;


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
  public void insert(String tableId, SimpleTable data, AsyncCallback<SimpleTable> callback) {
    insert(tableId, data, 0, null, callback);
  }
  
  private void insert(final String tableId, final SimpleTable data, int offset, 
      final SimpleTable result, final AsyncCallback<SimpleTable> callback) {
    StringBuilder sb = new StringBuilder();
    final int end = offset + Math.min(100, data.getRowArray().length() - offset);
    for (int r = offset; r < end; r++) {
      JsonArray row = data.getRowArray().getArray(r);
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
          sb.append(Util.singleQuote(data.getCols().getString(i)));
          values.append(Util.quote(value, '\'', true));
        }
      }
      sb.append(") VALUES (");
      sb.append(values);
      sb.append(')');
      if (data.getRowArray().length() - offset > 1) {
        sb.append(';');
      }
    }
//    System.out.println("Sending statement: " + sb);
    query(sb.toString(), new ChainedCallback<SimpleTable>(callback) {
      @Override
      public void onSuccess(SimpleTable newResult) {
        if (result != null) {
          int i = result.getRowArray().length();
          for (JsonArray row: newResult.getRows()) {
            result.getRowArray().setArray(i++, row);
          }
          newResult = result;
        }
        if (end < data.getRowArray().length()) {
          insert(tableId, data, end, newResult, callback);
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
  public void query(String sql, final AsyncCallback<SimpleTable> callback) {
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
          callback.onSuccess(new SimpleTable(jso.getObject("table")));
        } else if (!data.trim().startsWith("<")) {
          callback.onSuccess(new SimpleTable(JsonObject.parse(
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
