package com.zju.hzsz;

import com.zju.hzsz.model.District;

public class Values {
//	final public static String Ver = "1.3.11";
	final public static String Ver = "1.3.2";
	final public static boolean DEBUG = false;
	final public static boolean RELEASE = false;
	public static String LastVer = null;

	public static long lastAuthCodeTime = 0;

	final public static int AUTHCODE_GAP_S = 60;

	public static District[] districtLists;

	final public static String SMS_APPKEY = "7d9145a2169c";
	final public static String SMS_SECKEY = "dc1872330efab0b854bd7d7cc95a76f2";

	private static String[] WXAppIDs = new String[] { "wx7c34b978e3add950", "wx2997236d4d70a3aa" };
	private static String[] WXAppSecrets = new String[] { "bb00b86efe44f194380d40eb001cd0b4", "08e8bc26bc021917d9caf2082931fa58" };

	private static int ix = 1;

	public static int UPLOAD_IMG_W = 400;
	public static int UPLOAD_IMG_H = 400;

	public static int MAP_ZOOM_LEVEL = 16;
	public static int MAP_ZOOM_MAX_LEVEL = 20;
	public static int MAP_ZOOM_MIN_LEVEL = 14;

	public static int getIx() {
		return RELEASE ? 1 : ix;
	}

	public static String getWXAppID() {
		return WXAppIDs[getIx()];
	}

	public static String getWXAppSecret() {
		return WXAppSecrets[getIx()];
	}
}
