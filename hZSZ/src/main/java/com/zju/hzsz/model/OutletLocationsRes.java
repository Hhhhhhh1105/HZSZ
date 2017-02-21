package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/2/21.
 */

public class OutletLocationsRes extends TObjectRes<OutletLocations>{

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && this.data != null && this.data.industrialPortJsons != null;
    }
}
