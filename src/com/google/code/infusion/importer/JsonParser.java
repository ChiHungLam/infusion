package com.google.code.infusion.importer;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.json.JsonObject;
import com.google.code.infusion.service.Table;

public class JsonParser {

  
  public static Table parse(String json) {
    JsonArray parsed = JsonArray.parse(json);
    Table table = new Table(JsonArray.create(), JsonArray.create());

    for (int i = 0; i < parsed.length(); i++) {
      JsonObject o = parsed.getObject(i);
      JsonArray newRow = JsonArray.create();
      for (String key: o.getKeys()) {
        int idx = table.getIndex(key);
        if (idx == -1) {
          idx = table.addCol(key);
        }
        newRow.setString(idx, o.getString(key));
      }
      table.addRow(newRow);
    }
    return table;
  }
  
}
