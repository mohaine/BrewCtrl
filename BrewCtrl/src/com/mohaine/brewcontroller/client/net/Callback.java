package com.mohaine.brewcontroller.client.net;

public interface Callback<T> {

	public void onSuccess(T result);

	public void onNotSuccess(Throwable e);
}
