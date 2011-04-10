package com.google.code.infusion.demo.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;

public class FileReader extends JavaScriptObject {
  
  public static final int EMPTY = 0;
  public static final int LOADING = 1;
  public static final int DONE = 2;
  
  public native final static FileReader create() /*-{
    return new FileReader();
  }-*/;
  
  public native final int getReadyState() /*-{
    return this.readyState;
  }-*/;
  
  public native final String getResult() /*-{
    return this.result;
  }-*/;
  
  
  public native final void readAsText(Blob blob) /*-{
    this.readAsText(blob);
  }-*/;
}
