package com.zju.hzsz.activity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sin.android.sinlibs.base.Callable;
import com.zju.hzsz.R;
import com.zju.hzsz.Values;
import com.zju.hzsz.utils.StrUtils;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class AuthCodeActivity extends BaseActivity {
	private boolean running = false;
	protected String telno = null;
	protected String code = null;
	protected String country = "86";

	//声明EventHandler(获取验证码成功、提交验证码成功等回调都在EventHandler中实现）
	EventHandler smseh = new EventHandler() {

		//afterEvent并不在主线程中，因此回调完成的时候不能再afterEvent()执行更新UI，若需要执行UI操作请使用Handler
		@Override
		public void afterEvent(int event, int result, Object data) {
			safeCall(new Callable() {

				@Override
				public void call(Object... args) {
					int e = (Integer) args[0];
					int r = (Integer) args[1];
					Object d = args[2];
					hideOperating();
					if (r == SMSSDK.RESULT_COMPLETE) {
						// 回调完成
						if (e == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
							// 提交验证码成功
							@SuppressWarnings("unchecked")
							HashMap<String, Object> phoneMap = (HashMap<String, Object>) d;
							String ct = (String) phoneMap.get("country");
							String ph = (String) phoneMap.get("phone");
							if (country.equals(ct) == telno.equals(ph)) {
								whenAuthSuccess();
							} else {
								showToast("验证码不对！");
							}
						} else if (e == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
							// 获取验证码成功
							showToast("短信已经发生到您的手机，请注意查收!");
							Values.lastAuthCodeTime = System.currentTimeMillis();
							waitTimeToGetAuthCode(Values.AUTHCODE_GAP_S);
							whenGetAuthCode();

						} else if (e == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
							// 返回支持发送验证码的国家列表
						}
					} else {
						((Throwable) d).printStackTrace();
						showToast("验证码不对！");
					}
				}
			}, event, result, data);

			/*if (result == SMSSDK.RESULT_COMPLETE) {
				//回调完成
				if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
					//提交验证码成功
					HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
					String ct = (String) phoneMap.get("country");
					String ph = (String) phoneMap.get("phone");
					if (country.equals(ct) == telno.equals(ph)) {
						whenAuthSuccess();
					} else {
						showToast("验证码不对！");
					}
				}else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
					//获取验证码成功
					showToast("短信已经发生到您的手机，请注意查收!");
				}else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
					//返回支持发送验证码的国家列表
				}
			}else{
				((Throwable)data).printStackTrace();
				showToast("验证码不对！");
			}*/

		}
	};

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//初始化
		SMSSDK.initSDK(this, Values.SMS_APPKEY, Values.SMS_SECKEY);
		//注册短信回调
		SMSSDK.registerEventHandler(smseh);
	}

	protected void whenGetAuthCode() {

	}

	protected void whenAuthSuccess() {

	}

	private void waitTimeToGetAuthCode(int tm) {
		safeCall(new Callable() {
			@Override
			public void call(Object... args) {
				findViewById(R.id.btn_getauthcode).setEnabled(false);
			}
		});
		running = true;
		asynCall(new Callable() {
			@Override
			public void call(Object... args) {
				int tm = (Integer) args[0];
				while (tm > 0 && running) {
					safeCall(new Callable() {
						@Override
						public void call(Object... args) {
							((TextView) findViewById(R.id.btn_getauthcode)).setText(StrUtils.renderText(AuthCodeActivity.this, R.string.fmt_seconds_to_authcode, args[0]));
						}
					}, tm);
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
					--tm;
				}
				running = false;
				safeCall(new Callable() {
					@Override
					public void call(Object... args) {
						findViewById(R.id.btn_getauthcode).setEnabled(true);
						((TextView) findViewById(R.id.btn_getauthcode)).setText(R.string.getauthcode);
					}
				});
			}
		}, tm);
	}

	protected void startAuthCode() {
		showOperating(R.string.doing_submitting);
		// {"riverId":"5","advTheme":"河道脏","advContent":"河道实在太脏","encryptUserInfo":"34"}

		Log.i(getTag(), "验证验证码 " + country + " " + telno + " " + code);
		//用于向服务器提交接收到的短信验证码，验证成功后会通过EventHandler返回国家代码和电话号码
		SMSSDK.submitVerificationCode(country, telno, code);
	}

	@Override
	protected void onResume() {
		super.onResume();
		long wtm = (int) (Values.AUTHCODE_GAP_S - (System.currentTimeMillis() - Values.lastAuthCodeTime) / 1000);
		if (wtm > 0) {
			waitTimeToGetAuthCode((int) wtm);
		}

		findViewById(R.id.btn_getauthcode).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (canGetAuthCode()) {
					String telno = ((EditText) findViewById(R.id.et_phonenum)).getText().toString().trim();
					if (telno.length() == 0) {
						showToast("号码不能为空!");
						((EditText) findViewById(R.id.et_phonenum)).requestFocus();
						return;
					}

					if (!telno.matches("[0-9]{11}")) {
						showToast("号码格式不对!");
						((EditText) findViewById(R.id.et_phonenum)).requestFocus();
						return;
					}
					Log.i(getTag(), "获取验证码 " + country + " " + telno);
					showOperating(R.string.doing_gettingauthcode);
					SMSSDK.getVerificationCode(country, telno);
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		running = false;
		SMSSDK.unregisterAllEventHandler();
		super.onDestroy();
	}

	protected boolean canGetAuthCode() {
		return true;
	}
}
