package com.google.code.infusion.datastore;

import java.util.List;

import com.google.code.infusion.util.AsyncCallback;


public interface PreparedQueryAsync {

	void asList(FetchOptions fetchOptions, AsyncCallback<List<Entity>> asyncCallback);

  void countEntities(FetchOptions fetchOptions, AsyncCallback<Integer> asyncCallback);

}
