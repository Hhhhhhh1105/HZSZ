package com.zju.hzsz.model;

public class StatusRes extends TObjectRes<Status> {
	@Override
	public boolean isSuccess() {
		return super.isSuccess() && this.data.actionStatus;
	}
}
