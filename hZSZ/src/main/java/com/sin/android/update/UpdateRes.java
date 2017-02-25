package com.sin.android.update;

import com.zju.hzsz.model.BaseRes;

/**
 * Created by Wangli on 2017/2/25.
 */

public class UpdateRes extends BaseRes {

    public Update data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null;
    }
}
