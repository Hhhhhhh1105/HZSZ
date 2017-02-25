package com.sin.android.update;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.zju.hzsz.net.Constants;

public class ToolBox {
	public interface CheckCallback {
		public void onNewVersion(Update update);
	}

	private Context context;
	private static String BaseUrl = "http://tool.inruan.com/";
	private static String CheckUrl = BaseUrl + "update/";
	private static String NewCheckUrl = Constants.ApiUrl +
			"?app_sign=7efe0448e727f39874cd57685029c729&timestamp=1423637973&method=Get_Update_Info&app_key=10000";

	public ToolBox(Context context) {
		this.context = context;
	}

	// 1.0.00
	private static long getVerInt(String v) {
		v = v.replace("V", "").replace("v", "");
		long vl = 0;
		String vs[] = v.split("\\.");
		long ps[] = new long[] { 10000, 100, 1 };
		for (int i = 0; i < vs.length && i < 3; ++i) {
			try {
				vl += ps[i] * Long.parseLong(vs[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return vl;
	}

	public static boolean isNewVer(String nv, String cv) {
		return getVerInt(nv) > getVerInt(cv);
	}

	public void checkUpdate(String app, final String curver) {
		checkUpdate(app, curver, true, null);
	}

	public void checkUpdate(String app, final String curver, final boolean autoTip, final CheckCallback callback) {
		RequestQueue rq = Volley.newRequestQueue(context);
		//http://tool.inruan.com/update/hzsz.json?ver=1.3.11&_=1487939212656
		//CheckUrl + app + ".json?ver=" + curver + "&_=" + System.currentTimeMillis()
		StringRequest stringRequest = new StringRequest(Request.Method.GET, NewCheckUrl, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Gson gson = new Gson();
				Log.e("checkUpdate", response);
				final Update update = gson.fromJson(response, UpdateRes.class).data;
				// if (update == null || update.getLast() == null ||
				// update.getLast().length() == 0 ||
				// update.getLast().equals(curver)) {
				if (update == null || update.getLast() == null || update.getLast().length() == 0 || !isNewVer(update.getLast(), curver)) {
					//
					if (callback != null) {
						callback.onNewVersion(update);
					}
					return;
				}
				if (!update.getUrl().startsWith("http://") && !update.getUrl().startsWith("https://"))
					update.setUrl(BaseUrl + update.getUrl());
				if (autoTip) {
					AlertDialog.Builder ab = new AlertDialog.Builder(context);
					ab.setTitle("发现新版本 " + update.getLast());
					ab.setMessage(update.getExplain());
					ab.setPositiveButton("更新", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							Uri uri = Uri.parse(update.getUrl());
							Intent intent = new Intent(Intent.ACTION_VIEW, uri);
							context.startActivity(intent);
						}
					});
					ab.setNegativeButton("忽略", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});
					ab.setCancelable(false);
					ab.create().show();
				}
				if (callback != null) {
					callback.onNewVersion(update);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("checkUpdate", error.toString());
				if (callback != null) {
					callback.onNewVersion(null);
				}
			}
		}) {
		};
		stringRequest.setCacheEntry(null);
		rq.add(stringRequest);
	}
}
