package com.google.code.infusion.gwt.client.file;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class FileReader extends JavaScriptObject {

  protected FileReader() {
  }
  
  static native FileReader create(AsyncCallback<?> callback) /*-{
    var reader = new FileReader();
    
    reader.onload = function(evt) {
      callback.@com.google.gwt.user.client.rpc.AsyncCallback::onSuccess(Ljava/lang/Object;)(reader.result);
    }

    var ff = function(evt) { // TODO: Hand over the error object, too
      @com.google.code.infusion.gwt.client.file.FileReader::onFailure(Lcom/google/gwt/user/client/rpc/AsyncCallback;)(callback);
    }
    reader.onerror = ff;
    reader.onabort = ff;
    
    return reader;
  }-*/;
  
  
  private static void onFailure(AsyncCallback<?> callback) {
    callback.onFailure(new RuntimeException("File Error"));
  }
  

  
  native void readAsText(Blob blob, String encoding) /*-{
    this.readAsText(blob, encoding);
  }-*/;

}
