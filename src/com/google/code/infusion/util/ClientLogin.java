package com.google.code.infusion.util;

public class ClientLogin {
	public static final String ACCOUNT_TYPE_GOOGLE = "GOOGLE";
	
	public static void requestAuthToken(String accountType, String email, String password, String service, String source,
			final AsyncCallback<String> callback) {
		String data;

		data = "accountType=" + Util.urlEncode(accountType) + 
			"&Email=" + Util.urlEncode(email) + 
			"&Passwd=" + Util.urlEncode(password) +
			"&service=" + Util.urlEncode(service) + 
			"&source=" + Util.urlEncode(source) + "\r\n";
		
		HttpRequest request = new HttpRequest(HttpRequest.POST, "https://www.google.com/accounts/ClientLogin");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setData(data);
		request.send(new ChainedCallback<HttpResponse>(callback) {
			public void onSuccess(HttpResponse response) {					
				for (String line : response.getData().split("\n")) {
					if (line.startsWith("Auth=")) {
						callback.onSuccess(line.substring(5));
						return;
					}
				}
				callback.onFailure(new RuntimeException("Auth token not found in server response. Status code: " + response.getStatusCode() + " " + response.getStatusText()));
			}
		});
	}

}
