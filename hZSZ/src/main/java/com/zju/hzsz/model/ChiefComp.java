package com.zju.hzsz.model;

import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.utils.ResUtils;

public class ChiefComp {
	public String compId;
	public DateTime compDate;
	public String compSerNum;
	public String compTheme;
	public String compContent;

	public String advId;
	public DateTime advDate;
	public String advSerNum;
	public String advTheme;
	public String advContent;

	public String complaintsPicPath;





	// public int dubanStatus;
	public int dealStatus;

	// public int dubanId;

	public boolean isComp() {
		return compId != null;
	}

	public String getId() {
		return isComp() ? compId : advId;
	}

	public DateTime getDate() {
		return isComp() ? compDate : advDate;
	}

	public String getSerNum() {
		return isComp() ? compSerNum : advSerNum;
	}

	public String getContent() {
		return isComp() ? compContent : advContent;
	}

	public String getTheme() {
		return isComp() ? compTheme : advTheme;
	}

	public int getStatus() {
		return dealStatus;
	}

	public String getStatuss() {
		return BaseActivity.getCurActivity() != null ? BaseActivity.getCurActivity().getString(ResUtils.getHandleStatus(getStatus())) : "";
	}

	public boolean isHandling() {
		return dealStatus == 2;
	}

	public boolean isAddingHandle() {
		return dealStatus == 5;
	}
}
