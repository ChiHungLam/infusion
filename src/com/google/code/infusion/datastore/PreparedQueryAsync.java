package com.google.code.infusion.datastore;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PreparedQueryAsync {

	void asList(AsyncCallback<List<Entity>> callback);
	
}
