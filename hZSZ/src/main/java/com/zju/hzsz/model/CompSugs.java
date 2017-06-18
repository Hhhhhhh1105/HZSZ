package com.zju.hzsz.model;

import com.zju.hzsz.utils.StrUtils;

public class CompSugs {
	public String compTheme;
	public int compStatus;
	public int complaintsId;
	public String complaintsNum;
	public String complaintsContent;
	public String complaintsPicPath;

	public int adviseId;
	public String adviseNum;
	public String adviseContent;
	public DateTime releaseTime;
	public int advStatus;
	public String advTheme;
	public String advicePicPath;

	public int compPersonId; //投诉人的Id

	public int isRead; //代表投诉河长是否已阅

	public boolean isComp() {
		return complaintsId != 0;
	}

	public int getId() {
		return isComp() ? complaintsId : adviseId;
	}

	public int getStatus() {
		return isComp() ? compStatus : advStatus;
	}

	private final String[] STATUSES = new String[] { "待受理", "已受理", "已处理", "已评价" };

	public String getStatuss() {
		int s = getStatus();
		--s;
		if (s >= 0 && s < STATUSES.length) {
			return STATUSES[s];
		} else {
			return "未知";
		}
	}

	public String getNum() {
		return isComp() ? complaintsNum : adviseNum;
	}

	public DateTime getDateTime() {
		return releaseTime;
	}

	public String getContent() {
		return StrUtils.trimString(isComp() ? complaintsContent : adviseContent);
	}

	public String getTheme() {
		return isComp() ? compTheme : advTheme;
	}

	public String getPicPath() {
		return isComp() ? complaintsPicPath : advicePicPath;
	}
}
