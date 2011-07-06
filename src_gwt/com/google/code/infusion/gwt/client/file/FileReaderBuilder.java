package com.google.code.infusion.gwt.client.file;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileReaderBuilder {
  
  public FileReader readAsText(Blob file, String encoding, AsyncCallback<String> callback) {
    FileReader reader = FileReader.create(callback);
    reader.readAsText(file, encoding);
    return reader;
  }

}
