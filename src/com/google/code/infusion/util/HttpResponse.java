package com.google.code.infusion.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class HttpResponse {

  private static final char[] BASE64_CHARS =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    .toCharArray();
      

  HttpURLConnection connection;
  int statusCode;
  String statusText;
  String responseData;

  private static String readString(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    InputStreamReader reader = new InputStreamReader(is, "utf-8");
    char[] buf = new char[65536];
    while (true) {
      int count = reader.read(buf);
      if (count <= 0) {
        break;
      }
      sb.append(buf, 0, count);
    }
    return sb.toString();
  }
  
  HttpResponse(HttpRequestBuilder request, final AsyncCallback<HttpResponse> callback) {
    final ArrayList<String[]> headers = new ArrayList<String[]>(request.headers);
    final String requestData = request.data;
    final String method = request.method;
    final String url;
    if (request.token != null) {
      url = HttpResponse.signUrl(method, request.url, requestData, request.token);
    } else {
      url = request.url;
    }
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          connection = (HttpURLConnection) new URL(url).openConnection();
          connection.setDoOutput(requestData != null);
          connection.setRequestMethod(method);
          for (String[] header : headers) {
            connection.setRequestProperty(header[0], header[1]);
          }

          if (requestData != null) {
            OutputStreamWriter writer = new OutputStreamWriter(
                connection.getOutputStream(), "utf-8");
            writer.write(requestData);
            writer.close();
          }
          statusCode = connection.getResponseCode();
          statusText = connection.getResponseMessage();
          responseData = readString(connection.getInputStream());
          callback.onSuccess(HttpResponse.this);
        } catch (IOException e) {
          try {
            responseData = readString(connection.getErrorStream());
          } catch(IOException e2) {  
          }
          callback.onFailure(new IOException(e.getMessage() + " / " + responseData));
        }
      }
    }).start();
  }

  public String getData() {
    return responseData;
  }

  public String getHeader(String name) {
    return connection.getHeaderField(name);
  }

  public String getStatusText() {
    return statusText;
  }

  public int getStatusCode() {
    return statusCode;
  }

  
  public static byte[] hmacSha1(String text, String key) {
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      SecretKeySpec secret = new SecretKeySpec(key.getBytes(),"HmacSHA1");
      mac.init(secret);
      return mac.doFinal(text.getBytes());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String signUrl(String method, String url, String body, OAuthToken token) {
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
      result.append(Util.urlEncode(e.getValue()));
    }
    
    String base = method + "&" + Util.urlEncode(url.substring(0, cut)) + '&' + Util.urlEncode(result.toString());
      
//    System.out.println("Base: "+ base);
      
    String key = "anonymous&";
    if (token != null && token.getTokenSecret() != null) {
      key += Util.urlEncode(token.getTokenSecret());
    }
    String hash = encodeBase64(hmacSha1(base, key));
  
     // System.out.println("hash:"+ hash);
    return url +  "&oauth_signature=" + Util.urlEncode(hash);
  }

  
  /** 
   * Encodes the given byte array in the Base64 format and 
   * returns the corresponding String
   */
  private static String encodeBase64(byte[] data) {
    StringBuilder buf  = new StringBuilder(data.length * 3 / 2);
    int end = data.length - 3;
    int i = 0;
    int n = 0;
    while (i <= end) {
      int d = ((((int) data[i]) & 0x0ff) << 16)
        | ((((int) data[i + 1]) & 0x0ff) << 8)
        | (((int) data[i + 2]) & 0x0ff);
      buf.append(BASE64_CHARS[(d >> 18) & 63]);
      buf.append(BASE64_CHARS[(d >> 12) & 63]);
      buf.append(BASE64_CHARS[(d >> 6) & 63]);
      buf.append(BASE64_CHARS[d & 63]);
      i += 3;
    }

    if (i == data.length - 2) {
      int d = ((((int) data[i]) & 0x0ff) << 16)
        | ((((int) data[i + 1]) & 255) << 8);
      buf.append(BASE64_CHARS[(d >> 18) & 63]);
      buf.append(BASE64_CHARS[(d >> 12) & 63]);
      buf.append(BASE64_CHARS[(d >> 6) & 63]);
      buf.append("=");
    } else if (i == data.length - 1) {
      int d = (((int) data[i]) & 0x0ff) << 16;
      buf.append(BASE64_CHARS[(d >> 18) & 63]);
      buf.append(BASE64_CHARS[(d >> 12) & 63]);
      buf.append("==");
    }

    return buf.toString();
  }
}
