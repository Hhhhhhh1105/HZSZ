package com.zju.hzsz.model;

public class RiverQualityDataRes extends TObjectRes<RiverQualityData> {

	@Override
	public boolean isSuccess() {
		return super.isSuccess() && data.indexValues != null;
	}

}
