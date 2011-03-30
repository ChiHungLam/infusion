package com.google.code.infusion.util;

public class Util {

	public static String quote(String value) {
		StringBuilder sb = new StringBuilder("\"");
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch(c) {
			case '\n': 
				sb.append("\\n");
				break;
			case '\r': 
				sb.append("\\r");
				break;
			case '"': 
				sb.append("\\\"");
				break;
			default:
				sb.append(c);
			}
		}
		sb.append('"');
		return sb.toString();
	}

}
