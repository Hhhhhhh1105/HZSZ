package com.zju.hzsz.model;


public class News {

	public String picPath;
	public int id;
	public DateTime updateTime;
	public String theme;
	public String abstarct;
	public int type;
	public String creatorname;
	public DateTime release_time;
	public DateTime update_time;
	public String content;
	public String rePeople;

	public boolean isReaded(User u) {
		return u != null && u.getReadNewsIds().contains(Integer.valueOf(id));
	}

	public void setReaded(User u) {
		if (isReaded(u))
			return;
		u.getReadNewsIds().add(Integer.valueOf(id));
	}
}
