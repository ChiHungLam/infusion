package com.google.code.infusion.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class OAuthLogin {

 
  
  public static void getRequestToken(String scope, final AsyncCallback<OAuthToken> callback) {
    String url = "https://www.google.com/accounts/OAuthGetRequestToken?scope="+Util.urlEncode(scope)+"&oauth_callback=oob";
    
    HttpRequestBuilder request = new HttpRequestBuilder(HttpRequestBuilder.GET, url);
    request.setOAuthToken(new OAuthToken());
    request.send(new ChainedCallback<HttpResponse>(callback) {
      @Override
      public void onSuccess(HttpResponse result) {
        OAuthToken token = new OAuthToken();
        token.parse(result.getData());
        callback.onSuccess(token);
      }});
  }
  

  public static void getAccessToken(OAuthToken token, String verificationCode, final AsyncCallback<OAuthToken> callback) {
    String url = "https://www.google.com/accounts/OAuthGetAccessToken?oauth_verifier="+Util.urlEncode(verificationCode);
    
    HttpRequestBuilder request = new HttpRequestBuilder(HttpRequestBuilder.GET, url);
    request.setOAuthToken(token);
    request.send(new ChainedCallback<HttpResponse>(callback) {
      @Override
      public void onSuccess(HttpResponse result) {
        OAuthToken token = new OAuthToken();
        token.parse(result.getData());
        callback.onSuccess(token);
      }});
    
  }

  public static String getAuthorizationUrl(OAuthToken requestToken) {
    return "https://www.google.com/accounts/OAuthAuthorizeToken?hd=default&oauth_token=" + Util.urlEncode(requestToken.getToken());
  }
}
