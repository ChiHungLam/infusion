package com.google.code.infusion.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TableDescription implements Iterable<ColumnInfo> {

  private final String id;
  private HashMap<String,ColumnInfo> columnMap = new HashMap<String, ColumnInfo>();
  private ArrayList<ColumnInfo> columnList = new ArrayList<ColumnInfo>();
  
  TableDescription(String id) {
    this.id = id;
  }
  
  void add(ColumnInfo column) {
    columnMap.put(column.getName(), column);
    columnList.add(column);
  }
  
  public String getId() {
    return id;
  }

  public ColumnInfo get(String key) {
    return columnMap.get(key);
  }
  
  public ColumnInfo get(int index) {
    return columnMap.get(index);
  }

  public List<ColumnInfo> getColumnList() {
    return new ArrayList<ColumnInfo>(columnList);
  }

  @Override
  public Iterator<ColumnInfo> iterator() {
    return columnList.iterator();
  }

}
