package com.zju.hzsz.model;

public class TArrayRes<T> extends BaseRes {
	public T[] data;

	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data != null;
	}
}
