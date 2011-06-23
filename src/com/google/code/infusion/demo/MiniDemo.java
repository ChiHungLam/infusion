package com.google.code.infusion.demo;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.service.SimpleTable;
import com.google.gwt.user.client.rpc.AsyncCallback;

/** 
 * Simple demo that accesses a public table and prints the contents.
 */
public class MiniDemo {
  public static void main(String[] args) {
    FusionTableService service = new FusionTableService();
    
    service.query("select * from 197026", new AsyncCallback<SimpleTable>() {
      @Override
      public void onSuccess(SimpleTable result) {
        for (JsonArray row: result.getRows()) {
          System.out.println(row.serialize());
        }
      }
      @Override
      public void onFailure(Throwable error) {
        error.printStackTrace();
      }
    });
  }
}
