package com.google.code.infusion.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Contains helper methods for each of the 3 legs of OAuth authentication.
 */
public class OAuthLogin {

  public static final String BASE_URL = "https://www.google.com/accounts/";

  /**
   * Step 1 of OAuth 1.0 authentication: Requests a request token for
   * the given scope.
   * @param scope The scope for authentication, usually the base URL
   *   of the service authentication is obtained for. 
   * @param callbackUrl Url to provide the verification code to (which
   *   will be generated in the second step. If null, "oob" (out of 
   *   band) will be used, causing step 2 to print the verification
   *   code.
   * @param callback The callback the request token will be sent to.
   */
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
        if (token.parse(result.getData())) {
          callback.onSuccess(token);
        } else {
          callback.onFailure(new RuntimeException(
              "Request token not found in response: " + result.getData()));
        }
      }});
  }

  /**
   * Construct the URL for the second step of OAuth authentication.
   * Open a browser with this URL to enable the user to authorize
   * the request. If successful, the verification code will be printed
   * or sent to the URL provided in the first step.
   * 
   * @param requestToken the request token obtained in the first step.
   */
  public static String getAuthorizationUrl(OAuthToken requestToken) {
    return BASE_URL + "OAuthAuthorizeToken?hd=default&oauth_token=" + 
      Util.urlEncode(requestToken.getToken());
  }

  /**
   * Third step of the OAuth process. Obtains an access token,
   * given the request token and verification code. The access token
   * can be used to sign URLs and to authenticate API access.
   * 
   * @param token The request token
   * @param verificationCode The verification code, obtained via the 
   *   callback URL or from the user.
   * @param callback The access token will be delivered to this callback.
   */
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
        if (token.parse(result.getData())) {
          callback.onSuccess(token);
        } else {
          callback.onFailure(new RuntimeException(
              "Access token not found in response: " + result.getData()));
        }
      }});
    
  }
}
