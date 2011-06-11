package com.google.code.infusion.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

public class Util {

  public static String quote(String value, char quoteChar, boolean force) {
    StringBuilder sb;
    if (force) {
      sb =  new StringBuilder();
      sb.append(quoteChar);
    } else {
      sb = null;
    }
    int pos = 0;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c != '.' && (c < '0' || c > '9') && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
        if (sb == null) {
          sb = new StringBuilder();
          sb.append(quoteChar);
        }
        sb.append(value.substring(pos, i));
        pos = i + 1;
        switch(c) {
        case '\n': 
          sb.append("\\n");
          break;
        case '\t': 
          sb.append("\\t");
          break;
        case '\r': 
          sb.append("\\r");
          break;
        case '\\': 
          sb.append("\\\\");
          break;
        default: 
          if (c == quoteChar) {
            sb.append('\\');
          }
          sb.append(c);
        }
      }
    }
    
    if (sb == null) {
      return value;
    }
    sb.append(value.substring(pos));
    sb.append(quoteChar);
    return sb.toString();
  }
  
  public static String singleQuote(String value) {
    return quote(value, '\'', false);
  }


  public static String doubleQuote(String value) {
    return quote(value, '"', false);
  }

  public static String[] parseCsv(String s) {
    ArrayList<String> parts = new ArrayList<String>();

    StringBuilder current = new StringBuilder();
    boolean quoted = false;

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
      case '"':
        quoted = !quoted;
        break;
      case '\\':
        if (i + 1 < s.length()) {
          current.append(s.charAt(++i));
        } else {
          current.append('\\');
        }
        break;
      case ',':
        if (!quoted) {
          parts.add(current.toString());
          current.setLength(0);
        } else {
          current.append(',');
        }
        break;
      default:
        current.append(c);
      }
    }
    parts.add(current.toString());

    String[] result = new String[parts.size()];
    parts.toArray(result);
    return result;
  }

  public static void parseParameters(String param, Map<String,String> result) {
    if (param != null && param.length() > 0) {
      for (String part: param.split("&")) {
        int cut = part.indexOf('=');
        if (cut == -1) {
          result.put(part, "");
        } else {
          result.put(part.substring(0, cut), Util.urlDecode(part.substring(cut + 1)));
        }
      }
    }
  }

  public static String urlEncode(String s) {
    byte[] b;
    try {
      b = s.getBytes("utf-8");
  
    int len = b.length;
    StringBuilder sb = new StringBuilder(len * 3 / 2);
    for (int i = 0; i < len; i++) {
      char c = (char) (b[i] & 255);
      if ((c >= '0' && c <= '9') || 
          (c >= 'A' && c <= 'Z') ||
          (c >= 'a' && c <= 'z') || 
          c == '_' || c == '.' || c == '~' || c == '-') {
        sb.append(c);
      } else {
        sb.append('%');
        sb.append(Util.HEX_DIGITS.charAt(c / 16));
        sb.append(Util.HEX_DIGITS.charAt(c % 16));
      }
    }
    return sb.toString();
    } catch (UnsupportedEncodingException e) {
     throw new RuntimeException(e);
    }
  }

  public static String urlDecode(String s) {
    int len = s.length();
    byte[] b = new byte[len];
    int pos = 0;
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c == '%') {
        b[pos++] = (byte) Integer.parseInt(s.substring(i+1, i+3), 16);
        i+=2;
      } else if (c == '+') {
        b[pos++] = 32;
      } else {
        b[pos++] = (byte) c;
      }
    }
    try {
      return new String(b, 0, pos, "UTF-8");
    } catch (UnsupportedEncodingException e) {
     throw new RuntimeException(e);
    }
  }

  static final String HEX_DIGITS = "0123456789ABCDEF";

 /* public static String urlEncode(String url) {
    try {
      return URLEncoder.encode(url, "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  } */
}
