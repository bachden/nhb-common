package com.nhb.common.async;
public interface Callback<T> {

	void apply(T result);
}
