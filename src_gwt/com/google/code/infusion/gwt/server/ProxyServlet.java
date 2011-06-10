package com.google.code.infusion.gwt.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.code.infusion.util.HttpResponse;
import com.google.code.infusion.util.OAuthToken;

@SuppressWarnings("serial")
public class ProxyServlet extends HttpServlet{
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String forwardTo = request.getHeader("X-Forward-To");
    String tokenString = request.getHeader("X-OAuth-Token");
    if (tokenString != null) {
      OAuthToken token = new OAuthToken();
      token.parse(tokenString);
      forwardTo = HttpResponse.signUrl(request.getMethod(), forwardTo, null, token);
    }
    System.out.println("URL: " + forwardTo);
    HttpURLConnection connection = (HttpURLConnection) new URL(forwardTo).openConnection();

    boolean post = request.getMethod().equals("POST");

    connection.setDoOutput(post);
    connection.setRequestMethod(request.getMethod());
    Enumeration<?> names = request.getHeaderNames();
    while (names.hasMoreElements()) {
      String name = (String) names.nextElement();

      if (!name.equals("X-Forward-To") && !name.equals("Origin") && !name.equals("Referer") && !name.equals("Connection")
          && !name.equals("Host") && !name.equals("User-Agent") && !name.equals("Accept-Encoding")) {
        System.out.println("header "+ name + ": " + request.getHeader(name));
        connection.setRequestProperty(name, request.getHeader(name));
      }
    }
    byte[] buf = new byte[8096];      
    if (post) {
      InputStream is = request.getInputStream();
      OutputStream os = connection.getOutputStream();
      while (true) {
        int count = is.read(buf);
        if (count <= 0) {
          break;
        }
        System.out.println("buf: " + new String(buf, 0, count));
        os.write(buf, 0, count);
      }
      os.close();
      is.close();
    }
    response.setStatus(connection.getResponseCode());

    for(Map.Entry<String,List<String>> entry: connection.getHeaderFields().entrySet()) {
      for (String value : entry.getValue()) {
        response.addHeader(entry.getKey(), value);
      }
    }

    InputStream is = connection.getInputStream();
    OutputStream os = response.getOutputStream();
    while (true) {
      int count = is.read(buf);
      if (count <= 0) {
        break;
      }
      System.out.println("buf: " + new String(buf, 0, count));
      os.write(buf, 0, count);
    }
    os.close();
    is.close();		
  }
}
