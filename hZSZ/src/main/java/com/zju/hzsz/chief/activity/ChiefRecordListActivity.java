package com.zju.hzsz.chief.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.model.DateTime;
import com.zju.hzsz.model.RiverRecord;
import com.zju.hzsz.model.RiverRecordListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.DipPxUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;
import com.zju.hzsz.view.ListViewWarp.WarpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ChiefRecordListActivity extends BaseActivity implements WarpHandler {

	private ListViewWarp listViewWarp = null;
	private SimpleListAdapter adapter = null;
	private List<RiverRecord> records = new ArrayList<RiverRecord>();

	private ViewRender viewRender = new ViewRender();

	// 编辑巡河单时的监听器
	protected OnClickListener edtClk = new OnClickListener() {

		@Override
		public void onClick(View btn) {
			RiverRecord record = (RiverRecord) btn.getTag();
//			if (record != null) {
//				Intent intent = new Intent(ChiefRecordListActivity.this, com.zju.hzsz.chief.activity.ChiefEditRecordActivity.class);
//				intent.putExtra(Tags.TAG_RECORD, StrUtils.Obj2Str(record));
//				startActivityForResult(intent, Tags.CODE_EDIT);
//			}
			if (record != null) {
				if(record.isCompleted()){
					Intent intent = new Intent(ChiefRecordListActivity.this, com.zju.hzsz.chief.activity.ChiefEditRecordActivity.class);
					intent.putExtra(Tags.TAG_RECORD, StrUtils.Obj2Str(record));
					startActivityForResult(intent, Tags.CODE_EDIT);
				}else {
					Intent intent = new Intent(ChiefRecordListActivity.this, com.zju.hzsz.chief.activity.ChiefEditRecordActivity.class);
					startActivityForResult(intent, Tags.CODE_NEW);
				}

			}
		}
	};

	//删除巡河单，没有用到
	protected OnClickListener delClk = new OnClickListener() {

		@Override
		public void onClick(View btn) {
			final RiverRecord record = (RiverRecord) btn.getTag();
			if (record != null) {
				Dialog d = createMessageDialog("提示", "确定删除该记录吗?", "删除", "取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						showOperating();
						getRequestContext().add("Delete_RiverRecord", new Callback<BaseRes>() {
							@Override
							public void callback(BaseRes o) {
								hideOperating();
								if (o != null && o.isSuccess()) {
									showToast("删除成功!");
									records.remove(record);
									adapter.notifyDataSetChanged();
								}
							}
						}, BaseRes.class, ParamUtils.freeParam(null, "recordId", record.recordId));
					}
				}, null, null);
				d.show();
			}
		}
	};

	private SimpleViewInitor recordInitor = new SimpleViewInitor() {

		@Override
		public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
			if (convertView == null) {
				convertView = LinearLayout.inflate(context, R.layout.item_record, null);
			}

			RiverRecord record = (RiverRecord) data;
			if(record.isCompleted()){
				viewRender.renderView(convertView, record);
				((TextView)convertView.findViewById(R.id.dateOfRecoedList)).setTextColor(getResources().getColor(R.color.black));
				((TextView)convertView.findViewById(R.id.riverOfRecoedList)).setTextColor(getResources().getColor(R.color.black));
			}else {
				SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd");
				Date curDate = new Date(System.currentTimeMillis());//获取当前时间
				String str = formatter.format(curDate);
				((TextView)convertView.findViewById(R.id.dateOfRecoedList)).setText(str);
				((TextView)convertView.findViewById(R.id.dateOfRecoedList)).setTextColor(getResources().getColor(R.color.red));
				((TextView)convertView.findViewById(R.id.riverOfRecoedList)).setText("未完成，点击继续");
				((TextView)convertView.findViewById(R.id.riverOfRecoedList)).setTextColor(getResources().getColor(R.color.red));
			}

//			RiverRecord record = (RiverRecord) data;
//			viewRender.renderView(convertView, record);
			convertView.setTag(record);
			convertView.findViewById(R.id.btn_edit).setTag(record);
			convertView.findViewById(R.id.btn_delete).setTag(record);

			// convertView.findViewById(R.id.btn_edit).setOnClickListener(edtClk);
			convertView.setOnClickListener(edtClk);
			convertView.findViewById(R.id.btn_delete).setOnClickListener(delClk);

			return convertView;
		}
	};

	public String year = "2015";
	public String month = "7";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_recordlist);
		setTitle("巡查记录");
		initHead(R.drawable.ic_head_back, 0);

		Locale.setDefault(Locale.CHINA);
		Calendar calendar = Calendar.getInstance();
		year = "" + calendar.get(Calendar.YEAR);
		month = "" + (1 + calendar.get(Calendar.MONTH));

		findViewById(R.id.btn_new).setOnClickListener(this);
		findViewById(R.id.tv_seldate).setOnClickListener(this);

		adapter = new SimpleListAdapter(this, records, recordInitor);

		listViewWarp = new ListViewWarp(this, adapter, this);
		listViewWarp.getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.bg_gray)));
		listViewWarp.getListView().setDividerHeight(DipPxUtils.dip2px(this, getResources().getDimension(R.dimen.padding_medium)));

		((LinearLayout) findViewById(R.id.ll_main)).addView(listViewWarp.getRootView());
		// listViewWarp.getRootView().setBackgroundColor(getResources().getColor(R.color.white));
		loadRecords(true);

		// listViewWarp.getListView().setOnItemClickListener(new
		// AdapterView.OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long
		// arg3) {
		// RiverRecord record = records.get(pos);
		//
		// Intent intent = new Intent(ChiefRecordListActivity.this,
		// com.zju.hzsz.chief.activity.ChiefEditRecordActivity.class);
		// intent.putExtra(Tags.TAG_RECORD, StrUtils.Obj2Str(record));
		// startActivityForResult(intent, Tags.CODE_EDIT);
		// }
		// });
		refreshDateView();
	}

	private void refreshDateView() {
		((TextView) findViewById(R.id.tv_seldate)).setText(year + "年" + month + "月");
	}

	private YearMonthSelectDialog selectDialog = null;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_new: {
			Intent intent = new Intent(this, com.zju.hzsz.chief.activity.ChiefEditRecordActivity.class);
			startActivityForResult(intent, Tags.CODE_NEW);
			break;
		}
		case R.id.tv_seldate: {
			if (selectDialog == null) {
				selectDialog = new YearMonthSelectDialog(this, new YearMonthSelectDialog.Callback() {
					@Override
					public void onYMSelected(int year, int month) {
						ChiefRecordListActivity.this.year = "" + year;
						ChiefRecordListActivity.this.month = "" + month;
						refreshDateView();
						loadRecords(true);
					}
				});
			}
			selectDialog.show();
			break;
		}
		default:
			super.onClick(v);
			break;
		}
	}

	@Override
	public boolean onRefresh() {
		loadRecords(true);
		return true;
	}

	@Override
	public boolean onLoadMore() {
		loadRecords(false);
		return true;
	}

	private void loadRecords(final boolean refresh) {
		if (refresh)
			listViewWarp.setRefreshing(true);
		else
			listViewWarp.setLoadingMore(true);
		getRequestContext().add("Get_Record_List", new Callback<RiverRecordListRes>() {
			@Override
			public void callback(RiverRecordListRes o) {
				listViewWarp.setRefreshing(false);
				listViewWarp.setLoadingMore(false);

				if (o != null && o.isSuccess()) {
					if (refresh)
						records.clear();
					if (getUser().getBaiduLatPoints() != null && !getUser().getBaiduLatPoints().equals("")){
						RiverRecord unCompletedRiverRecord = new RiverRecord();
						unCompletedRiverRecord.setCompleted(false);
						unCompletedRiverRecord.latlist = getUser().getBaiduLatPoints();
						unCompletedRiverRecord.lnglist = getUser().getBaiduLngPoints();
						unCompletedRiverRecord.recordDate = DateTime.getNow();
						records.add(unCompletedRiverRecord);
					}
					for (RiverRecord rr : o.data) {
						rr.setCompleted(true);
						records.add(rr);
					}
					adapter.notifyDataSetChanged();

					listViewWarp.setNoMore(true);
				}
			}
		}, RiverRecordListRes.class, getPageParam(refresh));
	}

	private final int DefaultPageSize = Constants.DefaultPageSize;

	protected JSONObject getPageParam(boolean refresh) {
		JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (records.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
		try {
			j.put("year", year);
			j.put("month", month);
			j.put("authority", getUser().getAuthority());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//刷新两次，避免出现假的未完成轨迹
		loadRecords(true);
		loadRecords(true);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			loadRecords(true);
		}
	}
}
