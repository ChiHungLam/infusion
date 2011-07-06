package com.google.code.infusion.importer;

import java.util.Iterator;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.service.Table;
import com.google.code.infusion.util.Util;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ImporterBuilder {
  private final FusionTableService service;
  private String fileName;
  private String data;
  private char delimiter;
  private String tableId;
  private int offset;
  
  public ImporterBuilder(FusionTableService service) {
    this.service = service;
  }
  
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  
  public void setData(String data) {
    this.data = data;
  }
  
  public void setTableId(String tableId) {
    this.tableId = tableId;
  }
  
  public void setDelimiter(char delimiter) {
    this.delimiter = delimiter;
  }
  
  public void setOffset(int offset) {
    this.offset = offset;
  }
  
  public Importer importData(final ImporterCallback callback) {
    final JsonArray cols;
    final Iterator<JsonArray> rows;
    if (fileName.endsWith(".bib")) {
      Table table = BibtexParser.parse(data);
      cols = table.getCols();
      rows = table.iterator();
    } else {
      rows = new CsvParser(data, delimiter);
      cols = rows.next();
    }
    for (int i = 0; i < offset; i++) {
      rows.next();
    }
    final Importer importer = 
        new Importer(service, cols, rows, offset, callback);
    if (tableId != null) {
      importer.setTableId(tableId);
      importer.run();
    } else {
      String name = fileName;
      int cut = name.lastIndexOf('/');
      if (cut != -1) {
        name = name.substring(cut + 1);
      }
      cut = name.lastIndexOf('.');
      if (cut != -1) {
        name = name.substring(0, cut);
      }
      StringBuilder sb = new StringBuilder("CREATE TABLE ");
      sb.append(Util.singleQuote(name));
      sb.append(" (");
      for (int i = 0; i < cols.length(); i++) {
        if (i != 0) {
          sb.append(',');
        }
        sb.append(Util.singleQuote(cols.getString(i)));
        sb.append(":STRING");
      }
      sb.append(')');
      service.query(sb.toString(), new AsyncCallback<Table>() {
        @Override
        public void onSuccess(Table result) {
          String tableId = result.iterator().next().getString(0);
          importer.setTableId(tableId);
          importer.run();
        }
        
        @Override
        public void onFailure(Throwable error) {
          callback.onFailure(importer, error);
        }
      });
    }
    return importer;
  }

  public String getFileName() {
    return fileName;
  }
  
}
