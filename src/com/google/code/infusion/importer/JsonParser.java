package com.google.code.infusion.importer;

import com.google.code.infusion.json.Json;
import com.google.code.infusion.service.Table;

public class JsonParser {

  
  public static Table parse(String json) {
    Json parsed = Json.parse(json);
    Table table = new Table(Json.createArray(), Json.createArray());

    for (int i = 0; i < parsed.length(); i++) {
      Json o = parsed.getJson(i);
      Json newRow = Json.createArray();
      for (String key: o.getKeys()) {
        int idx = table.getIndex(key);
        if (idx == -1) {
          idx = table.addCol(key);
        }
        newRow.setString(idx, o.getAsString(key));
      }
      table.addRow(newRow);
    }
    return table;
  }
  
}
