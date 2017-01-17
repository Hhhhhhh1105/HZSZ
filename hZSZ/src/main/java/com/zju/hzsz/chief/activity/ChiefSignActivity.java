package com.zju.hzsz.chief.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.sin.android.sinlibs.utils.InjectUtils;
import com.zju.hzsz.R;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.model.NumbersRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;

public class ChiefSignActivity extends BaseActivity {
	class SignRecord {
		String text;
		boolean signed;

		public SignRecord(String text, boolean signed) {
			super();
			this.text = text;
			this.signed = signed;
		}
	}

	private GridView gv_signboard = null;
	private TextView tv_month = null;

	Calendar calendarSel = null;

	private List<SignRecord> list = new ArrayList<SignRecord>();
	private SimpleListAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_sign);
		initHead(R.drawable.ic_head_back, 0);
		setTitle("河长签到");

		Locale.setDefault(Locale.CHINA);

		findViewById(R.id.iv_month_left).setOnClickListener(this);
		findViewById(R.id.iv_month_right).setOnClickListener(this);
		findViewById(R.id.ib_sign).setOnClickListener(this);

		InjectUtils.injectViews(this, R.id.class);

		adapter = new SimpleListAdapter(this, list, new SimpleViewInitor() {

			@Override
			public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
				if (convertView == null) {
					convertView = LinearLayout.inflate(context, R.layout.item_signrecord, null);
				}
				SignRecord r = (SignRecord) data;
				((CheckBox) convertView.findViewById(R.id.cb_sign)).setText(r.text);
				((CheckBox) convertView.findViewById(R.id.cb_sign)).setChecked(r.signed);
				return convertView;
			}
		});
		gv_signboard.setAdapter(adapter);

		calendarSel = Calendar.getInstance();
		calendarSel.set(Calendar.DAY_OF_MONTH, 1);

		getSignRecords();
		refreshView();
	}

	private void refreshView() {
		((TextView) findViewById(R.id.tv_name)).setText(getUser().getDisplayName());
		((TextView) findViewById(R.id.tv_info)).setText(getUser().getDisplayRiver());
	}

	private void getSignRecords() {
		tv_month.setText(calendarSel.get(Calendar.YEAR) + "年" + (calendarSel.get(Calendar.MONTH) + 1) + "月");
		showOperating("获取签到数据...");
		getRequestContext().add("Get_HistorySign_Action", new Callback<NumbersRes>() {
			@Override
			public void callback(NumbersRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					int wof = calendarSel.get(Calendar.DAY_OF_WEEK) - 1;
					calendarSel.add(Calendar.MONTH, 1);
					calendarSel.add(Calendar.DAY_OF_MONTH, -1);
					int days = calendarSel.get(Calendar.DAY_OF_MONTH);
					calendarSel.set(Calendar.DAY_OF_MONTH, 1);

					int col = 7;
					int adays = ((wof + days + col - 1) / col) * col;
					list.clear();
					for (int i = 0; i < adays; ++i) {
						list.add(new SignRecord((i < wof || i >= (wof + days)) ? "" : "" + (i + 1 - wof), false));
					}
					if (o.data != null) {
						Calendar today = Calendar.getInstance();
						boolean dojuge = calendarSel.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendarSel.get(Calendar.MONTH) == today.get(Calendar.MONTH);
						boolean todaysigned = false;
						int todayday = today.get(Calendar.DAY_OF_MONTH);
						for (Long l : o.data) {
							list.get(l.intValue() + wof - 1).signed = true;
							if (dojuge && todayday == l.intValue()) {
								// 今天已经签到
								todaysigned = true;
							}
						}
						if (todaysigned) {
							findViewById(R.id.ib_sign).setEnabled(false);
						}
					}
					adapter.notifyDataSetChanged();
				}
			}

		}, NumbersRes.class, ParamUtils.freeParam(null, "year", calendarSel.get(Calendar.YEAR), "month", calendarSel.get(Calendar.MONTH) + 1));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_sign: {
			showOperating("正在签到...");
			getRequestContext().add("Chief_Sign_Action", new Callback<BaseRes>() {
				@Override
				public void callback(BaseRes o) {
					hideOperating();
					if (o != null && o.isSuccess()) {
						showMessage("签到成功!", null);
						getSignRecords();
					}
				}

			}, BaseRes.class, ParamUtils.freeParam(null));
			break;
		}
		case R.id.iv_month_left: {
			calendarSel.add(Calendar.MONDAY, -1);
			getSignRecords();
			break;
		}
		case R.id.iv_month_right: {
			calendarSel.add(Calendar.MONDAY, 1);
			getSignRecords();
			break;
		}
		default:
			super.onClick(v);
			break;
		}
	}

}
