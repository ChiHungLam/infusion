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

  private OAuth.Token token;

  public FusionTableService() {
  }

  public void setRequestToken(OAuth.Token token) {
    this.token = token;
  }
  


  public void getQuery(String sql, final AsyncCallback<Table> callback) {
    String url = "https://www.google.com/fusiontables/api/query?jsonCallback=callback&sql=" + Util.urlEncode(sql);
    if (token != null) {
      url = OAuth.signUrl(url, null, token); //authToken);
//      request.setHeader("Authorization", "GoogleLogin auth=" + authToken);
    }
    JsonpRequestBuilder builder = new JsonpRequestBuilder();
    builder.requestObject(url, new ChainedCallback<JsonObject>(callback) {
      public void onSuccess(JsonObject response) {
        System.out.println("SQL result: " + response);
        callback.onSuccess(new Table(response.getObject("table")));
      }
    });
  }

  public void postQuery(String sql, final AsyncCallback<String> callback) {
    String url = "https://www.google.com/fusiontables/api/query";
    String data = "sql=" + Util.urlEncode(sql);
    
    if (token != null) {
      url = OAuth.signUrl(url, data, token);
    }
    
    HttpRequest request = new HttpRequest(HttpRequest.POST,
      url);
    request.setData(data);
    request.send(new ChainedCallback<HttpResponse>(callback) {
      public void onSuccess(HttpResponse response) {
    // System.out.println("SQL result: " + response.getData());
        callback.onSuccess(response.getData());
      }
    });
  }
  
}
