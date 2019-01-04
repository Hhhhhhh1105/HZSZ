package com.zju.hzsz.model;

import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.utils.ResUtils;

public class PatrolEvent {
	public long id;
	public  User create_user;  //上报人id;
	public  String create_userName;
	public  String riverName;  //上报河道； 表中字段是section_id
	public  Event eventId; //上报事件类型ID
	public  String content;    //上报内容（备注）
	public  DateTime upload_time;  //上报时间
	public  Double longtitude; //经度
	public  Double latitude;   //纬度
	public  String comPicPath; //上报图片
	public  String eventSerNum; //投诉单编号

	//20181211 lyx
	public  Integer type; //河：1 湖：2
	public  Integer ifDeal; //已处理：1 未处理：0

	public  User deal_person; //处理人id
	public  String deal_personName;
	public  DateTime deal_time;  //处理时间
	public  String dealPicPath; //处理结果图片
	public  String dealContent;  //处理结果

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User getCreate_user() {
		return create_user;
	}

	public void setCreate_user(User create_user) {
		this.create_user = create_user;
	}

	public String getCreate_userName() {
		return create_userName;
	}

	public void setCreate_userName(String create_userName) {
		this.create_userName = create_userName;
	}

	public String getRiverName() {
		return riverName;
	}

	public void setRiverName(String riverName) {
		this.riverName = riverName;
	}

	public Event getEventId() {
		return eventId;
	}

	public void setEventId(Event eventId) {
		this.eventId = eventId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public DateTime getUpload_time() {
		return upload_time;
	}

	public void setUpload_time(DateTime upload_time) {
		this.upload_time = upload_time;
	}

	public Double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(Double longtitude) {
		this.longtitude = longtitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public String getComPicPath() {
		return comPicPath;
	}

	public void setComPicPath(String comPicPath) {
		this.comPicPath = comPicPath;
	}

	public String getEventSerNum() {
		return eventSerNum;
	}

	public void setEventSerNum(String eventSerNum) {
		this.eventSerNum = eventSerNum;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getIfDeal() {
		return ifDeal;
	}

	public void setIfDeal(Integer ifDeal) {
		this.ifDeal = ifDeal;
	}

	public User getDeal_person() {
		return deal_person;
	}

	public void setDeal_person(User deal_person) {
		this.deal_person = deal_person;
	}

	public String getDeal_personName() {
		return deal_personName;
	}

	public void setDeal_personName(String deal_personName) {
		this.deal_personName = deal_personName;
	}

	public DateTime getDeal_time() {
		return deal_time;
	}

	public void setDeal_time(DateTime deal_time) {
		this.deal_time = deal_time;
	}

	public String getDealPicPath() {
		return dealPicPath;
	}

	public void setDealPicPath(String dealPicPath) {
		this.dealPicPath = dealPicPath;
	}

	public String getDealContent() {
		return dealContent;
	}

	public void setDealContent(String dealContent) {
		this.dealContent = dealContent;
	}
	public String getTimeString() {
		if (upload_time != null) {
			return upload_time.getYMDHM(BaseActivity.getCurActivity());
		}
		return "";
	}



}
