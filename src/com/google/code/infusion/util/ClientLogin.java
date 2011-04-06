package com.google.code.infusion.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ClientLogin {

	public static void requestAuthToken(String email, String password,
			final AsyncCallback<String> callback) {
		String data;
		try {
			data = "accountType=GOOGLE&Email="
					+ URLEncoder.encode(email, "utf-8")
					+ "&Passwd="
					+ URLEncoder.encode(password, "utf-8")
					+ "&service=fusiontables&source=GoogleCodeProjectInfusion-Infusion-0.1\r\n";
			
			new RequestBuilder(RequestBuilder.POST, "https://www.google.com/accounts/ClientLogin").sendRequest(data, 
					new RequestCallback() {
				public void onResponseReceived(Request request,
								Response response) {
							for (String line : response.getText().split("\n")) {
								if (line.startsWith("Auth=")) {
									callback.onSuccess(line.substring(5));
									return;
								}
							}
							callback.onFailure(new RuntimeException(
									"Auth token not found in servr response"));
						}

						public void onError(Request request, Throwable exception) {
							callback.onFailure(exception);
						}
					});
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
	}

}
