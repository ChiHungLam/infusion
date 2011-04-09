package com.google.code.infusion.importer;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Haustein
 */
public class CsvParser implements TableReader {

    static final Object EOL = new Object();
    static final Object EOF = new Object();

    protected LookAheadReader reader;
    protected char commentsChar;
    protected String nullValue;
    private ArrayList<String> columnNames;
    
    
	/** 
	 * If there is no header row, it is necessary to read the first data row in order to determine the column count.
	 * In that case, the content of the first row is stored in the variable row0.
	 */
       
    public CsvParser(Reader reader, boolean titleLine) throws IOException {
        this.reader = new LookAheadReader(reader);

        if (titleLine) {
            while (true) {
                Object o = readColumn();
                if (o == EOL || o == EOF) {
                    break;
                }

                columnNames.add(o.toString());
            }
        }
    }

    protected CsvParser(LookAheadReader reader) {
        this.reader = reader;
    }

    public Map<String,Object> readRow() throws IOException {

    	Map<String,Object> result = new HashMap<String,Object>();
        
        Object val = null;
        int i = 0;
        while (true) {
            val = readColumn();
            if (i == 0 && val == EOF) {
                return null;
            }
            if (val == EOL || val == EOF) {
                break;
            }

            result.put(i < columnNames.size() ? columnNames.get(i) : ("col" + i), val);
            i++;
        }
        return result;
    }


	void skip()throws IOException{
		while (reader.peek(0)== ' ' || reader.peek(0)== '\t')
			reader.read();
	}

    Object readColumn() throws IOException {

        while (reader.peek(0) == commentsChar) reader.readLine();

		String str;
        Object result;
        //reader.skip(" \t");
        skip();
        int c = reader.peek(0);
        switch (c) {
            case '"' : 
                result = readQuoted();
                //reader.skip(" \t");
                skip();
                if (reader.peek(0) == ',') reader.read();
                return result;
                
            case '\r' :
                if (reader.peek(0) == '\n')
                    reader.read();
            case '\n' :
                reader.read();
                return EOL;
            case -1 :
                return EOF;
            case ',' :
                reader.read();
                return null;
            default:
            	str = reader.readTo(",\n\r").trim();
            	result = str.length() == 0 || str.equals(nullValue) 
            		? null : str;
                if (reader.peek(0) == ',') reader.read();
                return result;
        }



    }

    String readQuoted() throws IOException {
        reader.read();

        String result = reader.readTo('"');
        reader.read();
        if (reader.peek(0) != '"')
            return result;
        StringBuffer buf = new StringBuffer(result);

        do {
            buf.append((char) reader.read()); // consume pending quote
            buf.append(reader.readTo('"'));
            reader.read(); // consume first quote
        }
        while (reader.peek(0) == '"');

        return buf.toString();
    }

    public void close() throws IOException {
    	reader.close();
    }

}
