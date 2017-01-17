package com.zju.hzsz.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.utils.StrUtils;

public class CompFul {
	public boolean isComp = true;
	public String dealTeleNum;
	public String dealResult;
	public DateTime dealTime;
	public String dealPersonName;

	public DateTime compTime;
	public String compSerNum;
	public String compPersonName;
	public String compTeleNum;
	public String compRiverDist;
	public String compContent;
	public String compRiverName;
	public String compTheme;

	public DateTime advTime;
	public String advSerNum;
	public String advPersonName;
	public String advTeleNum;
	public String advRiverDist;
	public String advContent;
	public String advRiverName;
	public String advTheme;

	public String compPicPath;
	public String advPicPath;

	public String dealPicPath;
	
	public String getPicPath() {
		return compPicPath != null ? compPicPath : advPicPath;
	}

	// ex
	public int evelLevel;
	public String evelContent;
	public String addDealResult;

	public int ifAnonymous; // 是否匿名

	public DateTime getTime() {
		return isComp ? compTime : advTime;
	}

	public String getSerNum() {
		return isComp ? compSerNum : advSerNum;
	}

	public String getPersonName() {
		return isComp ? compPersonName : advPersonName;
	}

	public String getTeleNum() {
		return isComp ? compTeleNum : advTeleNum;
	}

	public String getRiverDist() {
		return isComp ? compRiverDist : advRiverDist;
	}

	public String getContent() {
		return StrUtils.trimString(isComp ? compContent : advContent);
	}

	public String getRiverName() {
		return isComp ? compRiverName : advRiverName;
	}

	public String getTheme() {
		return isComp ? compTheme : advTheme;
	}

	public String getDealTimeString() {
		if (dealTime != null) {
			return dealTime.getYMDHM(BaseActivity.getCurActivity());
		}
		return "";
	}

	public String getTimeString() {
		if (getTime() != null) {
			return getTime().getYMDHM(BaseActivity.getCurActivity());
		}
		return "";
	}

	private String[] EVELEVELS = new String[] { "不满意", "一般", "比较满意", "非常满意" };

	public String getEvelLevels() {
		// ResUtils.get
		int s = evelLevel;
		if (s >= 0 && s < EVELEVELS.length) {
			return EVELEVELS[s];
		} else {
			return "";
		}
	}

	static Pattern P_TAG = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);

	public String getDealResultText() {
		Matcher m_script = P_TAG.matcher(dealResult);
		return m_script.replaceAll("");
	}
}
