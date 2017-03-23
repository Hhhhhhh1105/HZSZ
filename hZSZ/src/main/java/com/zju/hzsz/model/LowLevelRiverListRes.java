package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/3/23.
 */

public class LowLevelRiverListRes extends BaseRes {
    public LowLevelRiverList data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null && data.lowLevelRivers != null;
    }
}
