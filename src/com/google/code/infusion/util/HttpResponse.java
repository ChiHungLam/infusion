package com.google.code.infusion.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class HttpResponse {

  HttpURLConnection connection;
  int statusCode;
  String statusText;
  String data;

  HttpResponse(HttpRequestBuilder request, AsyncCallback<HttpResponse> callback) {
    try {
      connection = (HttpURLConnection) new URL(request.url).openConnection();
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

  public String gerHeader(String name) {
    return connection.getHeaderField(name);
  }

  public String getStatusText() {
    return statusText;
  }

  public int getStatusCode() {
    return statusCode;
  }

}
