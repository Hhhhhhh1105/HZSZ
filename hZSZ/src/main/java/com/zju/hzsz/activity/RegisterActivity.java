package com.zju.hzsz.activity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.sin.android.sinlibs.utils.MD5Utils;
import com.zju.hzsz.R;
import com.zju.hzsz.model.LoginRes;
import com.zju.hzsz.model.SimpleRes;
import com.zju.hzsz.model.SugOrComRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;

public class RegisterActivity extends AuthCodeActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.register);

		findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				telno = ((EditText) findViewById(R.id.et_phonenum)).getText().toString().trim();
				code = ((EditText) findViewById(R.id.et_authcode)).getText().toString().trim();
				password = ((EditText) findViewById(R.id.et_password)).getText().toString().trim();

				if (telno.length() == 0) {
					showToast("手机号码不能为空!");
					((EditText) findViewById(R.id.et_phonenum)).requestFocus();
					return;
				}
				if (telno.length() != 11) {
					showToast("手机号码格式不对!");
					((EditText) findViewById(R.id.et_phonenum)).requestFocus();
					return;
				}

				if (password.length() < 6) {
					showToast("密码至少需要6位!");
					((EditText) findViewById(R.id.et_password)).requestFocus();
					return;
				}

				if (code.length() == 0) {
					showToast("验证码不能为空!");
					((EditText) findViewById(R.id.et_authcode)).requestFocus();
					return;
				}

				boolean agr = ((CheckBox) findViewById(R.id.ck_agree)).isChecked();

				if (!agr) {
					showToast("您需要同意《用户注册协议》才能继续注册!");
					return;
				}

				password = MD5Utils.calcMD5(password);

				startAuthCode();
			}
		});

		findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		findViewById(R.id.iv_showpwd).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// et_password
				EditText etpwd = ((EditText) findViewById(R.id.et_password));
				if ((etpwd.getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) == 0) {
					etpwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				} else {
					etpwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
				}
			}
		});
	}

	private String password = null;

	@Override
	protected void whenAuthSuccess() {
		submitData();
	}

	@Override
	protected boolean canGetAuthCode() {
		String password = ((EditText) findViewById(R.id.et_password)).getText().toString().trim();
		if (password.length() < 6) {
			showToast("密码至少需要6位!");
			((EditText) findViewById(R.id.et_password)).requestFocus();
			return false;
		} else {
			return true;
		}
	}

	private void submitData() {
		showOperating();
		getRequestContext().add("action_user_register", new Callback<LoginRes>() {
			@Override
			public void callback(LoginRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					getUser().userName = telno;
					getUser().uuid = o.data.uuid;
					getUser().pwdmd5 = password;
					showToast("注册成功!");
					setResult(RESULT_OK);
					finish();
					// finishActivity(requestCode);
				}
			}
		}, LoginRes.class, ParamUtils.freeParam(null, "userName", telno, "password", password));
	}
}
