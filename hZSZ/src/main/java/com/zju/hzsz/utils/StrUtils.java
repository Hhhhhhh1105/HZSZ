package com.zju.hzsz.utils;

import android.content.Context;

import com.google.gson.Gson;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {

	//用于创建格式化的字符串
	public static String renderText(Context context, int strid, Object... args) {
		String s = context.getText(strid).toString();
		//使用指定的语言环境，制定字符串格式和参数生成格式化的字符串
		return String.format(Locale.getDefault(), s, args);
	}

	private static Pattern imgP = Pattern.compile("src=\"([^\"]+)\"");

	public static String getImgUrl(String s) {
		if (s == null)
			return null;

		if (s.indexOf(':') > 0 && s.indexOf(':') < 5 && s.indexOf("\\ShuiHuanJingFabu") > 0) {
			// C:\apache-tomcat-7.0.61\webapps\ShuiHuanJingFabu\resource\images\1436413660818.jpg
			return getImgUrl(s.substring(s.indexOf("\\ShuiHuanJingFabu")).replace("\\", "/"));
		}

		Matcher m = imgP.matcher(s);
		if (m.find()) {
			s = m.group(1);
		}

		if (s != null && s.length() > 0 && !s.endsWith("/")) {
			if (s.startsWith("http"))
				return s;
			else
				return com.zju.hzsz.net.Constants.SerUrl + (s.startsWith("/") ? "" : "/") + s;
		}

		return null;
	}

	private static Gson gson = new Gson();
	//序列化
	public static String Obj2Str(Object o) {
		return gson.toJson(o);
	}
	//反序列化
	public static <T> T Str2Obj(String s, Class<T> classOfT) {
		return gson.fromJson(s, classOfT);
	}

	public static String float2Str(float v) {
		// return "" + (((int) (v * 1000)) / 1000.0);
		return String.format(Locale.getDefault(), "%.03f", v);
	}

	public static String floatS2Str(String vs) {
		try {
			return float2Str(Float.parseFloat(vs));
		} catch (Exception e) {
			e.printStackTrace();
			return "-";
		}
	}

	public static boolean isNullOrEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	static Pattern P_TAG = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
	public static String trimString(String s){
		if (s != null) {
			Matcher m_script = P_TAG.matcher(s.replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n"));
			return m_script.replaceAll("").replace("&nbsp;", " ");
		} else {
			return null;
		}
	}
}
