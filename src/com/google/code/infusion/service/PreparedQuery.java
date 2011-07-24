package com.google.code.infusion.service;

import java.util.List;

import com.google.code.infusion.util.ChainedCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class PreparedQuery {

  private final FusionTableService service;
  private final String query;
  
  PreparedQuery(FusionTableService fusionTableService, String query) {
    this.service = fusionTableService;
    this.query = query;
  }

  private String withFetchOptions(FetchOptions fetchOptions) {
    StringBuilder sb = new StringBuilder(query);
    if (fetchOptions.getOffset() != 0) {
      sb.append(" OFFSET ");
      sb.append(fetchOptions.offset);
    }
    if (fetchOptions.getLimit() != Integer.MAX_VALUE) {
      sb.append(" LIMIT ");
      sb.append(fetchOptions.getLimit());
    }
    return sb.toString();
  }
  
  public void asTable(FetchOptions fetchOptions, AsyncCallback<Table> callback) {
    service.query(withFetchOptions(fetchOptions), callback);
  }
  
  public void asTable(AsyncCallback<Table> callback) {
    service.query(query, callback);
  }

  public void countEntities(FetchOptions fetchOptions, final AsyncCallback<Integer> callback) {
    String q = withFetchOptions(fetchOptions);
    int cut = q.indexOf(" FROM ");
    q = "SELECT ROWID " + q.substring(cut);
    service.query(q,  new ChainedCallback<Table>(callback) {
      @Override
      public void onSuccess(Table result) {
        callback.onSuccess(result.getRowCount());
      }
    });
  }
}
