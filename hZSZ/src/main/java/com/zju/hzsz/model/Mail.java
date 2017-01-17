package com.zju.hzsz.model;

import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.utils.StrUtils;

public class Mail {
	public long id;
	public int ifRead;
	public String theme;
	public String picPath;
	public DateTime updateTime;

	public String creatorname;
	public DateTime update_time;
	public DateTime release_time;
	public String content;

	public String getYMDHM() {
		DateTime dt = updateTime != null ? updateTime : (update_time != null ? update_time : release_time);
		return dt != null ? dt.getYMDHM(BaseActivity.getCurActivity()) : "";
	}

	public String getContentText() {
		return StrUtils.trimString(content);
	}

	public String getStatusText() {
		return !isReaded() ? "未签收" : "已签收";
	}

	public boolean isReaded() {
		return ifRead != 0;
	}
}
