package com.zju.hzsz.chief.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;

import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.sin.android.sinlibs.utils.InjectUtils;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.model.Duban;
import com.zju.hzsz.model.DubanRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ObjUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

public class ChiefDubanDetailActivity extends BaseActivity {
	private EditText et_feedback = null;
	private View sv_main = null;
	private TableRow tr_response = null;
	private LinearLayout ll_feedback = null;

	private Duban duban = null;
	private ViewRender viewRender = new ViewRender();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_dubandetail);
		setTitle("查看督办单");
		initHead(R.drawable.ic_head_back, 0);

		findViewById(R.id.btn_submit).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);

		duban = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_DUBAN), Duban.class);
		if (duban != null) {
			InjectUtils.injectViews(this, R.id.class);
			sv_main.setVisibility(View.GONE);
			loadData();
		} else {
			showToast("没有督办单信息");
			finish();
		}
	}

	private void loadData() {
		showOperating();
		getRequestContext().add("Get_Duban_Content", new Callback<DubanRes>() {
			@Override
			public void callback(DubanRes o) {
				if (o != null && o.isSuccess()) {
					o.data.dubanId = duban.dubanId;
					ObjUtils.mergeObj(duban, o.data);
					viewRender.renderView(sv_main, duban);

					boolean responsed = duban.dbStatus != 1;
					tr_response.setVisibility(responsed ? View.VISIBLE : View.GONE);
					ll_feedback.setVisibility(!responsed ? View.VISIBLE : View.GONE);

					sv_main.setVisibility(View.VISIBLE);
				}
				hideOperating();
			}
		}, DubanRes.class, ParamUtils.freeParam(null, "dubanId", duban.dubanId));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancel:
			finish();
			break;
		case R.id.btn_submit: {
			String response = et_feedback.getText().toString().trim();
			if (response.length() == 0) {
				showToast("反馈内容不能为空!");
				et_feedback.requestFocus();
				et_feedback.setFocusable(true);
				return;
			}
			submitData(response);
			break;
		}
		default:
			super.onClick(v);
			break;
		}
	}

	private void submitData(String response) {
		showOperating(R.string.doing_submitting);
		getRequestContext().add("Add_Duban_Response", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					showToast("提交成功!");
					setResult(RESULT_OK);
//					finish();
					loadData();
				}
			}
		}, BaseRes.class, ParamUtils.freeParam(null, "dubanId", duban.dubanId, "response", response));
	}
}
