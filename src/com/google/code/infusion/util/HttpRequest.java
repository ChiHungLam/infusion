package com.google.code.infusion.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;


import com.google.gwt.user.client.rpc.AsyncCallback;

public class HttpRequest {
	String url;
	String data;
	ArrayList<String[]> headers = new ArrayList<String[]>();
	
	public HttpRequest(String url) {
		this.url = url;
	}
	
	public HttpRequest addHeader(String name, String value) {
		headers.add(new String[] {name, value});
		return this;
	}
	
	public HttpRequest setData(String requestData) {
		this.data = requestData;
		return this;
	}
	
	public void request(AsyncCallback<Response> callback) {
		try {
			URLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoInput(true);
			
			for (String[] header: headers) {
				connection.setRequestProperty(header[0], header[1]);
			}
			if (data != null) {
				connection.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
				writer.write(data);
				writer.close();
			}
			
			Response response = new Response();
			StringBuilder sb = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "utf-8");
			char[] buf = new char[8096];
			while (true) {
				int count = reader.read(buf);
				if (count <= 0) {
					break;
				}
				sb.append(buf, 0, count);
			}
			response.data = sb.toString();
			
			callback.onSuccess(response);			
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			callback.onFailure(e);
		}
		
	}
	
	public class Response{
		HashMap<String,String> header = new HashMap<String,String>();
		String data;
		
		public String getData() {
			return data;
		}
		
		public String getHeader(String name) {
			return header.get(name);
		}
	}
	
}
