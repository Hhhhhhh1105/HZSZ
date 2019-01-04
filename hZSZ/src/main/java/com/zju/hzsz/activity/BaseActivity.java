package com.zju.hzsz.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.Volley;
import com.sin.android.sinlibs.exutils.ImgUtils;
import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.umeng.analytics.MobclickAgent;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.CheckNotify;
import com.zju.hzsz.model.CheckNotifyRes;
import com.zju.hzsz.model.User;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.RequestContext;
import com.zju.hzsz.service.LocalService;
import com.zju.hzsz.service.LocalService.LocalBinder;
import com.zju.hzsz.utils.PreferencesWarp;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class BaseActivity extends com.sin.android.sinlibs.activities.BaseActivity implements OnClickListener {
	private static BaseActivity curActivity = null;

	public static BaseActivity getCurActivity() {
		return curActivity;
	}

	public interface BooleanCallback {
		public void callback(boolean b);
	}

	public interface LoginCallback {
		public void loginCallback(boolean logined);
	}

	private static int[] HeadIDs = new int[] { R.id.iv_head_left, R.id.iv_head_right };
	private RequestContext requestContext = null;

	private static User user = null;
	private static SharedPreferences preferences = null;
	private PreferencesWarp preferencesWarp = null;

	protected SharedPreferences getLocalStorage() {
		if (preferences == null) {
			// preferences = this.getPreferences(MODE_PRIVATE);
			preferences = this.getSharedPreferences("preferences", MODE_APPEND);
		}
		return preferences;
	}

	protected PreferencesWarp getPreferencesWarp() {
		if (preferencesWarp == null)
			preferencesWarp = new PreferencesWarp(this, getLocalStorage());
		return preferencesWarp;
	}

	public User getUser() {
		if (user == null) {
			user = getPreferencesWarp().getObj(Tags.TAG_USER, User.class);
			if (user == null)
				user = new User();
		}
		return user;
	}

	// public void setUser(User u) {
	// BaseActivity.user = u;
	// }

	public BaseActivity() {
		super();
		curActivity = this;
	}

	protected String getTag() {
		return this.getClass().getSimpleName();
	}

	public RequestContext getRequestContext() {
		if (requestContext == null)
			requestContext = new RequestContext(this, Volley.newRequestQueue(this));
		return requestContext;
	}

	protected void initHead(int leftimg, int rightimg) {
		ImageView iv_left = (ImageView) (findViewById(R.id.iv_head_left));
		ImageView iv_right = (ImageView) (findViewById(R.id.iv_head_right));
		if (iv_left != null) {
			if (leftimg != 0) {
				iv_left.setImageResource(leftimg);
			} else {
				iv_left.setVisibility(View.INVISIBLE);
			}
		}

		if (iv_right != null) {
			if (rightimg != 0) {
				iv_right.setImageResource(rightimg);
			} else {
				iv_right.setVisibility(View.INVISIBLE);
			}
		}
		for (int id : HeadIDs) {
			View v = findViewById(id);
			if (v != null) {
				v.setOnClickListener(this);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancel:
		case R.id.iv_head_left:
			finish();
			break;
		case R.id.iv_head_right:

			break;
		default:
			break;
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		TextView tv = (TextView) findViewById(R.id.tv_head_title);
		if (tv != null) {
			tv.setText(title);
		}
	}

	@Override
	public void setTitle(int titleId) {
		TextView tv = (TextView) findViewById(R.id.tv_head_title);
		if (tv != null) {
			tv.setText(titleId);
		}
	}

	@Override
	public void setTitleColor(int textColor) {
		TextView tv = (TextView) findViewById(R.id.tv_head_title);
		if (tv != null) {
			tv.setTextColor(textColor);
		}
	}

	protected ProgressDialog doingDlg = null;

	public ProgressDialog showOperating() {
		return showOperating(R.string.doing_loading);
	}

	public ProgressDialog showOperating(int strid) {
		return showOperating(getText(strid));
	}

	public ProgressDialog showOperating(CharSequence text) {
		if (doingDlg == null) {
			doingDlg = new ProgressDialog(this);
			doingDlg.setTitle(null);
			//设置ProgressDialog 是否可以按退回按键取消
			doingDlg.setCancelable(false);
		}
		//设置ProgressDialog 提示信息，显示ProgressDialog
		doingDlg.setMessage(text);
		doingDlg.show();
		return doingDlg;
	}

	public void hideOperating() {
		if (doingDlg != null)
			doingDlg.dismiss();
	}

	public void startXmlActivity(int xmlid, int titleid, int leftid, int rightid) {
		Intent intent = new Intent(this, XmlActivity.class);
		intent.putExtra(XmlActivity.STR_XMLID, xmlid);
		intent.putExtra(XmlActivity.STR_TITLEID, titleid);
		intent.putExtra(XmlActivity.STR_LEFTID, leftid);
		intent.putExtra(XmlActivity.STR_RIGHTID, rightid);
		startActivity(intent);
	}

	@Override
	public void startActivity(Intent intent) {
		startActivity(intent, true);
	}

	public void startActivity(Intent intent, boolean anm) {
		super.startActivity(intent);
		if (anm)
			overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
	}

	public void startActivityForResult(Intent intent, int requestCode, boolean anm) {
		super.startActivityForResult(intent, requestCode);
		if (anm)
			overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
	}

	@Override
	public void finish() {
		finish(true);
	}

	public void finish(boolean anm) {
		super.finish();
		if (anm)
			overridePendingTransition(R.anim.in_from_left, R.anim.out_from_right);
	}

	@Override
	protected void onDestroy() {
		if (user != null)
			saveLocalData();
		if (requestContext != null) {
			requestContext.cancelAll(this.getClass().getName());
		}

		if (getLocalService() != null) {
			getLocalService().unlistenLocation();
		}

		if (serviceConnection != null) {
			unbindService(serviceConnection);
		}

		super.onDestroy();
	}

	protected void saveLocalData() {
		Log.e(getTag(), "saveLocalData");
		getUser().readySave();
		getPreferencesWarp().setObj(Tags.TAG_USER, getUser());
	}

	private LoginCallback loginCallback = null;

	public void setLoginCallback(LoginCallback cbk) {
		loginCallback = cbk;
	}

	public boolean checkUserAndLogin(String message) {
		if (getUser().isLogined()) {
			return true;
		} else {
			createMessageDialog("提示", message, "确定", null, null, null, null).show();
			// Intent intent = new Intent(this, LoginActivity.class);
			// startActivityForResult(intent, Tags.CODE_LOGIN);
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Tags.CODE_LOGIN && loginCallback != null) {
			loginCallback.loginCallback(resultCode == RESULT_OK);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void showMessage(String message, final DialogInterface.OnDismissListener dmsclk) {
		Dialog dlg = createMessageDialog("提示", message, "确定", null, null, null, null);
		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				if (dmsclk != null)
					dmsclk.onDismiss(arg0);
			}
		});
		dlg.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		curActivity = this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private ViewRender viewRender = null;

	public ViewRender getViewRender() {
		if (viewRender == null) {
			viewRender = new ViewRender() {
				@Override
				public void renderImageView(ImageView iv, Object model, String tmpl) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
					String url = templateEngine.evalString(model, tmpl);
					if (url == null || url.trim().length() == 0) {
						iv.setImageResource(R.drawable.ic_launcher);
					} else {
						ImgUtils.loadImage(BaseActivity.this, iv, com.zju.hzsz.utils.StrUtils.getImgUrl(url), R.drawable.ic_launcher, R.drawable.ic_launcher);
					}
				}

				@Override
				public void renderView(View view, Object model, String tmpl) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
					if (view instanceof WebView) {
						WebView wv = (WebView) view;
						wv.getSettings().setDefaultTextEncodingName("utf-8");
						wv.loadData(templateEngine.evalString(model, tmpl), "text/html", "utf-8");
					} else {
						super.renderView(view, model, tmpl);
					}
				}
			};
		}
		return viewRender;
	}

	public void nofityChecked(CheckNotify cn) {
		Intent intent = new Intent(Tags.ACT_USERSTATUSCHANGED);
		intent.putExtra(Tags.TAG_PARAM, StrUtils.Obj2Str(cn));
		sendBroadcast(intent);
	}

	private static boolean isChecking = false;

	public void checkChiefNotify() {
		if (!isChecking && getUser().isLogined() && getUser().isChief()) {
			isChecking = true;
			getRequestContext().add("Get_Notify", new Callback<CheckNotifyRes>() {
				@Override
				public void callback(CheckNotifyRes o) {
					if (o != null && o.isSuccess()) {
						nofityChecked(o.data);
					}
					isChecking = false;
				}
			}, CheckNotifyRes.class, new JSONObject());
		}
	}

	private ServiceConnection serviceConnection = null;
	private LocalBinder localBinder = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName arg0) {

			}

			@Override
			public void onServiceConnected(ComponentName arg0, IBinder binder) {
				localBinder = (LocalBinder) binder;
				BaseActivity.this.onServiceConnected();
			}
		};

		bindService(new Intent(this, LocalService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}

	public LocalService  getLocalService() {
		return localBinder != null ? localBinder.getService() : null;
	}

	protected void onServiceConnected() {

	}

	/**
	 *位置信息相关
	 */
	public static double latitude = 0.0;
	public static double longitude = 0.0;

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
