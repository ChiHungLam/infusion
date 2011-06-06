package com.google.code.infusion.service;

import java.util.Iterator;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.json.JsonObject;

public class Table {
  JsonArray cols;
  JsonArray rows;
  
  public Table(JsonObject table) {
    this.cols = table.getArray("cols");
    this.rows = table.getArray("rows");
  }
  
  public JsonArray getRows() {
    return rows;
  }
  
  public JsonArray getCols() {
    return cols;
  }

  public Iterable<JsonArray> getRowsAsIterable() {
    return new Iterable<JsonArray>() {
      @Override
      public Iterator<JsonArray> iterator() {
        return new Iterator<JsonArray>() {
          int index;
          
          @Override
          public boolean hasNext() {
            return index < rows.length();
          }

          @Override
          public JsonArray next() {
            return rows.getArray(index++);
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
  
  
}
