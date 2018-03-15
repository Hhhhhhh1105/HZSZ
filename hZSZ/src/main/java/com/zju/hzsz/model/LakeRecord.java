package com.zju.hzsz.model;

import com.zju.hzsz.activity.BaseActivity;

import java.util.Date;

/**
 * Created by ZJTLM4600l on 2017/12/22.
 */

public class LakeRecord {

    public long lakeId;//巡查湖道Id
    public String recordSerNum;//编号RR-XH-00001

    public int flotage;
    public String flotages;//漂浮物
    public int rubbish;
    public String rubbishs;//垃圾
    public int  building;
    public String buildings;//建筑物
    public int sludge;
    public String sludges;//淤泥
    public int odour;
    public String odours;//臭味
    public int outfall;
    public String outfalls;//排污口
    public int lakeinplace;//湖道长效管理机制和保洁机制是否到位
    public String lakeinplaces;
    public String otherquestion;//其他问题
    public String deal;//处理情况
    public DateTime recordDate;//巡湖日期
    public String picPath;//图片地址

    public String latlist; //经度数组
    public String lnglist; //纬度数组
    public String recordLakeName;
    public String recordPersonName;

    public long recordId;
    public long recordPersonId; //巡湖人的userId
    public int recordPersonAuth;  //巡湖人的authority

    private boolean isCompleted;//是否是已完成的巡湖记录

    // local
    public Lake locLake;
    public String locLakeName;

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getYMD2() {
        return recordDate != null ? recordDate.getYMD2(BaseActivity.getCurActivity()) : "";
    }

    public String getYMDHM() {
        return recordDate != null ? recordDate.getYMDHM(BaseActivity.getCurActivity()) : "";
    }
}
