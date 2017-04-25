package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/4/25.
 */

public class NpcSugListRes extends BaseRes {

    public NpcSugList data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null && data.deputySuperviseJsons != null;
    }
}
