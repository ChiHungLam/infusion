package com.google.code.infusion.importer;

public interface ImporterCallback {
  public void onProgress(Importer importer);
  public void onSuccess(Importer importer);
  public void onFailure(Importer importer, Throwable error);

  
}
