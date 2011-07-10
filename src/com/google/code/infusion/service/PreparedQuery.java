package com.google.code.infusion.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class PreparedQuery {

  private final FusionTableService service;
  private final String query;
  
  PreparedQuery(FusionTableService fusionTableService, String query) {
    this.service = fusionTableService;
    this.query = query;
  }

  public void asTable(FetchOptions fetchOptions, AsyncCallback<Table> callback) {
    StringBuilder sb = new StringBuilder(query);
    if (fetchOptions.getOffset() != 0) {
      sb.append(" OFFSET ");
      sb.append(fetchOptions.offset);
    }
    if (fetchOptions.getLimit() != Integer.MAX_VALUE) {
      sb.append(" LIMIT ");
      sb.append(fetchOptions.getLimit());
    }
    service.query(sb.toString(), callback);
  }
  
  public void asTable(AsyncCallback<Table> callback) {
    service.query(query, callback);
  }
  
}
