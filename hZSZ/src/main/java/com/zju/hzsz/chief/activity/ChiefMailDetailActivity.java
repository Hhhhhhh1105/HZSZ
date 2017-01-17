package com.zju.hzsz.chief.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.model.Mail;
import com.zju.hzsz.model.MailRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ObjUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

public class ChiefMailDetailActivity extends BaseActivity {

	private Mail mail = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_maildetail);
		initHead(R.drawable.ic_head_back, 0);
		setTitle("消息详情");
		findViewById(R.id.ll_main).setVisibility(View.GONE);
		mail = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_MAIL), Mail.class);
		if (mail == null) {
			showToast("确实消息参数");
			finish();
			return;
		} else {
			if (mail.isReaded()) {
				((Button) findViewById(R.id.btn_sign)).setText("已签收");
				findViewById(R.id.btn_sign).setEnabled(false);
			}
			findViewById(R.id.btn_cancel).setOnClickListener(this);
			findViewById(R.id.btn_sign).setOnClickListener(this);
			loadMail();
		}
	}

	private boolean loadMail() {
		showOperating("获取消息详情...");
		getRequestContext().add("Get_Mail_Content", new Callback<MailRes>() {
			@Override
			public void callback(MailRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					ObjUtils.mergeObj(mail, o.data);

					getViewRender().renderView(findViewById(R.id.ll_main), mail);
					findViewById(R.id.ll_main).setVisibility(View.VISIBLE);
				}
			}

		}, MailRes.class, ParamUtils.freeParam(null, "mailId", mail.id));
		return true;
	}

	private boolean signMail() {
		showOperating("正在签收...");
		getRequestContext().add("Mail_Sign", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				showMessage("签收成功！", null);
				mail.ifRead = 1;
				((Button) findViewById(R.id.btn_sign)).setText("已签收");
				findViewById(R.id.btn_sign).setEnabled(false);
				
				setResult(RESULT_OK);
			}

		}, BaseRes.class, ParamUtils.freeParam(null, "mailId", mail.id));
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_sign:
			signMail();
			break;

		default:
			super.onClick(v);
			break;
		}
	}
}
