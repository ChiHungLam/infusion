package com.google.code.infusion.gwt.client.file;

public final class File extends Blob {

  protected File() {
  }


  public native String getName() /*-{
    return this.name;
  }-*/;

}