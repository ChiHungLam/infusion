package com.google.code.infusion.importer;

import java.io.IOException;
import java.util.Map;

public interface TableReader {

	public Map<String,Object> readRow() throws IOException;
}
