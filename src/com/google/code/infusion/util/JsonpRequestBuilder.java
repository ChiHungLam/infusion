package com.google.code.infusion.util;

import com.google.code.infusion.json.JsonObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class JsonpRequestBuilder {

  
  public void requestObject(java.lang.String url,
      final AsyncCallback<JsonObject> callback) {
    
    HttpRequest httpRequest = new HttpRequest(HttpRequest.GET, url);
    httpRequest.send(new ChainedCallback<HttpResponse>(callback) {
      
      @Override
      public void onSuccess(HttpResponse result) {
        String data = result.getData();
        int start = data.indexOf('(') + 1;
        int end = data.lastIndexOf(')');
        callback.onSuccess(JsonObject.parse(data.substring(start, end)));
      }
    });
    
  }

  
}
