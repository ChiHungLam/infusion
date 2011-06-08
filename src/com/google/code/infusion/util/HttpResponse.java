package com.google.code.infusion.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class HttpResponse {

  HttpURLConnection connection;
  int statusCode;
  String statusText;
  String data;

  HttpResponse(HttpRequestBuilder request, AsyncCallback<HttpResponse> callback) {
    try {
      String url = request.url;
      if (request.token != null) {
        url = HttpResponse.signUrl(request.method, url, request.data, request.token);
      }
      
      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setDoOutput(request.data != null);
      connection.setRequestMethod(request.method);
      for (String[] header : request.headers) {
        connection.setRequestProperty(header[0], header[1]);
      }

      if (request.data != null) {
        OutputStreamWriter writer = new OutputStreamWriter(
            connection.getOutputStream(), "utf-8");
        writer.write(request.data);
        writer.close();
      }

      statusCode = connection.getResponseCode();
      statusText = connection.getResponseMessage();

      StringBuilder sb = new StringBuilder();
      InputStreamReader reader = new InputStreamReader(
          connection.getInputStream(), "utf-8");
      char[] buf = new char[8096];
      while (true) {
        int count = reader.read(buf);
        if (count <= 0) {
          break;
        }
        sb.append(buf, 0, count);
      }
      data = sb.toString();
      callback.onSuccess(this);
    } catch (IOException e) {
      StringBuilder sb = new StringBuilder();
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
        while(true) {
          String line = reader.readLine();
          if (line == null) {
            break;
          }
          sb.append(line);
          sb.append('\n');
        }
      } catch(IOException e2) {  
      }
      callback.onFailure(sb.length() == 0 ? e : new IOException(e.getMessage() + " / " + sb.toString()));
    }
  }

  public String getData() {
    return data;
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

  
  public static String hmacSha1Base64(String text, String key) {
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      SecretKeySpec secret = new SecretKeySpec(key.getBytes(),"HmacSHA1");
      mac.init(secret);
      byte[] digest = mac.doFinal(text.getBytes());
      
      return encodeBase64(digest);
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
      
   //   base = "GET&https%3A%2F%2Fwww.google.com%2Faccounts%2FOAuthGetRequestToken&oauth_callback%3Dhttp%253A%252F%252Fgooglecodesamples.com%252Foauth_playground%252Findex.php%26oauth_consumer_key%3Danonymous%26oauth_nonce%3Dbde95c5378d275ffaf36f538aab0ae77%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1274670260%26oauth_version%3D1.0%26scope%3Dhttps%253A%252F%252Fwww.google.com%252Fcalendar%252Ffeeds%252F";
      
  //    System.out.println("base: " + base);
      
    String key = "anonymous&";
    if (token != null && token.getTokenSecret() != null) {
      key += Util.urlEncode(token.getTokenSecret());
    }
    String hash = HttpResponse.hmacSha1Base64(base, key);
  
     // System.out.println("hash:"+ hash);
      return url +  "&oauth_signature=" + Util.urlEncode(hash);
    }

  
  static final char[] charTab =
              "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
                  .toCharArray();
      
          public static String encodeBase64(byte[] data) {
              return encodeBase64(data, 0, data.length, null).toString();
          }
      
          /** Encodes the part of the given byte array denoted by start and
          len to the Base64 format.  The encoded data is appended to the
          given StringBuffer. If no StringBuffer is given, a new one is
          created automatically. The StringBuffer is the return value of
          this method. */
      
          public static StringBuilder encodeBase64(
              byte[] data,
              int start,
              int len,
              StringBuilder buf) {
      
              if (buf == null)
                  buf = new StringBuilder(data.length * 3 / 2);
      
             int end = len - 3;
              int i = start;
              int n = 0;
      
              while (i <= end) {
                  int d =
                      ((((int) data[i]) & 0x0ff) << 16)
                          | ((((int) data[i + 1]) & 0x0ff) << 8)
                          | (((int) data[i + 2]) & 0x0ff);
      
                  buf.append(charTab[(d >> 18) & 63]);
                  buf.append(charTab[(d >> 12) & 63]);
                  buf.append(charTab[(d >> 6) & 63]);
                  buf.append(charTab[d & 63]);
      
                  i += 3;
      
                 if (n++ >= 14) {
                      n = 0;
                      buf.append("\r\n");
                  }
              }
      
              if (i == start + len - 2) {
                  int d =
                      ((((int) data[i]) & 0x0ff) << 16)
                          | ((((int) data[i + 1]) & 255) << 8);
      
                  buf.append(charTab[(d >> 18) & 63]);
                  buf.append(charTab[(d >> 12) & 63]);
                  buf.append(charTab[(d >> 6) & 63]);
                  buf.append("=");
              }
              else if (i == start + len - 1) {
                  int d = (((int) data[i]) & 0x0ff) << 16;
      
                  buf.append(charTab[(d >> 18) & 63]);
                  buf.append(charTab[(d >> 12) & 63]);
                  buf.append("==");
              }
      
              return buf;
          }
    
}
