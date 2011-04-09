package com.google.code.infusion.util;

public interface AsyncCallback<T> {
	void onSuccess(T result);
	void onFailure(Throwable error);
}
