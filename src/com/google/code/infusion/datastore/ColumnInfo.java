package com.google.code.infusion.datastore;

public class ColumnInfo {
  private final String name;
  private final ColumnType<?> type;

  public ColumnInfo(String name, ColumnType<?> type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public ColumnType<?> getType() {
    return type;
  }

  public String toString() {
    return name + ": " + type;
  }
}
