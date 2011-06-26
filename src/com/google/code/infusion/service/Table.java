package com.google.code.infusion.service;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.json.JsonObject;

import java.util.Iterator;

/**
 * A table, backed by a JsonArrays to avoid manual parsing in the GWT 
 * case.
 * 
 * TODO: Add more convenient access.
 * 
 * @author Stefan Haustein
 */
public class Table implements Iterable<JsonArray> {
  JsonArray cols;
  JsonArray rows;
  
  public Table() {
    this (JsonArray.create(), JsonArray.create());
  }
  
  Table(JsonObject table) {
    this(table.getArray("cols"), table.getArray("rows"));
  }
  
  public Table(JsonArray cols, JsonArray rows) {
    this.cols = cols;
    this.rows = rows;
  }
  
  public JsonArray getCols() {
    return cols;
  }

  public int getRowCount() {
    return rows.length();
  }

  public void addRow(JsonArray row) {
    rows.setArray(rows.length(), row);
  }

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
}
