package com.zju.hzsz.model;

public class RiverListRes extends BaseRes {
	public River[] data;

	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data != null;
	}
}
