package com.google.code.infusion.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
//import com.google.gwt.http.client.StringValidator;

/**
 * Identical to com.google.gwt.http.client.RequestBuilder,
 * but with a pure Java implementation.
 */
public class RequestBuilder extends com.google.gwt.http.client.RequestBuilder {
	
	HashSet<String> headers = new HashSet<String>();
	
	public RequestBuilder(Method httpMethod, String url) {
		super(httpMethod, url);
	}

	public void setHeader(String name, String value) {
		super.setHeader(name, value);
		headers.add(name);
	}

	
	public Request sendRequest() throws RequestException {	
		return sendRequest(getRequestData(), getCallback());
	}

	
	public Request sendRequest(final String requestData, final RequestCallback callback) throws RequestException{
		Request request = new Request() {};
		 
		try {
			final HttpURLConnection connection = (HttpURLConnection) new URL(getUrl()).openConnection();
			connection.setDoInput(true);			  
			  
			for (String header : headers) {
				connection.setRequestProperty(header, getHeader(header));
				System.out.println("Setting header '" + header + "' to '" + getHeader(header) +"'.");
			}
			
			
			if (getHTTPMethod().equalsIgnoreCase("post")) {
				System.out.println("POST " + getUrl());
				connection.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(
						connection.getOutputStream(), "utf-8");
				System.out.println("Sending: " + requestData);
				writer.write(requestData);
				writer.close();
			} else {
				System.out.println("GET " + getUrl());
			}

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
			final String responseData = sb.toString(); 
			final int statusCode = connection.getResponseCode();
			final String statusText = connection.getResponseMessage();
			
			System.out.println("Received: ");
			System.out.println(responseData);
			
			com.google.gwt.http.client.Response response = new com.google.gwt.http.client.Response() {

				@Override
				public String getHeader(String header) {
					return connection.getHeaderField(header);
				}

				@Override
				public Header[] getHeaders() {
					ArrayList<Header> headers = new ArrayList<Header>();
					for (final Map.Entry<String, List<String>> e : connection.getHeaderFields().entrySet()) {
						for (final String v: e.getValue()) {
						  headers.add(new Header() {

							@Override
							public String getName() {
								return e.getKey();
							}

							@Override
							public String getValue() {
								return v;
							}});	
						}
					}
					Header[] result = new Header[headers.size()];
					return headers.toArray(result);
				}

				@Override
				public String getHeadersAsString() {
					Header[] headers = getHeaders();
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < headers.length; i++) {
						sb.append(headers[i].getName());
						sb.append(": ");
						sb.append(headers[i].getValue());
					}
					return sb.toString();
				}
				

				@Override
				public int getStatusCode() {
					return statusCode;
				}

				@Override
				public String getStatusText() {
					return statusText;
				}
				@Override
				public String getText() {
					return responseData;
					}
					
				};
				callback.onResponseReceived(request, response);
			} catch (MalformedURLException e) {
				throw new RequestException(e);
			} catch (IOException e) {
				callback.onError(request, e);
			}  
		  
		return request;

	  }
	  
	  
	  
}
