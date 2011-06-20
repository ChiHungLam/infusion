package com.google.gwt.user.client.rpc;

/**
 * Simple interface for asnchrnous callbacks provided for GWT
 * compatibilty (matches the corresponding GWT interface).
 */
public interface AsyncCallback<T> {
  /**
   * Called on success.
   */
  void onSuccess(T result);
  /**
   * Called on failure.
   */
  void onFailure(Throwable error);
}
