package com.google.code.infusion.importer;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class BibtexParser implements TableReader {

	LookAheadReader reader;
	boolean eof;

	static String[] CODES = {
			"#",
			"\\#",
			"$",
			"\\$",
			"&",
			"\\&",
			"_",
			"\\_",
			"^",
			"\\^",
			"%",
			"\\%",

			// "\u00a7", "\\S ",
			// "\u00a9", "\\copyright ",
			// "\u00b6", "\\P ",

			"\u00c0", "\\`{A}", "\u00c1", "\\'{A}", "\u00c2", "\\^{A}",
			"\u00c3", "\\~{A}", "\u00c4", "\\\"{A}", "\u00c4", "{\\\"A}",
			"\u00c5", "{\\AA}", "\u00c6", "{\\AE}", "\u00c7", "\\c{C}",
			"\u00c8", "\\`{E}", "\u00c9", "\\'{E}", "\u00ca", "\\^{E}",
			"\u00cb", "\\\"{E}", "\u00cc", "\\`{I}", "\u00cd", "\\'{I}",
			"\u00ce", "\\^{I}", "\u00cf", "\\\"{I}", "\u00d1", "\\~{N}",
			"\u00d2", "\\`{O}", "\u00d3", "\\'{O}", "\u00d4", "\\^{O}",
			"\u00d5", "\\~{O}", "\u00d6", "\\\"{O}", "\u00d6", "{\\\"O}",
			"\u00d8", "{\\O}", "\u00d9", "\\`{U}", "\u00da", "\\'{U}",
			"\u00db", "\\^{U}", "\u00dc", "\\\"{U}", "\u00dc", "{\\\"U}",
			"\u00dd", "\\'{Y}", "\u00df", "{\\ss}", "\u00e0", "\\`{a}",
			"\u00e1", "\\'{a}", "\u00e2", "\\^{a}", "\u00e3", "\\~{a}",
			"\u00e4", "\\\"{a}", "\u00e4", "{\\\"a}", "\u00e5", "{\\aa}",
			"\u00e6", "{\\ae}", "\u00e7", "\\c{c}", "\u00e8", "\\`{e}",
			"\u00e9", "\\'{e}", "\u00ea", "\\^{e}", "\u00eb", "\\\"{e}",
			"\u00ec", "\\`{\\i}", "\u00ed", "\\'{\\i}", "\u00ee", "\\^{\\i}",
			"\u00ef", "\\\"{\\i}", "\u00f1", "\\~{n}", "\u00f2", "\\`{o}",
			"\u00f3", "\\'{o}", "\u00f4", "\\^{o}", "\u00f5", "\\~{o}",
			"\u00f6", "\\\"{o}", "\u00f6", "{\\\"o}", "\u00f8", "{\\o}",
			"\u00f9", "\\`{u}", "\u00fa", "\\'{u}", "\u00fb", "\\^{u}",
			"\u00fc", "\\\"{u}", "\u00fc", "{\\\"u}", "\u00fd", "\\'{y}",
			"\u00ff", "\\\"{y}" };

	static String[] ENTRY_TYPES = { "article", "book", "booklet", "inbook",
			"inproceedings", "incollection", "manual", "mastersthesis", "misc",
			"proceedings", "techreport", "phdthesis", "unpublished" };

	public BibtexParser(Reader reader) throws IOException {
		this.reader = new LookAheadReader(reader);
	}

	static String replace(String src, String replace, String by) {

		int i = src.indexOf(replace);
		return (i == -1) ? src : (src.substring(0, i) + by + replace(
				src.substring(i + replace.length()), replace, by));
	}

	public static String toUnicode(String s) {

		if (s.indexOf('\\') != -1) {
			for (int i = 0; i < CODES.length; i += 2) {
				s = replace(s, CODES[i + 1], CODES[i]);
			}
		}
		return s;
	}

	public Map<String, Object> readRow() throws IOException {

		if (eof) {
			return null;
		}

		Map<String, Object> result = new HashMap<String, Object>();

		reader.readTo("@<*");
		int i = reader.read();

		if (i == '*') {
			result.put("bibkey", "*" + reader.readTo("\n\r\t "));
			return result;
		}

		if (i != '@') {
			if (i == '<') {
				reader.readTo('>');
				reader.read();
			}

			eof = true;
			return null;
		}

		// mit '<' wird <ende> vom emacs erkannt...
		// if (peek == -1) break; redundant with i != '@'

		StringBuffer type = new StringBuffer();
		StringBuffer id = new StringBuffer();

		type.append(reader.readTo('{'));

		reader.read();

		id.append(reader.readTo(",}"));
		int c = reader.read();

		// System.out.println ("type: " + type.toString () + " id: " +
		// id.toString
		// ().trim ());

		// "eintrag"

		result.put("bibtype", type.toString().trim().toLowerCase());
		result.put("bibkey", id.toString().trim());

		if (c == ',') {
			while (readLine(result)) {
				// System.out.println ("rl");
			}
		}

		// System.out.println ("c");

		return result;
	}

	void recurse(StringBuffer buf) throws IOException {

		while (true) {
			buf.append(reader.readTo("{}"));

			if (reader.read() != '{') {
				break;
			}

			buf.append('{');
			recurse(buf);
			buf.append('}');
		}
	}

	boolean readLine(Map<String, Object> result) throws IOException {
		StringBuffer valueBuf = new StringBuffer();

		String id = reader.readTo("}=").trim().toLowerCase();

		if (reader.read() == '}') {
			return false;
		}

		valueBuf.append(reader.readTo("{,}\""));

		int c = reader.read();

		if (c == '{') {
			recurse(valueBuf);
			valueBuf.append(reader.readTo(",}"));

			c = reader.read();
		} else if (c == '"') {
			while (true) {
				valueBuf.append(reader.readTo("\"\\"));
				int d = reader.read();

				if (d == '"' || d == -1)
					break;

				valueBuf.append((char) d);
				valueBuf.append((char) reader.read());
			}

			reader.readTo(",}");

			c = reader.read();
		}

		String value = valueBuf.toString().trim();

		result.put(id, toUnicode(value));

		return (c == ',');
	}

	public void close() throws IOException {
		reader.close();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
