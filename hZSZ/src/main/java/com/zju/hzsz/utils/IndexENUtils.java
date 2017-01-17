package com.zju.hzsz.utils;

import java.util.HashMap;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;

public class IndexENUtils {
	public static HashMap<String, SpannableString> enMap = null;

	public static CharSequence getString(String en) {
		if (enMap == null) {
			enMap = new HashMap<String, SpannableString>();
			float xb = 0.7f;
			SpannableString ss = null;
			ss = new SpannableString("CODMn");
			ss.setSpan(new RelativeSizeSpan(xb), 3, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			enMap.put("CODMn", ss);

			ss = new SpannableString("NH3-N");
			ss.setSpan(new RelativeSizeSpan(xb), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			enMap.put("NH3N", ss);
			enMap.put("NH3-N", ss);
		}
		if (enMap.containsKey(en)) {
			return enMap.get(en);
		} else {
			return en;
		}
	}
}
