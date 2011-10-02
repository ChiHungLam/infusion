package com.google.code.infusion.importer;

import java.util.Iterator;

import com.google.code.infusion.json.Json;
import com.google.code.infusion.service.Table;

/**
 * Parser for tables in CSV format. 
 * 
 * @author Stefan Haustein
 */
public class CsvParser implements Iterator<Json>{
  static final String EOL = "<EOL>";
  static final String EOF = "<EOF>";

  protected LookAheadReader reader;
  protected char commentsChar;
  private char delimiter;
  private String readTo;
  Json currentRow;

  /**
   * Parses the given string as a table in CSV format and returns
   * the corresponding table object.
   */
  public static Table parse(final String csv, final char delimiter) {
    CsvParser parser = new CsvParser(csv, delimiter);
    Table table = new Table(parser.next(), Json.createArray());
    while(parser.hasNext()) {
      table.addRow(parser.next());
    }
    return table;
  }

  public CsvParser(String csv, char delimiter) {
    this.reader = new LookAheadReader(csv);
    this.delimiter = delimiter;
    this.readTo = "\n\r" + delimiter;
  }

  private Json readRow() {
    Json row = Json.createArray();

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
    if (c == delimiter) {
      reader.read();
      return null;
    } else {
      switch (c) {
      case '"':
        result = readQuoted();
        // reader.skip(" \t");
        skip();
        if (reader.peek(0) == delimiter) {
          reader.read();
        }
        return result;

      case '\r':
        if (reader.peek(1) == '\n') {
          reader.read();
        }
      case '\n':
        reader.read();
        return EOL;
      case -1:
        return EOF;
      default:
        result = reader.readTo(readTo).trim();
        if (reader.peek(0) == delimiter)
          reader.read();
        return result;
      }
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

  @Override
  public boolean hasNext() {
    if (currentRow == null) {
      currentRow = readRow();
    }
    return currentRow != null;
  }

  @Override
  public Json next() {
    if (!hasNext()) {
      throw new IllegalStateException();
    }
    Json result = currentRow;
    currentRow = null;
    return result;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
