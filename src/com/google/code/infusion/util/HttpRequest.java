package com.google.code.infusion.util;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class HttpRequest {

	public static final String GET = "GET";
	public static final String POST = "POST";
	
	String method;
	String url;
	ArrayList<String[]> headers = new ArrayList<String[]>();
	String data;
	
	public HttpRequest(String method, String url) {
		this.method = method;
		this.url = url;
	}
	
	public HttpRequest setHeader(String name, String value) {
		headers.add(new String[] {name, value});
		return this;
	}
	
	public void send(AsyncCallback<HttpResponse> callback) {
		new HttpResponse(this, callback);
	}

	public HttpRequest setData(String data) {
		this.data = data;
		return this;
	}
}
