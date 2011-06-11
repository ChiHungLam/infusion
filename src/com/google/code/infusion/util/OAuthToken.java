package com.google.code.infusion.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the OAuth token and secret. Used for request and access tokens.
 */
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
    token = parsed.get("oauth_token");
    tokenSecret = parsed.get("oauth_token_secret");
    if (token == null) {
      throw new RuntimeException("Token not found in " + s);
    }
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