package com.google.code.infusion.service;

import com.google.code.infusion.json.Json;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A table, backed by a JsonArrays to avoid manual parsing in the GWT 
 * case.
 * 
 * @author Stefan Haustein
 */
public class Table implements Iterable<Json> {
  Json cols;
  Json rows;
  private HashMap<String,Integer> map = new HashMap<String,Integer>();
  
  Table(Json table) {
    this(table.getJson("cols"), table.getJson("rows"));
  }
  
  public Table(Json cols, Json rows) {
    this.cols = cols;
    this.rows = rows;
  }
  
  public Table(String[] cols, Json rows) {
    this.cols = Json.createArray();
    this.rows = rows;
    for (int i = 0; i < cols.length; i++) {
      this.cols.setString(i, cols[i]);
    }
  }

  public String[] getCols() {
    String[] arr = new String[cols.length()];
    for (int i = 0; i < cols.length(); i++) {
      arr[i] = cols.getString(i);
    }
    return arr;
  }
  
  public int addCol(String name) {
    int idx = cols.length();
    cols.setString(idx, name);
    if (map != null) {
      map.put(name, idx);
    }
    return idx;
  }

  public String getCol(int index) {
    return cols.getString(index);
  }
  
  public int getColCount() {
    return cols.length();
  }
  
  public int getIndex(String colName) {
    if (map == null) {
      map = new HashMap<String,Integer>();
      for (int i = 0; i < cols.length(); i++) {
        map.put(cols.getString(i), i);
      }
    }
    Integer idx = map.get(colName);
    return idx == null ? -1 : idx.intValue();
  }
  
  
  public int getRowCount() {
    return rows.length();
  }

  public void addRow(Json row) {
    rows.setJson(rows.length(), row);
  }

  @Override
  public Iterator<Json> iterator() {
    return new Iterator<Json>() {
      int index;
      
      @Override
      public boolean hasNext() {
        return index < rows.length();
      }

      @Override
      public Json next() {
        return rows.getJson(index++);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };

  }
}
