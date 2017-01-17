package com.zju.hzsz.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.model.CheckNotifyRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;

import org.json.JSONObject;

public class MeActivity extends BaseActivity {

	private int[] showWhenLogined = { R.id.tv_logout, R.id.rl_setting, R.id.rl_complaint, R.id.rl_suggestion };
//	private int[] showWhenChiefLogined = { R.id.rl_chief_sign, R.id.rl_chief_mail, R.id.rl_chief_complaint, R.id.rl_chief_duban, R.id.rl_chief_inspect, R.id.rl_chief_record, R.id.rl_chief_suggestion };
	private int[] showWhenChiefLogined = { R.id.rl_chief_sign, R.id.rl_chief_mail, R.id.rl_chief_complaint, R.id.rl_chief_duban,  R.id.rl_chief_record, R.id.rl_chief_suggestion };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me);
		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.mycenter);

		findViewById(R.id.tv_complaint).setOnClickListener(this);
		findViewById(R.id.tv_suggestion).setOnClickListener(this);
		findViewById(R.id.tv_collection).setOnClickListener(this);
		findViewById(R.id.tv_setting).setOnClickListener(this);
		findViewById(R.id.tv_about).setOnClickListener(this);
		findViewById(R.id.tv_logout).setOnClickListener(this);
		findViewById(R.id.iv_logo).setOnClickListener(this);

		findViewById(R.id.tv_chief_complaint).setOnClickListener(this);
		findViewById(R.id.tv_chief_duban).setOnClickListener(this);
		findViewById(R.id.tv_chief_inspect).setOnClickListener(this);
		findViewById(R.id.tv_chief_record).setOnClickListener(this);
		findViewById(R.id.tv_chief_suggestion).setOnClickListener(this);
		findViewById(R.id.tv_chief_sign).setOnClickListener(this);
		findViewById(R.id.tv_chief_mail).setOnClickListener(this);
	}

	private void checkNotify() {
		getRequestContext().add("Get_Notify", new Callback<CheckNotifyRes>() {
			@Override
			public void callback(CheckNotifyRes o) {
				if (o != null && o.isSuccess()) {
					// o.data.sumUndealComp = 10;
					// o.data.sumUndealAdv = 5;
					nofityChecked(o.data);

					//投诉信息提醒
					if (o.data.sumUndealComp > 0) {
						((TextView) findViewById(R.id.tv_chief_unhandlecomplaint_count)).setText(o.data.sumUndealComp + "个投诉未处理");
						((TextView) findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.VISIBLE);
					} else {
						((TextView) findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.GONE);
					}

					if (o.data.sumUndealAdv > 0) {
						((TextView) findViewById(R.id.tv_chief_unhandlesuggestion_count)).setText(o.data.sumUndealAdv + "个建议未处理");
						((TextView) findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.VISIBLE);
					} else {
						((TextView) findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.GONE);
					}

					if (o.data.sumUnReadMail > 0) {
						((TextView) findViewById(R.id.tv_chief_unreadmail_count)).setText(o.data.sumUnReadMail + "条未读消息");
						((TextView) findViewById(R.id.tv_chief_unreadmail_count)).setVisibility(View.VISIBLE);
					} else {
						((TextView) findViewById(R.id.tv_chief_unreadmail_count)).setVisibility(View.GONE);
					}
				}
			}
		}, CheckNotifyRes.class, new JSONObject());
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshView();

		((TextView) findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.GONE);
		((TextView) findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.GONE);
		if (getUser().isLogined() && getUser().isChief()) {
			checkNotify();
		}
	}

	private void refreshView() {
		boolean logined = getUser().isLogined();
		boolean ischief = getUser().isLogined() && getUser().isChief();
		for (int id : showWhenLogined) {
			View v = findViewById(id);
			if (v != null)
				v.setVisibility(logined ? View.VISIBLE : View.GONE);
		}
		for (int id : showWhenChiefLogined) {
			View v = findViewById(id);
			if (v != null)
				v.setVisibility(ischief ? View.VISIBLE : View.GONE);
		}
		
//		if(ischief){
//			findViewById(R.id.rl_complaint).setVisibility(View.GONE);
//			findViewById(R.id.rl_suggestion).setVisibility(View.GONE);
//		}

		((TextView) findViewById(R.id.tv_name)).setText(getUser().getDisplayName());
		((TextView) findViewById(R.id.tv_info)).setText(getUser().getDisplayRiver());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_chief_sign: {
			startActivity(new Intent(this, com.zju.hzsz.chief.activity.ChiefSignActivity.class));
			break;
		}
		case R.id.tv_chief_mail: {
			startActivity(new Intent(this, com.zju.hzsz.chief.activity.ChiefMailListActivity.class));
			break;
		}
		case R.id.tv_chief_complaint: {
			startActivity(new Intent(this, com.zju.hzsz.chief.activity.ChiefCompListActivity.class));
			break;
		}
		case R.id.tv_chief_suggestion: {
			Intent intent = new Intent(this, com.zju.hzsz.chief.activity.ChiefCompListActivity.class);
			intent.putExtra(Tags.TAG_ABOOLEAN, false);
			startActivity(intent);

			break;
		}
		case R.id.tv_chief_inspect:{
			Intent intent = new Intent( this,com.zju.hzsz.chief.activity.ChiefInspectActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.tv_chief_record: {
			Intent intent = new Intent(this, com.zju.hzsz.chief.activity.ChiefRecordListActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.tv_chief_duban: {
			Intent intent = new Intent(this, com.zju.hzsz.chief.activity.ChiefDubanListActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.tv_complaint: {
			startActivity(new Intent(this, CompListActivity.class));
			break;
		}
		case R.id.tv_suggestion: {
			Intent intent = new Intent(this, CompListActivity.class);
			intent.putExtra(Tags.TAG_ABOOLEAN, false);
			startActivity(intent);
			break;
		}
		case R.id.tv_collection: {
			startActivity(new Intent(this, MyCollectActivity.class));
			break;
		}
		case R.id.tv_about: {
			startActivity(new Intent(this, AboutActivity.class));
			break;
		}
		case R.id.tv_setting: {
			startActivity(new Intent(this, SettingActivity.class));
			break;
		}
		case R.id.iv_logo: {
			if (!getUser().isLogined()) {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivityForResult(intent, Tags.CODE_LOGIN);
			}
			break;
		}
		case R.id.tv_logout:
			createMessageDialog("提示", "确定要注销登录吗?", "确定", "取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					logout();
				}
			}, null, null).show();
			break;
		default:
			super.onClick(v);
			break;
		}
	}

	private void logout() {
		showOperating();
		getRequestContext().add("User_Logout_Action", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				// if (o != null && o.isSuccess()) {
				getUser().clearInfo();
				refreshView();
				hideOperating();
				// }
			}
		}, BaseRes.class, ParamUtils.freeParam(null, "cid", getLocalService() != null ? getLocalService().getCid() : ""));
	}

	@Override
	public void finish() {
		super.finish(false);
	}
}
