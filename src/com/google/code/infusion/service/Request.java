package com.google.code.infusion.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class Request {
  FusionTableService service;
  String query;
  AsyncCallback<Table> callback;
  
  
  public Request(FusionTableService service, String sql,
      AsyncCallback<Table> callback) {
    this.service = service;
    this.query = sql;
    this.callback = callback;
  }

  void execute() {
    service.queryImpl(query, callback);
  }
  
  public void cancel() {
    service.requestQueue.remove(this);
  }
}
