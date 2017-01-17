package com.zju.hzsz.model;

import com.zju.hzsz.utils.StrUtils;

public class LoginRes extends TObjectRes<User> {
	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data != null && !(StrUtils.isNullOrEmpty(data.uuid));
	}
}
