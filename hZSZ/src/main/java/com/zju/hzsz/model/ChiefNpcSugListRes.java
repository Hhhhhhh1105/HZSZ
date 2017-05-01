package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/5/1.
 */

public class ChiefNpcSugListRes extends BaseRes {

    public ChiefNpcSugList data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null && data.deputySuperviseSum != null;
    }
}
