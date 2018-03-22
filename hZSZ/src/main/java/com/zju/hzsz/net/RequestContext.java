package com.zju.hzsz.net;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.sin.android.sinlibs.utils.MD5Utils;
import com.zju.hzsz.R;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.model.BaseRes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {
	protected static final String TAG = "NET";
	private RequestQueue requestQueue = null;
	private Gson gson;
	private BaseActivity context;

	public RequestContext(BaseActivity context, RequestQueue requestQueue) {
		super();
		this.context = context;
		this.requestQueue = requestQueue;
		this.gson = new Gson();
	}

	private void logObject(String s, Object o) {
		Log.i(TAG, "null " + (o == null || s == null));
	}

	public <T extends BaseRes> void add(final String method, final Callback<T> c, final Class<T> classOfT, final Object params) {
		final String time = "" + System.currentTimeMillis();
		final String sign = MD5Utils.calcMD5(Constants.AppKey + method + time + Constants.AppSecret);
		Log.i(TAG, method + ":");
		StringRequest req = new StringRequest(Method.POST, Constants.ApiUrl, new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.i(TAG, response);
				T o = null;
				try {
					o = gson.fromJson(response, classOfT);
					System.out.println("o的值：" + o.toString());

				} catch (Exception e) {
					context.safeToast(e.getMessage());
					e.printStackTrace();
				}
				logObject(response, o);
				if (c != null) {
					c.callback(o);
				}
				if (o != null && !o.isSuccess()) {
					context.safeToast(o.getCode() + ":" + o.getMsg());
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				String err = error.getMessage();
				Log.e(TAG, err == null ? "" : err);
				if (c != null) {
					c.callback(null);
				}
				context.safeToast(R.string.error_network);
//				context.safeToast("url:" + Constants.ApiUrl + "\n" +"method:" + method + "\n" + "parms:" + params);
			}
		}) {

			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> form = new HashMap<String, String>();
				form.put("app_key", Constants.AppKey);
				form.put("method", method);
				form.put("timestamp", time);
				form.put("app_sign", sign);
				if (params != null) {
					String p = null;
					if (params instanceof JSONObject) {
						try {
							if (context.getUser().isLogined())
								((JSONObject) params).put("encryptUserInfo", context.getUser().uuid);
							((JSONObject) params).put("UUID", context.getUser().uuid);
							p = ((JSONObject) params).toString(0);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						p = params.toString();
					}
					Log.i(TAG, "params: " + p);
					form.put("params", p);
				}
				// params.put("userType", "");

				StringBuilder sb = new StringBuilder();
				try {
					for (Map.Entry<String, String> entry : form.entrySet()) {
						// sb.append(URLEncoder.encode(entry.getKey(),
						// "utf-8"));
						sb.append(entry.getKey());
						sb.append('=');
						// sb.append(URLEncoder.encode(entry.getValue(),
						// "utf-8"));
						sb.append(entry.getValue());
						sb.append('&');
					}

					Log.i(TAG, Constants.ApiUrl + "?" + sb.toString().replace("\r", "").replace("\n", ""));
				} catch (Exception e) {
					e.printStackTrace();
				}

				return form;
			}
		};
		req.setTag(context.getClass().getName());
		req.setCacheEntry(null);
		//控制响应时间与retry条数
		req.setRetryPolicy(new DefaultRetryPolicy(100000, 0, 1f));
		requestQueue.add(req);

	}

	public void cancelAll(Object tag) {
		requestQueue.cancelAll(tag);
	}
}
