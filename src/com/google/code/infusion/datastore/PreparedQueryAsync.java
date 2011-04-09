package com.google.code.infusion.datastore;

import java.util.List;

import com.google.code.infusion.util.AsyncCallback;


public interface PreparedQueryAsync {

	void asList(AsyncCallback<List<Entity>> callback);

}
