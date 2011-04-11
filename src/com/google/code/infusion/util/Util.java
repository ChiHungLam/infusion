package com.google.code.infusion.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Util {

	public static String singleQuote(String value) {
		StringBuilder sb = new StringBuilder("\'");
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\'':
				sb.append("\\\'");
				break;
            case '\\':
              sb.append("\\\\");
              break;
			default:
				sb.append(c);
			}
		}
		sb.append('\'');
		return sb.toString();
	}

	
	   public static String doubleQuote(String value) {
	        StringBuilder sb = new StringBuilder("\"");
	        for (int i = 0; i < value.length(); i++) {
	            char c = value.charAt(i);
	            switch (c) {
	            case '\n':
	                sb.append("\\n");
	                break;
	            case '\r':
	                sb.append("\\r");
	                break;
	            case '"':
	                sb.append("\\\"");
	                break;
	            case '\\':
	              sb.append("\\\\");
	              break;
	            default:
	                sb.append(c);
	            }
	        }
	        sb.append('"');
	        return sb.toString();
	    }

	public static String[] parseCsv(String s) {
		ArrayList<String> parts = new ArrayList<String>();
	
		StringBuilder current = new StringBuilder();
		boolean quoted = false;
	
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '"':
				quoted = !quoted;
				break;
			case '\\':
				if (i + 1 < s.length()) {
					current.append(s.charAt(++i));
				} else {
					current.append('\\');
				}
				break;
			case ',':
				if (!quoted) {
					parts.add(current.toString());
					current.setLength(0);
				} else {
					current.append(',');
				}
				break;
			default:
				current.append(c);
			}
	
		}
		parts.add(current.toString());
	
		String[] result = new String[parts.size()];
		parts.toArray(result);
		return result;
	
	}

	public static String urlEncode(String url) {
		try {
			return URLEncoder.encode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
