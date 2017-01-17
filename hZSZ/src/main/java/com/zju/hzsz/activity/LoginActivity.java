package com.zju.hzsz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.sin.android.sinlibs.utils.MD5Utils;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.model.LoginRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

public class LoginActivity extends BaseActivity {
	EditText et_username = null;
	EditText et_password = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.login);

		findViewById(R.id.tv_forgot).setOnClickListener(this);
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_register).setOnClickListener(this);

		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login: {
			String username = et_username.getText().toString().trim();
			String passwrod = et_password.getText().toString().trim();
			// if(et_username.getText().toString())
			if (username.length() == 0) {
				showToast(R.string.tip_username_empty);
				et_username.requestFocus();
			} else if (passwrod.length() == 0) {
				showToast(R.string.tip_password_empty);
				et_password.requestFocus();
			} else {
				loginWitdhUP(username, passwrod);
			}
			break;
		}
		case R.id.btn_register: {
			startActivityForResult(new Intent(this, RegisterActivity.class), Tags.CODE_REG);
			break;
		}
		case R.id.tv_forgot: {
			startActivity(new Intent(this, ForgotPasswordActivity.class));
			// startActivityForResult(intent, requestCode);
			break;
		}
		default:
			super.onClick(v);
			break;
		}
	}

	private void loginWitdhUP(final String username, String passwrod) {
		final String pwdmd5 = MD5Utils.calcMD5(passwrod);
		showOperating();
		getUser().uuid = null;
		getRequestContext().add("action_user_load", new Callback<LoginRes>() {
			@Override
			public void callback(LoginRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					showToast("登录成功");
					getUser().userName = username;
					getUser().uuid = o.data.uuid;
					getUser().authority = o.data.authority;
					getUser().riverSum = o.data.riverSum;
					getUser().ifOnJob = o.data.ifOnJob;
					
					getUser().pwdmd5 = pwdmd5;

					saveLocalData();

					setResult(RESULT_OK);
					finish();
					if (getLocalService() != null) {
						saveLocalData();
						getLocalService().notifyableChanged();
					}
				}
			}
		}, LoginRes.class, ParamUtils.freeParam(null, "userName", username, "password", pwdmd5, "cid", getLocalService() != null ? getLocalService().getCid() : ""));
	}

	@Override
	public void finish() {
		super.finish(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (et_username != null && !StrUtils.isNullOrEmpty(getUser().userName)) {
			et_username.setText(getUser().userName);
		}
		if (et_username != null) {
			if (Values.DEBUG) {
//				et_username.setText("13706811460");
				et_username.setText("18857100011");
//				et_username.setText("13705813320");
				et_password.setText("HZHZ123456");
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Tags.CODE_REG && resultCode == RESULT_OK && getUser().isLogined()) {
			setResult(RESULT_OK);
			finish();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
