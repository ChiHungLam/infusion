package com.google.code.infusion.util;

import java.util.HashMap;
import java.util.Map;

public class OAuthToken {
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

  public void parse(String s) {
    Map<String,String> parsed = new HashMap<String,String>();
    Util.parseParameters(s, parsed);
    setToken(parsed.get("oauth_token"));
    setTokenSecret(parsed.get("oauth_token_secret"));
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (token != null) {
      sb.append("oauth_token=");
      sb.append(Util.urlEncode(token));
    } 
    if (tokenSecret != null) {
      if (sb.length() > 0) {
        sb.append('&');
      }
      sb.append("oauth_token_secret=");
      sb.append(Util.urlEncode(tokenSecret));
    }
    return sb.toString();
  }
  
}