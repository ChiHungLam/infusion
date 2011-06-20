package com.google.code.infusion.importer;

import com.google.code.infusion.json.JsonArray;
import com.google.code.infusion.service.Table;

/**
 * Parser for tables in CSV format. 
 * 
 * @author Stefan Haustein
 */
public class CsvParser {
  static final String EOL = "<EOL>";
  static final String EOF = "<EOF>";

  protected LookAheadReader reader;
  protected char commentsChar;
  private JsonArray cols;

  /**
   * Parses the given string as a table in CSV format and returns
   * the corresponding table object.
   * 
   * @param csv the CSV file content as a string
   * @param hasTitles Set to true to indicate that the first row should 
   *   be interpreted as titles. Otherwise, the columns will be named
   *   col1...colN
   * @return The parsed table
   */
  public static Table parse(String csv, boolean hasTitles) {
    CsvParser parser = new CsvParser(csv, hasTitles);
    JsonArray rows = JsonArray.create();
    while (true) {
      JsonArray row = parser.readRow();
      if (row == null) {
        break;
      }
      rows.setArray(rows.length(), row);
    }
    return new Table(parser.cols, rows);
  }

  private CsvParser(String csv, boolean titleLine) {
    this.reader = new LookAheadReader(csv);
    if(titleLine) {
      cols = readRow();
    } else {
      cols = JsonArray.create();
    }
  }

  public JsonArray readRow() {
    JsonArray row = JsonArray.create();

    String val = null;
    int i = 0;
    while (true) {
      val = readColumn();
      if (i == 0 && val == EOF) {
        return null;
      }
      if (val == EOL || val == EOF) {
        break;
      }

      row.setString(i, val);
      if (i > cols.length()) {
        cols.setString(i, "col" + i);
      }
      i++;
    }
    return row;
  }


  private void skip() {
    while (reader.peek(0) == ' ' || reader.peek(0) == '\t') {
      reader.read();
    }
  }

  private String readColumn() {
    while (reader.peek(0) == commentsChar)
      reader.readLine();

    String result;
    // reader.skip(" \t");
    skip();
    int c = reader.peek(0);
    switch (c) {
    case '"':
      result = readQuoted();
      // reader.skip(" \t");
      skip();
      if (reader.peek(0) == ',')
        reader.read();
      return result;

    case '\r':
      if (reader.peek(0) == '\n')
        reader.read();
    case '\n':
      reader.read();
      return EOL;
    case -1:
      return EOF;
    case ',':
      reader.read();
      return null;
    default:
      result = reader.readTo(",\n\r").trim();
      if (reader.peek(0) == ',')
        reader.read();
      return result;
    }
  }

  String readQuoted() {
    reader.read();

    String result = reader.readTo('"');
    reader.read();
    if (reader.peek(0) != '"')
      return result;
    StringBuffer buf = new StringBuffer(result);

    do {
      buf.append((char) reader.read()); // consume pending quote
      buf.append(reader.readTo('"'));
      reader.read(); // consume first quote
    } while (reader.peek(0) == '"');

    return buf.toString();
  }
}
