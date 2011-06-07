package com.google.code.infusion.util;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class OAuthLogin {

 
  
  public static void getRequestToken(String scope, final AsyncCallback<OAuthToken> callback) {
    String url = "https://www.google.com/accounts/OAuthGetRequestToken?scope="+Util.urlEncode(scope)+"&oauth_callback=oob";
    url = OAuth.signUrl("GET", url, null, null);
    
    HttpRequestBuilder request = new HttpRequestBuilder(HttpRequestBuilder.GET, url);
    request.send(new ChainedCallback<HttpResponse>(callback) {
      @Override
      public void onSuccess(HttpResponse result) {
        Map<String,String> parsed = parseResponse(result.getData());
        OAuthToken token = new OAuthToken();
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
        String value = Util.urlDecode(part.substring(cut + 1));
        result.put(name, value);
      }
    }
    System.out.println("parsed response: " + result);
    return result;
  }

  public static void getAccessToken(OAuthToken token, String verificationCode, final AsyncCallback<OAuthToken> callback) {
    String url = "https://www.google.com/accounts/OAuthGetAccessToken?oauth_verifier="+Util.urlEncode(verificationCode);
    url = OAuth.signUrl("GET", url, null, token);
    
    HttpRequestBuilder request = new HttpRequestBuilder(HttpRequestBuilder.GET, url);
    request.send(new ChainedCallback<HttpResponse>(callback) {
      @Override
      public void onSuccess(HttpResponse result) {
        Map<String,String> parsed = parseResponse(result.getData());
        OAuthToken token = new OAuthToken();
        token.setToken(parsed.get("oauth_token"));
        token.setTokenSecret(parsed.get("oauth_token_secret"));
        callback.onSuccess(token);
      }});
    
  }
  
  
  
}
