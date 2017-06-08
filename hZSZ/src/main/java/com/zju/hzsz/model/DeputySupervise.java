package com.zju.hzsz.model;

import com.zju.hzsz.activity.BaseActivity;

/**
 * 人大-监督河长
 * Created by Wangli on 2017/4/25.
 */

public class DeputySupervise {

    public int advId;  //建议单id
    public String advSerNum;  //建议单编号
    public String advContent;  //建议单内容
    public DateTime advTime;   //建议时间
    public int advStatus;  //建议单状态
    public String advTheme;
    public String advPersonName;
    public String advTeleNum;
    public String advRiverName;
    public String advRiverDist;

    public String advPerson;
    public DateTime advDate;

    public String dealPersonName;  //处理人
    public String dealTeleNum;
    public DateTime dealTime;

    public int chiefPatrol;
    public int chiefFeedBack;
    public int chiefWork;

    public String dealResult;
    public String evelContent;

    public int isRead;  //是否已阅

    //得到年月日形式的字符串
    public String getDateTime() {
        return advTime != null ? advTime.getYMDHM(BaseActivity.getCurActivity()) : "";
    }

    public String getYMD2() {
        return advTime != null ? advTime.getYMD2(BaseActivity.getCurActivity()) : "";
    }

    public String getAdvDateTime() {
        return advDate != null ? advDate.getYMDHM(BaseActivity.getCurActivity()) : "";
    }

    public String getAdvYMD2() {
        return advDate != null ? advDate.getYMD2(BaseActivity.getCurActivity()) : "";
    }

}
