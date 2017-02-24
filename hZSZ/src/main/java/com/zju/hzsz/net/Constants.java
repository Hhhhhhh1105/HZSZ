package com.zju.hzsz.net;

/**
 * 通信常量定义
 * 
 * @author Robin+
 * 
 */
public class Constants {
	public static final String SerUrl1 = "http://115.159.53.25:8080"; // test
	public static final String SerUrl2 = "http://220.191.208.69:8080"; // release

	public static final String SerUrl = SerUrl2;

	public static final String ApiUrl = SerUrl2 + "/ShuiHuanJingFabu/background/app.htm";
//	public static final String ApiUrl = SerUrl1 + "/gzhd/background/app.htm";
	public static final String AppKey = "10000";
	public static final String AppSecret = "de85ac6fa475a0391d8c2e4e7413760";

	public static final int DefaultPageSize = 20;
}
