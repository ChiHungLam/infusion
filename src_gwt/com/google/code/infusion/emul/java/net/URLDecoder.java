package java.net;

import java.io.UnsupportedEncodingException;

public class URLDecoder {
  public static String decode(String url, String charset) throws java.io.UnsupportedEncodingException {
    if (!charset.equalsIgnoreCase("utf-8")) {
      throw new UnsupportedEncodingException();
    }
    return com.google.gwt.http.client.URL.decodeQueryString(url);
  }
}