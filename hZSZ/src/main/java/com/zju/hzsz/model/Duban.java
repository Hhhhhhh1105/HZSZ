package com.zju.hzsz.model;

import com.zju.hzsz.activity.BaseActivity;

public class Duban extends ChiefComp{
//	public int compId; // 投诉单Id
//	public String compSerNum; // 投诉单编号
//	public String compContent; // 投诉内容
//	public DateTime compDate; // 投诉时间
//	public int dealStatus; // 处理状态
	
	public int dubanStatus; // 督办单状态
	public int dubanId; // 督办单Id

	public String dubanSerNum; // 督办单编号
	public String respPeoName; // 责任河长姓名
	public DateTime dubanTime; // 督办单创建时间
	public String content; // 督办事项
	public String creatorName; // 督办单创建人姓名
	public String creatorPhone; // 督办单创建人电话
	public String creatorDepart; // 督办单创建人部门职务

	public String chiefResponse; // 河长反馈意见
	public String riverOfficeResult; // 河长办反馈意见
	public String mark; // 备注
	public int dbStatus; // 督办单状态

	public String getYMDHM() {
		return compDate != null ? compDate.getYMDHM(BaseActivity.getCurActivity()) : "";
	}

	public String getDBYMDHM() {
		return dubanTime != null ? dubanTime.getYMDHM(BaseActivity.getCurActivity()) : "";
	}
}
