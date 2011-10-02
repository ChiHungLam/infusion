package com.google.code.infusion.importer;

import java.util.Iterator;

import com.google.code.infusion.json.Json;
import com.google.code.infusion.service.FusionTableService;
import com.google.code.infusion.service.Table;
import com.google.code.infusion.util.Util;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ImporterBuilder {
  public enum Type {
    BIBTEX, CSV, JSON,
  }
  
  private final FusionTableService service;
  private String name;
  private String data;
  private char delimiter;
  private String tableId;
  private int offset;
  private Type type;
  
  public ImporterBuilder(FusionTableService service) {
    this.service = service;
  }
  
  /** 
   * Set the name of the table that will be created if no tableId is 
   * provided.
   */
  public void setName(String name) {
    this.name = name;
  }
  
  public void setData(String data) {
    this.data = data;
  }
  
  public void setDelimiter(char delimiter) {
    this.delimiter = delimiter;
  }
  
  /**
   * Guess name and type from the file name if not set explicitly.
   */
  public void setFileName(String fileName) {
    if (type == null) {
      if (fileName.endsWith(".bib")) {
        type = Type.BIBTEX;
      } else if (fileName.endsWith(".csv")) {
        type = Type.CSV;
      } else if (fileName.endsWith(".json")) {
        type = Type.JSON;
      }
    }
    if (name == null) {
      int cut = fileName.lastIndexOf('/');
      if (cut != -1) {
        fileName = fileName.substring(cut + 1);
      }
      cut = fileName.lastIndexOf('.');
      if (cut != -1) {
        fileName = fileName.substring(0, cut);
      }
      name = fileName;
    }
  }
  
  public void setOffset(int offset) {
    this.offset = offset;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }
  
  public void setType(Type type) {
    this.type = type;
  }
  
  public Importer importData(final ImporterCallback callback) {
    final String[] cols;
    final Iterator<Json> rows;
    switch(type == null ? Type.CSV : type) {
    case BIBTEX: {
      Table table = BibtexParser.parse(data);
      cols = table.getCols();
      rows = table.iterator();
      break;
    }      
    case JSON: {
      Table table = JsonParser.parse(data);
      cols = table.getCols();
      rows = table.iterator();
      break;
    }
    default: {
      rows = new CsvParser(data, delimiter);
      Table table = new Table(rows.next(), null);
      cols = table.getCols();
      break;
    }
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
      StringBuilder sb = new StringBuilder("CREATE TABLE ");
      sb.append(Util.singleQuote(name));
      sb.append(" (");
      for (int i = 0; i < cols.length; i++) {
        if (i != 0) {
          sb.append(',');
        }
        sb.append(Util.singleQuote(cols[i]));
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

  
}
