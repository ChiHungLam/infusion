package com.google.code.infusion.importer;

import java.util.Iterator;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.service.Table;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Importer {
  private final FusionTableService service;
  private final JsonArray cols;
  private final Iterator<JsonArray> rows;
  private final int offset;
  private final ImporterCallback callback;
  private String tableId;
  private int count;
  
  Importer(FusionTableService service, final JsonArray cols, final Iterator<JsonArray> rows, int offset, ImporterCallback callback) {
    this.service = service;
    this.cols = cols;
    this.rows = rows;
    this.offset = offset;
    this.callback = callback;
  }
  
  // package
  void run() {
    final Table buf = new Table(cols, JsonArray.create());
    if (!rows.hasNext()) {
      callback.onSuccess(this);
      return;
    }
    for (int i = 0; i < 250 && rows.hasNext(); i++) {
      buf.addRow(rows.next());
    }
    
    service.insert(tableId, buf, new AsyncCallback<Table>() {
      @Override
      public void onSuccess(Table result) {
        count += result.getRowCount();
        callback.onProgress(Importer.this);
        run();
      }

      @Override
      public void onFailure(Throwable error) {
        callback.onFailure(Importer.this, error);
      }
    });
  }
  
  void setTableId(String tableId) {
    this.tableId = tableId;
  }
  
  /**
   * Number of records successfully imported.
   */
  public int getCount() { 
    return count;
  }
  
  /**
   * Offset where import started.
   */
  public int getOffset() {
    return offset;
  }
  
  /**
   * Id of the table where the records are imported.
   */
  public String getTableId() {
    return tableId;
  }
}
