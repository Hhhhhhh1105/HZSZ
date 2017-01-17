package com.zju.hzsz.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.model.CompSugs;
import com.zju.hzsz.model.EvalRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

public class EvalCompActivity extends BaseActivity {
	private CompSugs comp = null;

	// private CompFul compFul = null;

	private static int[] RB_IDS = new int[] { R.id.rb_eval4, R.id.rb_eval3, R.id.rb_eval2, R.id.rb_eval1 };

	private boolean isEvalued() {
		return comp.getStatus() >= 4 && comp.getStatus() != 6;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_evalcomp);

		setTitle(R.string.evaluatenow);
		initHead(R.drawable.ic_head_back, 0);

		comp = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_COMP), CompSugs.class);
		if (comp != null) {
			// loadData();

			if (!comp.isComp()) {
				findViewById(R.id.ll_evallevel).setVisibility(View.GONE);
				findViewById(R.id.tv_evaltips).setVisibility(View.GONE);
			}

			if (isEvalued()) {
				setTitle(R.string.evaluated);
			} else {
				setTitle(R.string.evaluatenow);
			}

			((TextView) findViewById(R.id.tv_sernum)).setText(comp.getNum());

			findViewById(R.id.btn_submit).setOnClickListener(this);
			findViewById(R.id.btn_cancel).setOnClickListener(this);

			if (isEvalued()) {
				// 已评价
				findViewById(R.id.btn_submit).setVisibility(View.GONE);
				((EditText) findViewById(R.id.et_contentt)).setEnabled(false);
				((EditText) findViewById(R.id.et_contentt)).setHint("");
				((RadioGroup) findViewById(R.id.rg_eval)).setEnabled(false);

				for (int id : RB_IDS) {
					findViewById(id).setEnabled(false);
				}

				loadData();
			}

		} else {
			showToast("没有处理单信息");
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_submit: {
			// 提交
			RadioGroup rg_eval = (RadioGroup) findViewById(R.id.rg_eval);
			int rid = rg_eval.getCheckedRadioButtonId();
			if (rid <= 0 && comp.isComp()) {
				showToast("您需要对处理结果进行评价之后才能提交!");
				findViewById(R.id.rg_eval).requestFocus();
				return;
			}
			int vl = comp.isComp() ? Integer.parseInt(findViewById(rid).getTag().toString()) : 0;

			String remark = ((EditText) findViewById(R.id.et_contentt)).getText().toString();
			submitData(vl, remark);

			break;
		}

		case R.id.btn_cancel: {
			finish();
			break;
		}
		default:
			super.onClick(v);
			break;
		}
	}

	private void submitData(int evalLevel, String remark) {
		showOperating();
		getRequestContext().add(comp.isComp() ? "complaintseval_action_add" : "adviceeval_action_add", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					showToast("评价成功");
					setResult(RESULT_OK);
					finish();
				}
			}
		}, BaseRes.class, ParamUtils.freeParam(null, comp.isComp() ? "complaintsId" : "adviceId", comp.getId(), "evalLevel", evalLevel, "evalContent", remark));
	}

	private void loadData() {
		showOperating();
		getRequestContext().add(comp.isComp() ? "complaintseval_action_get" : "adviceeval_action_get", new Callback<EvalRes>() {
			@Override
			public void callback(EvalRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					// RadioGroup rg_eval = (RadioGroup)
					// findViewById(R.id.rg_eval);
					if (o.data.evalLevel >= 0 && o.data.evalLevel <= RB_IDS.length) {
						((RadioButton) findViewById(RB_IDS[o.data.evalLevel])).setChecked(true);
					}
					((EditText) findViewById(R.id.et_contentt)).setText(o.data.evalContent);
					// for(RadioButton rg:rg_eval.getCH)
				}
			}
		}, EvalRes.class, ParamUtils.freeParam(null, comp.isComp() ? "complaintsId" : "adviceId", comp.getId()));
	}
}
