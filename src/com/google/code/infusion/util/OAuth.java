package com.google.code.infusion.util;

import java.util.Map;
import java.util.TreeMap;

public class OAuth {
  /**
   * Sending anything but null as the body implies a POST request.
   * (Send "" to force a POST).
   */
  public static String signUrl(String method, String url, String body, OAuthToken token) {
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
        url += "&oauth_token=" + Util.urlEncode(token.getToken());
      }
    
    Util.parseParameters(url.substring(cut + 1), params);

    if (body != null) {
      Util.parseParameters(body, params);
    }
    
    StringBuilder result = new StringBuilder();
    for (Map.Entry<String,String> e: params.entrySet()) {
      if (result.length() > 0) {
        result.append('&');
      }
      result.append(e.getKey());
      result.append('=');
      result.append(Util.urlEncode(Util.urlDecode(e.getValue())));
    }
    
    String base = method + "&" + Util.urlEncode(url.substring(0, cut)) + '&' + Util.urlEncode(result.toString());
    
 //   base = "GET&https%3A%2F%2Fwww.google.com%2Faccounts%2FOAuthGetRequestToken&oauth_callback%3Dhttp%253A%252F%252Fgooglecodesamples.com%252Foauth_playground%252Findex.php%26oauth_consumer_key%3Danonymous%26oauth_nonce%3Dbde95c5378d275ffaf36f538aab0ae77%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1274670260%26oauth_version%3D1.0%26scope%3Dhttps%253A%252F%252Fwww.google.com%252Fcalendar%252Ffeeds%252F";
    
//    System.out.println("base: " + base);
    
    String key = "anonymous&";
    if (token != null && token.getTokenSecret() != null) {
      key += Util.urlEncode(token.getTokenSecret());
    }
    String hash = HmacSha1Base64.sign(base, key);

   // System.out.println("hash:"+ hash);
    return url +  "&oauth_signature=" + Util.urlEncode(hash);
  }
  
}