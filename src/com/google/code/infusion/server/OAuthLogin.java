package com.google.code.infusion.server;

import java.util.HashMap;
import java.util.Map;

import com.google.code.infusion.util.ChainedCallback;
import com.google.code.infusion.util.HttpRequest;
import com.google.code.infusion.util.HttpResponse;
import com.google.code.infusion.util.OAuth;
import com.google.code.infusion.util.Util;
import com.google.code.infusion.util.OAuth.Token;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class OAuthLogin {

 
  
  public static void getRequestToken(String scope, final AsyncCallback<Token> callback) {
    String url = "https://www.google.com/accounts/OAuthGetRequestToken?scope="+OAuth.urlEncode(scope)+"&oauth_callback=oob";
    url = OAuth.signUrl(url, null, null);
    
    HttpRequest request = new HttpRequest(HttpRequest.GET, url);
    request.send(new ChainedCallback<HttpResponse>(callback) {
      @Override
      public void onSuccess(HttpResponse result) {
        Map<String,String> parsed = parseResponse(result.getData());
        Token token = new Token();
        token.setToken(parsed.get("oauth_token"));
        token.setTokenSecret(parsed.get("oauth_token_secret"));
        callback.onSuccess(token);
      }});
  }
  
  static Map<String,String> parseResponse(String response) {
    HashMap<String,String> result = new HashMap<String,String>();
    for(String part: response.split("&")) {
      int cut = part.indexOf('=');
      if (cut != -1) {
        String name = part.substring(0, cut);
        String value = OAuth.urlDecode(part.substring(cut + 1));
        result.put(name, value);
      }
    }
    System.out.println("parsed response: " + result);
    return result;
  }

  public static void getAccessToken(Token token, String verificationCode, final AsyncCallback<Token> callback) {
    String url = "https://www.google.com/accounts/OAuthGetAccessToken?oauth_verifier="+OAuth.urlEncode(verificationCode);
    url = OAuth.signUrl(url, null, token);
    
    HttpRequest request = new HttpRequest(HttpRequest.GET, url);
    request.send(new ChainedCallback<HttpResponse>(callback) {
      @Override
      public void onSuccess(HttpResponse result) {
        Map<String,String> parsed = parseResponse(result.getData());
        Token token = new Token();
        token.setToken(parsed.get("oauth_token"));
        token.setTokenSecret(parsed.get("oauth_token_secret"));
        callback.onSuccess(token);
      }});
    
  }
  
  
  
}
