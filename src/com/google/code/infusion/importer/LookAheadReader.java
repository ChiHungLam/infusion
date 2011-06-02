package com.google.code.infusion.importer;

/**
 * Wrapper around a string that keeps a position and some helper methods that
 * simplify parsing.
 */
public class LookAheadReader {

  String content;
  int pos;
  int len;

  public LookAheadReader(String content) {
    this.content = content;
    len = content.length();
  }

  public String readTo(String chars) {
    int start = pos;
    while (pos < len && chars.indexOf(content.charAt(pos)) == -1) {
      pos++;
    }
    return content.substring(start, pos);
  }

  public String readTo(char c) {
    int start = pos;
    while (pos < len && content.charAt(pos) != c) {
      pos++;
    }
    return content.substring(start, pos);
  }

  public int read() {
    if (pos >= content.length()) {
      return -1;
    }
    return content.charAt(pos++);
  }

  public int peek(int delta) {
    return pos + delta >= content.length() ? -1 : content.charAt(pos + delta);
  }

  public String readLine() {
    if (peek(0) == -1)
      return null;
    String s = readTo("\r\n");
    if (read() == '\r' && peek(0) == '\n')
      read();
    return s;
  }

  public String readWhile(String chars) {
    int start = pos;
    while (pos < len && chars.indexOf(content.charAt(pos)) != -1) {
      pos++;
    }
    return content.substring(start, pos);
  }

  public void skip(String chars) {
    while (pos < len && chars.indexOf(content.charAt(pos)) != -1) {
      pos++;
    }
  }
}