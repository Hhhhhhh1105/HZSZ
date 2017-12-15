package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/7/26.
 */

public class Lake {

    public long lakeId;
    public String lakeName;
    public int lakeLevel;
    public int waterType;
    private String lakePicPath;
    private boolean ifCare;
    private long districtId;
    private String districtName; //所属区域名字
    private String lakeSerialNum;//编号

    public void setLakePicPath(String lakePicPath) {
        this.lakePicPath = lakePicPath;
    }

    public void setIfCare(boolean ifCare) {
        this.ifCare = ifCare;
    }

    public void setDistrictId(long districtId) {
        this.districtId = districtId;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public void setLakeSerialNum(String lakeSerialNum) {
        this.lakeSerialNum = lakeSerialNum;
    }

    public String getLakePicPath() {
        return lakePicPath;
    }

    public boolean isIfCare() {
        return ifCare;
    }

    public long getDistrictId() {
        return districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public String getLakeSerialNum() {
        return lakeSerialNum;
    }
}
