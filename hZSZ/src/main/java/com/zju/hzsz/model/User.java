package com.zju.hzsz.model;

import java.util.ArrayList;
import java.util.List;

public class User {
	// public int userId = 3;
	public String userName = null;
	// public String userRealName = "未登录";
	public String uuid = null;

	public List<River> collections = null;
	public List<Integer> readNewsIds = null;

	public String pwdmd5 = null;

	public boolean isLogined() {
		return uuid != null && uuid.length() > 0;
	}

	public List<River> getCollections() {
		if (collections == null) {
			collections = new ArrayList<River>();
		}
		return collections;
	}

	public String getDisplayName() {
		if (isLogined()) {
			return userName;
		} else {
			return "未登录，请点击头像登录或注册";
		}
	}

	public void clearInfo() {
		// userName = null;
		uuid = null;
		pwdmd5 = null;
	}

	// 河长
	public int authority;
	public River[] riverSum = new River[] {};
	public int ifOnJob = 0;

	// 是否河长
	public boolean isChief() {
		return authority == 2 && ifOnJob == 0;
	}

	//是否是村级河长
	public boolean isVillageChief(){
		//村级河长的权限
		return authority == 8 && ifOnJob == 0;
	}

	// end 河长

	public String getDisplayRiver() {
		if (isLogined() && isChief()) {
			if (riverSum == null || riverSum.length == 0) {
				return "";
			} else if (riverSum.length == 1) {
				return "负责河道:" + riverSum[0].riverName;
			} else {
				return "负责河道:" + riverSum[0].riverName + "等" + riverSum.length + "条河道";
			}
		} else {
			return "";
		}

	}

	public List<Integer> getReadNewsIds() {
		if (readNewsIds == null)
			readNewsIds = new ArrayList<Integer>();
		return readNewsIds;
	}

	public void readySave() {
		int max = 100;
		if (readNewsIds != null && readNewsIds.size() > (max * 2)) {
			List<Integer> list = new ArrayList<Integer>();
			int size = readNewsIds.size();
			for (int i = size - max; i < size; ++i) {
				list.add(readNewsIds.get(i));
			}
			readNewsIds = list;
		}
	}

	public boolean gpsdisable = false;
	public boolean notifyable = true;

	public boolean isNotifyable() {
		return this.isLogined() && notifyable;
	}

	public void setNotifyable(boolean ab) {
		notifyable = ab;
	}
}
