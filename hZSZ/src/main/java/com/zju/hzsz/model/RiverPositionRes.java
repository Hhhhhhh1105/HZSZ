package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/4/3.
 */

public class RiverPositionRes extends BaseRes {

    public RiverPosition data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null;
    }

}
