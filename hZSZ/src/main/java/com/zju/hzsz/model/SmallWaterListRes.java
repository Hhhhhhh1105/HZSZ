package com.zju.hzsz.model;

/**
 * 小微水体列表
 * Created by Wangli on 2017/3/28.
 */

public class SmallWaterListRes extends BaseRes {
    public SmallWaterList data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null && data.smallWaterSums != null;
    }
}
