package com.google.code.infusion.service;


import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.json.JsonObject;
import com.google.code.infusion.util.ChainedCallback;

import com.google.code.infusion.util.HttpRequestBuilder;
import com.google.code.infusion.util.HttpResponse;
import com.google.code.infusion.util.OAuth;
import com.google.code.infusion.util.OAuthToken;
import com.google.code.infusion.util.Util;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FusionTableService {

  private static String BASE_URL = "https://www.google.com/fusiontables/api/query";
    
  public static String SCOPE = BASE_URL;
  
  private OAuthToken token;

  public FusionTableService() {
  }

  public void setRequestToken(OAuthToken token) {
    this.token = token;
  }

  public void insert(String tableId, Table data, AsyncCallback<Table> callback) {
    insert(tableId, data, 0, callback);
  }
  
  private void insert(final String tableId, final Table data, int offset, final AsyncCallback<Table> callback) {
    StringBuilder sb = new StringBuilder();
    final int end = offset + Math.min(100, data.getRows().length() - offset);
    for (int r = offset; r < end; r++) {
      JsonArray row = data.getRows().getArray(r);
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
      if (data.getRows().length() - offset > 1) {
        sb.append(';');
      }
    }
//    System.out.println("Sending statement: " + sb);
    query(sb.toString(), new ChainedCallback<Table>(callback) {
      @Override
      public void onSuccess(Table result) {
        if (end < data.getRows().length()) {
          insert(tableId, data, end, callback);
        } else {
          callback.onSuccess(result);
        }
      }
    });
  }
  

  /*
  private void getQuery(String sql, final AsyncCallback<Table> callback) {
    String url = BASE_URL + "?jsonCallback=callback&sql=" + Util.urlEncode(sql);
    if (token != null) {
      url = OAuth.signUrl(url, null, token); 
    }
    JsonpRequestBuilder builder = new JsonpRequestBuilder();
    builder.requestObject(url, new ChainedCallback<JsonObject>(callback) {
      public void onSuccess(JsonObject response) {
       // System.out.println("SQL result: " + response);
        callback.onSuccess(new Table(response.getObject("table")));
      }
    });
  }
*/ 
  
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
    
    if (token != null) {
      url = OAuth.signUrl(method, url, data, token);
    }
    
    HttpRequestBuilder request = new HttpRequestBuilder(method, url);
    request.setData(data);
    request.send(new ChainedCallback<HttpResponse>(callback) {
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
          int start = data.indexOf("<TITLE>");
          int end = data.indexOf("</TITLE>");
          if (start != -1 && end != -1) {
            data = data.substring(start + 7, end);
          }
          callback.onFailure(new RuntimeException(data));
        }
      }
    });
  }

}
