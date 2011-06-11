package com.google.code.infusion.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Contains helper methods for each of the 3 legs of OAuth authentication.
 */
public class OAuthLogin {

  public static String BASE_URL = "https://www.google.com/accounts/";

  public static void getRequestToken(String scope, String callbackUrl, 
      final AsyncCallback<OAuthToken> callback) {
    if (callbackUrl == null) {
      callbackUrl = "oob";
    }
    String url = BASE_URL + "OAuthGetRequestToken?scope="+
      Util.urlEncode(scope)+"&oauth_callback=" + Util.urlDecode(callbackUrl);
    
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
  

  public static void getAccessToken(OAuthToken token, String verificationCode, 
      final AsyncCallback<OAuthToken> callback) {
    String url = BASE_URL + "OAuthGetAccessToken?oauth_verifier=" + 
      Util.urlEncode(verificationCode);
    
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
    return BASE_URL + "/OAuthAuthorizeToken?hd=default&oauth_token=" + 
      Util.urlEncode(requestToken.getToken());
  }
}
