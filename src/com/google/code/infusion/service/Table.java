package com.google.code.infusion.service;

import com.google.code.infusion.json.JsonArray;

public interface Table {

  JsonArray getCols();
  Iterable<JsonArray> getRows();
  /**
   * Optional operation
   */
  void addRow(JsonArray row);
  int getRowCount();
}
