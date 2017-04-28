package com.zju.hzsz.model;

import com.zju.hzsz.activity.BaseActivity;

public class RiverRecord {
	public int recordId;
	public String recordSerNum;
	public DateTime recordDate;

	public int recordPersonAuthority; //巡河人的权限

	public int flotage; // 河面是否存在漂浮物
	public String flotages; // 具体描述
	public int rubbish; // 河岸是否存在垃圾
	public String rubbishs; // 具体描述
	public int building; // 河岸是否存在违章建筑
	public String buildings; // 具体描述
	public int sludge; // 河底是否存在明显淤泥
	public String sludges; // 具体描述
	public int odour; // 水体是否存在臭味
	public String odours; // 具体描述
	public int outfall; // 水体是否存在排污口
	public String outfalls; // 具体描述
	public int riverinplace; // 保洁机制是否到位
	public String riverinplaces; // 具体描述
	public String otherquestion; // 其他问题
	public String deal; // 处理情况

	public String latlist;//显示轨迹纬度数组
	public String lnglist;//显示轨迹经度数组

	public String picPath;	// 图片
	
	public int riverId; // 巡查的河道Id
	public String recordPersonName;
	public String recordRiverName;

	// local
	public River locRiver;
	public String locRiverName;

	public String getYMD2() {
		return recordDate != null ? recordDate.getYMD2(BaseActivity.getCurActivity()) : "";
	}

	public String getYMDHM() {
		return recordDate != null ? recordDate.getYMDHM(BaseActivity.getCurActivity()) : "";
	}
}
