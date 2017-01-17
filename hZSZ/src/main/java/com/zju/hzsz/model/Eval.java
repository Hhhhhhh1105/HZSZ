package com.zju.hzsz.model;

public class Eval {
	public int evalLevel;
	public String evalContent;
	
	private String[] EVELEVELS = new String[] { "不满意", "一般", "比较满意", "非常满意" };

	public String getEvelLevels() {
		// ResUtils.get
		int s = evalLevel;
		if (s >= 0 && s < EVELEVELS.length) {
			return EVELEVELS[s];
		} else {
			return "";
		}
	}
}
