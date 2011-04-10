package com.google.code.infusion.importer;

/**
 * Wrapper around a string that keeps a position and some helper methods that
 * simplify parsing.
 */

public class LookAheadReader {

  String content;
  int pos;

  public LookAheadReader(String content) {
    this.content = content;
  }

  public String readTo(String chars) {
    StringBuffer buf = new StringBuffer();

    while (peek(0) != -1 && chars.indexOf((char) peek(0)) == -1) {
      buf.append((char) read());
    }

    return buf.toString();
  }

  public String readTo(char c) {
    StringBuffer buf = new StringBuffer();
    while (peek(0) != -1 && peek(0) != c) {
      buf.append((char) read());
    }
    return buf.toString();
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
    StringBuffer buf = new StringBuffer();
    while (peek(0) != -1 && chars.indexOf((char) peek(0)) != -1) {
      buf.append((char) read());
    }
    return buf.toString();
  }

  public void skip(String chars) {
    while (peek(0) != -1 && chars.indexOf((char) peek(0)) != -1) {
      read();
    }
  }
}