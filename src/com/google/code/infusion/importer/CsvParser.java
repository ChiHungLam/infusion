package com.google.code.infusion.importer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Stefan Haustein
 */
public class CsvParser implements Iterator<Map<String, String>> {
  static final String EOL = "<EOL>";
  static final String EOF = "<EOF>";

  protected LookAheadReader reader;
  protected char commentsChar;
  private ArrayList<String> columnNames;
  
  private Map<String,String> current;

  /**
   * If there is no header row, it is necessary to read the first data row in
   * order to determine the column count. In that case, the content of the first
   * row is stored in the variable row0.
   */

  public CsvParser(String csv, boolean titleLine) {
    this.reader = new LookAheadReader(csv);

    if (titleLine) {
      while (true) {
        String o = readColumn();
        if (o == EOL || o == EOF) {
          break;
        }

        columnNames.add(o.toString());
      }
    }
  }

  public Map<String, String> readRow() {
    Map<String, String> result = new HashMap<String, String>();

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

      result.put(i < columnNames.size() ? columnNames.get(i) : ("col" + i), val);
      i++;
    }
    return result;
  }

  
  void skip() {
    while (reader.peek(0) == ' ' || reader.peek(0) == '\t') {
      reader.read();
    }
  }

  
  String readColumn() {
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

  @Override
  public boolean hasNext() {
    if (current != null) {
      return true;
    }
    current = readRow();
    return current != null;
  }

  @Override
  public Map<String, String> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    Map<String,String> result = current;
    current = null;
    return result;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
