package com.zju.hzsz.activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.sin.android.sinlibs.base.Callable;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.chief.activity.ChiefCompListActivity;
import com.zju.hzsz.chief.activity.ChiefMailListActivity;
import com.zju.hzsz.model.CheckNotifyRes;
import com.zju.hzsz.model.StartInfo;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.receiver.PushReceiver;
import com.zju.hzsz.utils.ImgUtils;

import org.json.JSONObject;

public class WelcomeActivity extends BaseActivity {
	ImageView iv_welcome = null;
	ImageView iv_welcome_bottom = null;
	StartInfo startInfo = null;

	boolean welcome_showed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		if (PushReceiver.getPayload() != null) {
			startActivity(new Intent(WelcomeActivity.this, MainActivity.class), false);
			finish(false);
			return;
		}

		iv_welcome = (ImageView) findViewById(R.id.iv_welcome);
		iv_welcome_bottom = (ImageView) findViewById(R.id.iv_welcome_bottom);

		startInfo = getPreferencesWarp().getObj("startInfo", StartInfo.class);
		if (startInfo != null) {
			ImgUtils.loadImage(WelcomeActivity.this, iv_welcome, com.zju.hzsz.utils.StrUtils.getImgUrl(startInfo.picPath));
		}

	/*	getRequestContext().add("Get_StartPage", new Callback<StartInfoRes>() {
			@Override
			public void callback(StartInfoRes o) {
				if (o != null && o.isSuccess() && o.data != null) {
					startInfo = o.data;

					getPreferencesWarp().setObj("Get_StartPage", startInfo);
					if (welcome_showed && startInfo.picPath != null && startInfo.picPath.length() > 0) {
						ImgUtils.loadImage(WelcomeActivity.this, iv_welcome, com.zju.hzsz.utils.StrUtils.getImgUrl(startInfo.picPath));
					}
				}
			}
		}, StartInfoRes.class, null);*/

		asynCall(new Callable() {
			@Override
			public void call(Object... args) {
				try {
					// Thread.sleep(6000);
					synchronized (WelcomeActivity.this) {
//						WelcomeActivity.this.wait(2000);
					}
					welcome_showed = true;
					safeCall(new Callable() {

						@Override
						public void call(Object... args) {
							if (startInfo != null && startInfo.picPath != null && startInfo.picPath.length() > 0) {
								ImgUtils.loadImage(WelcomeActivity.this, iv_welcome, com.zju.hzsz.utils.StrUtils.getImgUrl(startInfo.picPath));
								iv_welcome_bottom.setVisibility(View.VISIBLE);
							}
						}
					});
					if (startInfo.picPath != null && startInfo.picPath.length() > 0) {
						synchronized (WelcomeActivity.this) {
//							WelcomeActivity.this.wait(2000);
						}
					}
				} catch (Exception e) {

				}
				safeCall(new Callable() {

					@Override
					public void call(Object... args) {
						startActivity(new Intent(WelcomeActivity.this, MainActivity.class), false);
						finish(false);
					}
				});
			}
		});

		iv_welcome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (startInfo != null && welcome_showed) {
					Uri uri = Uri.parse(startInfo.linkPath);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}
			}
		});

		iv_welcome_bottom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				synchronized (WelcomeActivity.this) {
					WelcomeActivity.this.notifyAll();
				}
			}
		});

		if (getUser().getAuthority() != 0 && getUser().getAuthority() == 2) {
			checkNotify();
		}

	}

	private void checkNotify() {
		getRequestContext().add("Get_Notify", new Callback<CheckNotifyRes>() {
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void callback(CheckNotifyRes o) {
				if (o != null && o.isSuccess()) {
					// o.data.sumUndealComp = 10;
					// o.data.sumUndealAdv = 5;
					nofityChecked(o.data);

					if (o.data.sumUndealComp > 0) {
						Context context = WelcomeActivity.this;
						NotificationManager nm = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

						Notification.Builder builder = new Notification.Builder(context);
						builder.setSmallIcon(R.drawable.ic_launcher);

						String content = "您有" + o.data.sumUndealComp + "条投诉未处理!";

						builder.setTicker(content);
						builder.setContentTitle(getString(R.string.app_name));
						builder.setContentText(content);
						builder.setAutoCancel(true);
						Notification notification = null;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							notification = builder.build();
						} else {
							notification = builder.getNotification();
						}

						Intent notificationIntent = new Intent(context, ChiefCompListActivity.class);
						PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
						notification.contentIntent = contentIntent;
						nm.notify(Tags.ID_NOTIFICATION, notification);
					}

					if (o.data.sumUnReadMail > 0) {
						Context context = WelcomeActivity.this;
						NotificationManager nm = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

						Notification.Builder builder = new Notification.Builder(context);
						builder.setSmallIcon(R.drawable.ic_launcher);

						String content = "您有" + o.data.sumUnReadMail + "条消息未读!";

						builder.setTicker(content);
						builder.setContentTitle(getString(R.string.app_name));
						builder.setContentText(content);
						builder.setAutoCancel(true);
						Notification notification = null;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							notification = builder.build();
						} else {
							notification = builder.getNotification();
						}

						Intent notificationIntent = new Intent(context, ChiefMailListActivity.class);
						PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
						notification.contentIntent = contentIntent;
						nm.notify(Tags.ID_NOTIFICATION, notification);
					}
				}
			}
		}, CheckNotifyRes.class, new JSONObject());
	}
}
