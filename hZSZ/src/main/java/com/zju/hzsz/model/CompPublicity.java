package com.zju.hzsz.model;

import com.zju.hzsz.activity.BaseActivity;

public class CompPublicity {
	public int compId;
	public String compTheme;
	public int compStatus;
	public String compRiverDistrict;
	public DateTime compTime;
	public String compNum;
	public String compPicPath;
	public String compContent;
	public String compRiverName;

	public int complaintsId;
	public String complaintsNum;
	public String complaintsContent;
	public String complaintsPicPath;

	public double compLong;
	public double compLat;

	//得到年月日形式的字符串
	public String getDateTime() {
		return compTime != null ? compTime.getYMDHM(BaseActivity.getCurActivity()) : "";
	}

	//如果compStatus的值>=3，则为已处理
	public boolean isHandled() {
		return compStatus >= 3;
	}

	public String getStatus() {
		return isHandled() ? "已处理" : "未处理";
	}

	public String getCompPicPath() {
		String s = getPicFulPath();
		String p = s == null ? null : s.split(";").clone()[0].trim();
		return p;
	}

	public String getContent() {
		return compContent == null ? (complaintsContent == null ? "" : complaintsContent) : compContent;
	}

	public String getPicFulPath() {
		return compPicPath == null ? (complaintsPicPath == null ? null : complaintsPicPath) : compPicPath;
	}

	//获取编号
	public String getCompNum() {
		return complaintsNum == null ? (compNum == null ? null : compNum) : complaintsNum;
	}

	public int getId() {
		return compId == 0 ? complaintsId : compId;
	}
}
