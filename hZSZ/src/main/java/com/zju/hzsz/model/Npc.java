package com.zju.hzsz.model;


/**
 * 人大代表
 * Created by Wangli on 2017/4/24.
 */

public class Npc {
    public int deputyId;  //人代
    public String name;   //人代姓名
    public int districtId;  //所属区县
    public String position;  //管理等级
    public int riverId;  //管理河道
    public String mobilephone;   //联系方式
    public int lastRecordDays;   //上一次巡河是几天前
    public int lastRecordHours; //上一次巡河是几小时前
    public String riverName;   //管理河道名字
    public String districtName; //所属区划
    public int authority; //代表权限
}
