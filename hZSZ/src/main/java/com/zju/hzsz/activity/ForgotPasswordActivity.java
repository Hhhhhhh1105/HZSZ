package com.zju.hzsz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;

public class ForgotPasswordActivity extends AuthCodeActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_forgot);

		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.forgotpwd);

		findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				telno = ((EditText) findViewById(R.id.et_phonenum)).getText().toString().trim();
				code = ((EditText) findViewById(R.id.et_authcode)).getText().toString().trim();

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

				if (code.length() == 0) {
					showToast("验证码不能为空!");
					((EditText) findViewById(R.id.et_authcode)).requestFocus();
					return;
				}

				startAuthCode();
			}
		});
	}

	@Override
	protected void whenAuthSuccess() {
		getUser().clearInfo();
		getUser().userName = telno;

		startActivityForResult(new Intent(this, ChangePasswordActivity.class), Tags.CODE_SETPWD);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Tags.CODE_SETPWD && resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
