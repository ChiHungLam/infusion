package com.google.code.infusion.gwt.server;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.code.infusion.util.HttpResponse;
import com.google.code.infusion.util.OAuthLogin;
import com.google.code.infusion.util.OAuthToken;

@SuppressWarnings("serial")
public class ProxyServlet extends HttpServlet{

  private final String[] URL_WHITELIST = {
      OAuthLogin.BASE_URL,
      "https://www.google.com/fusiontables/api/query"
  };

  private final String[] REQUEST_HEADER_WHITELIST = {
  };

  private final String[] RESPONSE_HEADER_WHITELIST = {
  };
  
  
  private static byte[] readStream(InputStream is, int contentLength) throws IOException {
    if (contentLength != -1) {
      byte[] data = new byte [contentLength];
      DataInputStream dis = new DataInputStream(is);
      dis.readFully(data);
      return data;
    } 
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[65536];
    while(true) {
      int count = is.read(buf);
      if (count <= 0) {
        break;
      }
      baos.write(buf, 0, count);
    }
    return baos.toByteArray();
   }
  
  
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String forwardTo = request.getHeader("X-Forward-To");
    boolean whitelisted = false;
    for (String w: URL_WHITELIST) {
      if (forwardTo.startsWith(w)) {
        whitelisted = true;
      }
    }
    if (!whitelisted) {
      throw new RuntimeException("Invalid target URL");
    }

    byte[] data = readStream(request.getInputStream(), request.getContentLength());
    String tokenString = request.getHeader("X-OAuth-Token");
    if (tokenString != null) {
      OAuthToken token = new OAuthToken();
      token.parse(tokenString);
      forwardTo = HttpResponse.signUrl(request.getMethod(), forwardTo, new String(data, "utf-8"), token);
    }

    HttpURLConnection connection = (HttpURLConnection) new URL(forwardTo).openConnection();    
    connection.setDoOutput(data.length > 0);
    connection.setRequestMethod(request.getMethod());
    
//    Enumeration e = request.getHeaderNames();
//    while (e.hasMoreElements()) {
//      String name = (String) e.nextElement();
//      System.out.println("ignoring header " + name + ": " + request.getHeader(name));
//    }
    
    for (String name: REQUEST_HEADER_WHITELIST) {
      String value = request.getHeader(name);
      if (value != null && value.length() > 0) {
        System.out.println("Setting header " + name + ": " + value);
        connection.setRequestProperty(name, value);
      }
    }
    connection.setRequestProperty("X-Forwarded-For", request.getRemoteAddr());
    
    if (data.length > 0) {
      OutputStream os = connection.getOutputStream();
      os.write(data);
      os.close();
    }
    response.setStatus(connection.getResponseCode());

//    System.out.println("Status:  " + connection.getResponseMessage());
//    System.out.println("all response headers: " + connection.getHeaderFields());

    for (String name: RESPONSE_HEADER_WHITELIST) {
      String value = connection.getHeaderField(name);
      if (value != null && value.length() > 0) {
//        System.out.println("Setting header " + name + ": " + value);
        response.setHeader(name, value);
      }
    }

    InputStream is = connection.getInputStream();
    OutputStream os = response.getOutputStream();
    byte[] buf = new byte[65536];
    while (true) {
      int count = is.read(buf);
      if (count <= 0) {
        break;
      }
//      System.out.println("Buf: " + new String(buf));
      os.write(buf, 0, count);
      //os.flush();
    }
    os.close();
    is.close();
  }
}
