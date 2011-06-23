package com.google.code.infusion.service;

import java.util.Iterator;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.json.JsonObject;

/**
 * A table, backed by a JsonArrays to avoid manual parsing in the GWT 
 * case.
 * 
 * TODO: Add more convenient access.
 * 
 * @author Stefan Haustein
 */
public class SimpleTable implements Table {
  JsonArray cols;
  JsonArray rows;
  
  public SimpleTable() {
    this (JsonArray.create(), JsonArray.create());
  }
  
  SimpleTable(JsonObject table) {
    this(table.getArray("cols"), table.getArray("rows"));
  }
  
  public SimpleTable(JsonArray cols, JsonArray rows) {
    this.cols = cols;
    this.rows = rows;
  }
  
  public JsonArray getCols() {
    return cols;
  }

  public Iterable<JsonArray> getRows() {
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

  public int getRowCount() {
    return rows.length();
  }

  @Override
  public void addRow(JsonArray row) {
    rows.setArray(rows.length(), row);
  }
}
