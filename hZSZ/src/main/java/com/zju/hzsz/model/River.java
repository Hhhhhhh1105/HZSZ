package com.zju.hzsz.model;

import android.content.DialogInterface;

import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.utils.StrUtils;

/***
 * 河道
 * 
 * @author Robin
 * 
 */
public class River {
	public int riverId;
	public int waterType;
	public String riverName;
	public int riverType;
	public int riverSort;
	public int riverLevel;

	public String picPath;

	public DateTime uploadTime;

	public String endingPoint; //终点
	public String districtName;
	public int districtId;
	public String riverSerialNum;
	public String startingPoint; //起点
	public String target;
	public String riverLength;
	public String responsibility;

	public String riverPicPath;

	public int ifPiecewise; // 是否分段，1是，0不是

	public double longtitude;
	public double latitude;

	// 人

	public RiverUser districtRiverChief; /* 区级河长 */
	public RiverUser districtComtactPeo; /* 区级河长联系人 */

	public TownRiverUser[] townRiverChiefs; /* 镇街河长 */
	public TownRiverUser[] townRiverSheriffs; /* 镇街河长联系人 */

	public RiverUser riverSheriff; /* 河道警长 */
	public RiverUser districtRiverSheriff; /* 区警长 */
	public ContactDepartment comtactDepartment; /* 联系部门 */

	public LowLevelRiver[] lowLevelRivers; //下属河道
	public VillageRiverChief[] villageRiverChiefs; //下属村级河长列表

	//
	public Station[] stations;
	public SectionIndex[] indexs;

	//
	 public boolean ifCare; /* 是否关注 */

	public boolean isCared(User u) {
		boolean s = u.getCollections().contains(this);

		// Log.e("TT", this.riverId + " " + s);
		return s;
	}

	public void setCared(User u, boolean c) {
		if (c) {
			u.getCollections().add(this);
		} else {
			u.getCollections().remove(this);
		}
	}

	public boolean isPiecewise() {
		return ifPiecewise != 0;
	}

	public String getImgUrl() {
		return StrUtils.isNullOrEmpty(picPath) ? riverPicPath : picPath;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof River) {
			return ((River) o).riverId == this.riverId;
		} else {
			return false;
		}
	}

	public void toggleCare(final BaseActivity context, final BaseActivity.BooleanCallback callback) {
		if (isCared(context.getUser())) {
			if (context.getUser().getCollections().size() > 1) {
				context.createMessageDialog("提示", "将取消对该电子公示牌的收藏。同时会在我的收藏列表中删除", "好的", "取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						setCared(context.getUser(), false);
						if (callback != null) {
							callback.callback(false);
						}
					}
				}, null, null).show();
			} else {
				context.showToast("请至少收藏一个电子公示牌!");
			}
		} else {
			setCared(context.getUser(), true);
			context.createMessageDialog("提示", "收藏成功", "确定", null, null, null, null).show();
			if (callback != null) {
				callback.callback(true);
			}
		}
	}
}
