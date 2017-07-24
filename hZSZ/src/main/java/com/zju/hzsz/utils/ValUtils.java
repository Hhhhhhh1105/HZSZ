package com.zju.hzsz.utils;

import java.util.HashMap;
import java.util.Map;

public class ValUtils {
	private static Map<String, String[]> valMap = null;

	public static String[] getYVals(String ename) {
		if (valMap == null) {
			valMap = new HashMap<String, String[]>();
			valMap.put("DO", new String[] { "12", "7.5", "6", "5", "3", "2", "0" });
			valMap.put("TP", new String[] { "1.0", "0.4", "0.3", "0.2", "0.1", "0.02", "0" });
			valMap.put("NH3N", new String[] { "10.0", "2.0", "1.5", "1.0", "0.5", "0.15", "0" });
			valMap.put("CODMn", new String[] { "20", "15", "10", "6", "4", "2", "0" });
			valMap.put("ORP", new String[] {"1.0", "0.4", "0.3", "0.2", "0.1", "0.02", "0.0"});
			valMap.put("Transp", new String[] {"999", "60", "40", "20", "1", "0.1", "0"});

			valMap.put("NO", new String[] { "-", "-", "-", "-", "-", "-", "-" });
		}
		if (valMap.containsKey(ename))
			return valMap.get(ename);
		else
			return valMap.get("NO");
	}
}
