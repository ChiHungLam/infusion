package com.google.code.infusion.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.XMLHttpRequest;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;

public class HttpResponse {
  XMLHttpRequest xhr;

  public static String signUrl(String method, String url, String body, OAuthToken token) {
    throw new UnsupportedOperationException();
  }
    

  HttpResponse(final HttpRequestBuilder request, final AsyncCallback<HttpResponse> callback) {
    xhr = XMLHttpRequest.create();
    xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
      public void onReadyStateChange(XMLHttpRequest xhr) {
        if (xhr.getReadyState() == XMLHttpRequest.DONE) {
          callback.onSuccess(HttpResponse.this);
          xhr.clearOnReadyStateChange();
        }
      }
    });

    xhr.open(request.method, GWT.getModuleBaseURL() + "proxy");
    for (String[] header: request.headers) {
      xhr.setRequestHeader(header[0], header[1]);
    }
    xhr.setRequestHeader("X-Forward-To", request.url);
    if (request.token != null) {
      xhr.setRequestHeader("X-OAuth-Token", request.token.toString());
    }
    xhr.send(request.data);
  }

  public String getData() {
    return xhr.getResponseText();
  }

  public String getStatusText() {
    return xhr.getStatusText();
  }

  public int getStatusCode() {
    return xhr.getStatus();
  }
}
