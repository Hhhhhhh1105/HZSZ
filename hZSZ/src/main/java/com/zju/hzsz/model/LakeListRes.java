package com.zju.hzsz.model;

/**
 * 湖泊列表
 * Created by Wangli on 2017/8/1.
 */

public class LakeListRes extends BaseRes {

    public LakeList data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null && data.lakeList != null;
    }
}
