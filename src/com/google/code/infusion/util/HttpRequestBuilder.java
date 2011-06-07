package com.google.code.infusion.util;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class HttpRequestBuilder {

	public static final String GET = "GET";
	public static final String POST = "POST";
	
	String method;
	String url;
	ArrayList<String[]> headers = new ArrayList<String[]>();
	String data;
	OAuthToken token;
	
	public HttpRequestBuilder(String method, String url) {
		this.method = method;
		this.url = url;
	}
	
	public HttpRequestBuilder setHeader(String name, String value) {
		headers.add(new String[] {name, value});
		return this;
	}
	
	public void send(AsyncCallback<HttpResponse> callback) {
		new HttpResponse(this, callback);
	}
	
	public void setOAuthToken(OAuthToken token) {
	  this.token = token;
	}

	public HttpRequestBuilder setData(String data) {
		this.data = data;
		return this;
	}
}
