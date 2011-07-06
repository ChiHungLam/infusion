package com.google.code.infusion.gwt.client.file;

import com.google.gwt.dom.client.Element;

public class FileUtil {

  private FileUtil() {
    
  }
  
  public static native File getFile(Element inputElement, int index) /*-{
    return input.files[index];
  }-*/;

  
}
