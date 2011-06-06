package com.google.gwt.user.client.rpc;

public interface AsyncCallback<T> {
	void onSuccess(T result);
	void onFailure(Throwable error);
}
