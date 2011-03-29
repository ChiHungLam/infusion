package com.google.code.infusion.util;

import java.util.ArrayList;


/** 
 * Parses a CSV String to a String array.
 */

public class CsvParser {
	public static String[] parse(String s) {
		ArrayList<String> parts = new ArrayList<String>();
		
		StringBuilder current = new StringBuilder();
		boolean quoted = false;
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch(c) {
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
}
