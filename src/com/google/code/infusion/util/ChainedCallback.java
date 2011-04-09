package com.google.code.infusion.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class ChainedCallback<T> implements AsyncCallback<T> {

	private final AsyncCallback<?> callback;

	public ChainedCallback(final AsyncCallback<?> callback) {
		this.callback = callback;
	}
	
	public final void onFailure(Throwable caught) {
		callback.onFailure(caught);
	}
}
