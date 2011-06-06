package com.google.code.infusion.service;


import com.google.code.infusion.json.JsonObject;
import com.google.code.infusion.util.ChainedCallback;

import com.google.code.infusion.util.HttpRequest;
import com.google.code.infusion.util.HttpResponse;
import com.google.code.infusion.util.JsonpRequestBuilder;
import com.google.code.infusion.util.OAuth;
import com.google.code.infusion.util.Util;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FusionTableService {

  private static String BASE_URL = "https://www.google.com/fusiontables/api/query";
    
  public static String SCOPE = BASE_URL;
  
  private OAuth.Token token;

  public FusionTableService() {
  }

  public void setRequestToken(OAuth.Token token) {
    this.token = token;
  }
  

  /**
   * Sends the given SQL command.
   * Currently delegates to getQuery/postQuery, depending on the command
   * because FusionTables does not seem to support GET requests 
   * everywhere (which is required for proper Jsonp support to get rid
   * of the proxy.
   */
  public void sendQuery(String sql, final AsyncCallback<Table> callback) {
    String lSql = (sql.length() > 10 ? sql.substring(0, 10) : sql).toLowerCase();
    if (lSql.startsWith("select") || lSql.startsWith("show") || lSql.startsWith("describe")) {
      getQuery(sql, callback);
    } else {
      postQuery(sql, callback);
    }
  }
  
  

  private void getQuery(String sql, final AsyncCallback<Table> callback) {
    String url = BASE_URL + "?jsonCallback=callback&sql=" + Util.urlEncode(sql);
    if (token != null) {
      url = OAuth.signUrl(url, null, token); 
    }
    JsonpRequestBuilder builder = new JsonpRequestBuilder();
    builder.requestObject(url, new ChainedCallback<JsonObject>(callback) {
      public void onSuccess(JsonObject response) {
        System.out.println("SQL result: " + response);
        callback.onSuccess(new Table(response.getObject("table")));
      }
    });
  }

  
  private void postQuery(String sql, final AsyncCallback<Table> callback) {
    String url = BASE_URL + "?jsonCallback=callback";
    String data = "sql=" + Util.urlEncode(sql);
    
    if (token != null) {
      url = OAuth.signUrl(url, data, token);
    }
    
    HttpRequest request = new HttpRequest(HttpRequest.POST, url);
    request.setData(data);
    request.send(new ChainedCallback<HttpResponse>(callback) {
      public void onSuccess(HttpResponse response) {
        String data = response.getData();
        if (data.startsWith("callback")) {
          int start = data.indexOf('(');
          int end = data.lastIndexOf(')');
          JsonObject jso = JsonObject.parse(data.substring(start + 1, end));
          callback.onSuccess(new Table(jso.getObject("table")));
        } else if (data.trim().startsWith("<")) {
          callback.onSuccess(new Table(JsonObject.parse(
              "{'table':{'cols':['result'],'rows':[['" + 
              Util.quote(data, '"', true) +"']]}")));
        } else {
          // TODO: Extract nicer error from <TITLE> if present
          callback.onFailure(new RuntimeException(data));
        }
      }
    });
  }

}
