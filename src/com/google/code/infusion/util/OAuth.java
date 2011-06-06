package com.google.code.infusion.util;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;

public class OAuth {
  static final String HEX_DIGITS = "0123456789ABCDEF";
  
  public static class Token {
    private String token;
    private String tokenSecret;
    
    public String getToken() {
      return token;
    }
    
    public String getTokenSecret() {
      return tokenSecret;
    }
    
    public void setToken(String token) {
      this.token = token;
    }
    
    public void setTokenSecret(String secret) {
      this.tokenSecret = secret;
    }
  }

  private static void parseParameters(String param, Map<String,String> result) {
    for (String part: param.split("&")) {
      int cut = part.indexOf('=');
      if (cut == -1) {
        result.put(part, "");
      } else {
        result.put(part.substring(0, cut), part.substring(cut + 1));
      }
    }
  }
  

  /**
   * Sending anything but null as the body implies a POST request.
   * (Send "" to force a POST).
   */
  public static String signUrl(String url, String body, Token token) {
    // http//:foo.com:555/
    
    TreeMap<String,String> params = new TreeMap<String,String>();
    
    int cut = url.indexOf('?');
    if (cut == -1) {
      cut = url.length();
      url += "?";
    } else {
      url += "&";
    }

    url += "oauth_consumer_key=anonymous" +    
      "&oauth_nonce=" + Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE)) +
      "&oauth_signature_method=HMAC-SHA1"+
      "&oauth_timestamp="+ (System.currentTimeMillis() / 1000) +
      "&oauth_versiom=1.0";
      if (token != null && token.getToken() != null) {
        url += "&oauth_token=" + urlEncode(token.getToken());
      }
    
    parseParameters(url.substring(cut + 1), params);

    if (body != null) {
      parseParameters(body, params);
    }
    
    StringBuilder result = new StringBuilder();
    for (Map.Entry<String,String> e: params.entrySet()) {
      if (result.length() > 0) {
        result.append('&');
      }
      result.append(e.getKey());
      result.append('=');
      result.append(urlEncode(urlDecode(e.getValue())));
    }
    
    String base = (body == null ? "GET&" : "POST&") + urlEncode(url.substring(0, cut)) + '&' + urlEncode(result.toString());
    
 //   base = "GET&https%3A%2F%2Fwww.google.com%2Faccounts%2FOAuthGetRequestToken&oauth_callback%3Dhttp%253A%252F%252Fgooglecodesamples.com%252Foauth_playground%252Findex.php%26oauth_consumer_key%3Danonymous%26oauth_nonce%3Dbde95c5378d275ffaf36f538aab0ae77%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1274670260%26oauth_version%3D1.0%26scope%3Dhttps%253A%252F%252Fwww.google.com%252Fcalendar%252Ffeeds%252F";
    
//    System.out.println("base: " + base);
    
    String key = "anonymous&";
    if (token != null && token.getTokenSecret() != null) {
      key += urlEncode(token.getTokenSecret());
    }
    String hash = HmacSha1Base64.sign(base, key);

   // System.out.println("hash:"+ hash);
    return url +  "&oauth_signature=" + urlEncode(hash);
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
        sb.append(HEX_DIGITS.charAt(c / 16));
        sb.append(HEX_DIGITS.charAt(c % 16));
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
  
}