package com.zju.hzsz.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.sin.android.sinlibs.utils.MD5Utils;
import com.zju.hzsz.R;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

public class ChangePasswordActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setpwd);

		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.changepwd);

		findViewById(R.id.btn_submit).setOnClickListener(this);

		if (StrUtils.isNullOrEmpty(getUser().pwdmd5) && !getUser().isLogined()) {
			findViewById(R.id.ll_curpwd).setVisibility(View.GONE);
			setTitle(R.string.setnewpwd);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_submit:
			String curpwd = ((EditText) findViewById(R.id.et_curpassword)).getText().toString();
			String newpwd = ((EditText) findViewById(R.id.et_password)).getText().toString();
			String repwd = ((EditText) findViewById(R.id.et_repassword)).getText().toString();

			if (curpwd.length() == 0 && !StrUtils.isNullOrEmpty(getUser().pwdmd5)) {
				showToast("当前密码不能为空");
				((EditText) findViewById(R.id.et_curpassword)).requestFocus();
				return;
			}

			if (newpwd.length() == 0) {
				showToast("新密码不能为空");
				((EditText) findViewById(R.id.et_curpassword)).requestFocus();
				return;
			}

			if (newpwd.length() < 6) {
				showToast("密码至少需要6位!");
				((EditText) findViewById(R.id.et_password)).requestFocus();
				return;
			}

			if (!newpwd.equals(repwd)) {
				showToast("两次输入的密码不一致");
				((EditText) findViewById(R.id.et_repassword)).requestFocus();
				return;
			}

			changePassword(curpwd, newpwd);

			break;
		default:
			super.onClick(v);
		}

	}

	public void changePassword(String rcurpwd, String rnewpwd) {
		String curpwd = MD5Utils.calcMD5(rcurpwd);
		final String newpwd = MD5Utils.calcMD5(rnewpwd);

		if (!StrUtils.isNullOrEmpty(getUser().pwdmd5) && !getUser().pwdmd5.equals(curpwd)) {
			showToast("当前密码不正确");
			return;
		}

		showOperating();
		getRequestContext().add("action_user_changepassword", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					getUser().pwdmd5 = newpwd;
					saveLocalData();
					showMessage("修改密码成功", new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							setResult(RESULT_OK);
							finish();
						}
					});
				}
			}

		}, BaseRes.class, ParamUtils.freeParam(null, "telephoneNum", getUser().userName, "password", newpwd));
	}
}
