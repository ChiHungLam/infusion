package com.google.code.infusion.util;

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
}