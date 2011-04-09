package com.google.code.infusion.util;

import com.google.gwt.http.client.RequestBuilder.Method;

public class RequestBuilder extends com.google.gwt.http.client.RequestBuilder {

	public RequestBuilder(Method httpMethod, String url) {
		super(httpMethod, url);
	}
}