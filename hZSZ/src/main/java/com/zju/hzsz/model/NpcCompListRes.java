package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/4/25.
 */

public class NpcCompListRes extends BaseRes {

    public NpcCompList data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null && data.complaints != null;
    }
}
