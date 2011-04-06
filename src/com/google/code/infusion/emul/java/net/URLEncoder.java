package java.net;

public class URLEncoder {
	public static String encode(String url, String charset) throws java.io.UnsupportedEncodingException {
		return com.google.gwt.http.client.URL.encode(url);
	}
  
}