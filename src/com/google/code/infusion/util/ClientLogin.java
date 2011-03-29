package com.google.code.infusion.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ClientLogin {

	public static void requestAuthToken(String email, String password, final AsyncCallback<String> callback) {
		String data;
		try {
			data = "accountType=GOOGLE&Email=" + URLEncoder.encode(email, "utf-8") + 
			"&Passwd=" + URLEncoder.encode(password, "utf-8") + 
			"&service=fusiontables&source=GoogleCodeProjectInfusion-Infusion-0.1\r\n";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		new HttpRequest("https://www.google.com/accounts/ClientLogin").setData(data).request(new ChainedCallback<HttpRequest.Response>(callback) {
			@Override
			public void onSuccess(HttpRequest.Response response) {
				for (String line : response.getData().split("\n")) {
					if (line.startsWith("Auth=")) {
						callback.onSuccess(line.substring(5));
						return;
					}
				}
				callback.onFailure(new RuntimeException("Auth token not found in servr response"));
			}
		});
	}
	
}
