package com.zju.hzsz.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ParamUtils {
	public static JSONObject pageParam(int pageSize, int currentPage) {
		return pageParam(pageSize, currentPage, null);
	}

	public static JSONObject pageParam(int pageSize, int currentPage, JSONObject o) {
		if (o == null)
			o = new JSONObject();
		try {
			o.put("pageSize", pageSize);
			o.put("currentPage", currentPage);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	public static JSONObject freeParam(JSONObject raw, Object... args) {
		if (raw == null)
			raw = new JSONObject();
		for (int i = 0; i < args.length; i += 2) {
			try {
				raw.put((String) args[i], args[i + 1]);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return raw;
	}
}
