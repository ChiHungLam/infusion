package com.google.code.infusion.demo.gwt.client;

import java.util.List;

import com.google.code.infusion.datastore.Entity;
import com.google.code.infusion.datastore.FetchOptions;
import com.google.code.infusion.datastore.FusionTableService;
import com.google.code.infusion.datastore.PreparedQueryAsync;
import com.google.code.infusion.datastore.Query;
import com.google.code.infusion.util.AsyncCallback;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class FusionTableProvider extends AbstractDataProvider<Entity> {

  FusionTableService service;
  Query query;
  
  FusionTableProvider(FusionTableService service, Query query) {
    this.service = service;
    this.query = query;
  }
  
  
  @Override
  protected void onRangeChanged(final HasData<Entity> display) {
    final Range range = display.getVisibleRange();
    PreparedQueryAsync preparedQuery = service.prepareQuery(query);
    FetchOptions fetchOptions = FetchOptions.Builder.withOffset(range.getStart()).limit(range.getLength());
    
    preparedQuery.asList(fetchOptions, new AsyncCallback<List<Entity>>() {

      public void onSuccess(List<Entity> result) {
        display.setRowData(range.getStart(), result);
      }

      public void onFailure(Throwable error) {
        // TODO Auto-generated method stub
        
      }
    });
  }
  
  
}
