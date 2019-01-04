package com.zju.hzsz.fragment.river;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.activity.CompDetailActivity;
import com.zju.hzsz.chief.activity.YearMonthSelectDialog;
import com.zju.hzsz.model.CompPublicity;
import com.zju.hzsz.model.CompPublicitysRes;
import com.zju.hzsz.model.CompSugs;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverRecord;
import com.zju.hzsz.model.RiverRecordListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RiverInfoPubItem extends BaseRiverPagerItem {
	private static final String TAG = "RiverInfoPubItem";
	public RiverInfoPubItem(River river, BaseActivity context) {
		super(river, context);
		Locale.setDefault(Locale.CHINA);

		year = "" + Calendar.getInstance().get(Calendar.YEAR);
		month = "" + (Calendar.getInstance().get(Calendar.MONTH) + 1);
	}

	private ListViewWarp listViewWarp = null;
	private SimpleListAdapter adapter = null;
	private List<Object> list = new ArrayList<Object>();
	private boolean isComp = true;

	public String year = "2015";
	public String month = "7";

	@Override
	public View getView() {
		if (view == null) {
			view = LinearLayout.inflate(context, R.layout.fragment_river_infopub, null);
			if (river.riverLevel==2){
				view.findViewById(R.id.rb_river_infopub_xhjl).setVisibility(View.GONE);
			}else {
				view.findViewById(R.id.rb_river_infopub_xhjl).setVisibility(View.VISIBLE );
			}
			((RadioGroup) view.findViewById(R.id.rg_river_pubinfo_showwith)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup arg0, int rid) {
					isComp = rid == R.id.rb_river_infopub_tsxx;
					view.findViewById(R.id.ll_recodrhead).setVisibility(isComp ? View.GONE : View.VISIBLE);
					loadData(true);
				}
			});
			adapter = new SimpleListAdapter(context, list, new SimpleViewInitor() {

				@Override
				public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
					boolean isComp = data instanceof CompPublicity;
					if (isComp && (convertView == null || convertView.findViewById(R.id.ll_comp_container) == null)) {
						convertView = LinearLayout.inflate(context, R.layout.item_river_comppublicity, null);
					} else if (!isComp && (convertView == null || convertView.findViewById(R.id.ll_comp_container) != null)) {
						convertView = LinearLayout.inflate(context, R.layout.item_record_3fied, null);
					}

					if (isComp) {
						((BaseActivity) context).getViewRender().renderView(convertView, data);
						CompPublicity cp = (CompPublicity) data;
						((ImageView) convertView.findViewById(R.id.iv_status)).setImageResource(cp.isHandled() ? R.drawable.im_cp_handled : R.drawable.im_cp_unhandle);
						((TextView) convertView.findViewById(R.id.tv_status)).setTextColor(context.getResources().getColor(cp.isHandled() ? R.color.green : R.color.red));
					} else {
						RiverRecord record = (RiverRecord) data;
						((BaseActivity) context).getViewRender().renderView(convertView, record);
					}
					return convertView;
				}
			});
			listViewWarp = new ListViewWarp(context, adapter, new ListViewWarp.WarpHandler() {

				@Override
				public boolean onRefresh() {
					return loadData(true);
				}

				@Override
				public boolean onLoadMore() {
					return false;
				}
			});

			listViewWarp.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
					if (pos < list.size()) {
						Object o = list.get(pos);
						if (o instanceof CompPublicity) {
							CompPublicity cp = (CompPublicity) o;
							CompSugs comp = new CompSugs();
							comp.complaintsId = cp.getId();
							comp.complaintsPicPath = cp.getCompPicPath();
							comp.compStatus = cp.compStatus;
							comp.compPersonId = cp.compPersonId;

							Intent intent = new Intent(context, CompDetailActivity.class);
							intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
							intent.putExtra(Tags.TAG_ABOOLEAN, true);
							context.startActivity(intent);
						} else if (o instanceof RiverRecord) {
							RiverRecord record = (RiverRecord) o;
							if (record != null) {
								Intent intent = new Intent(context, com.zju.hzsz.chief.activity.ChiefEditRecordActivity.class);
								intent.putExtra(Tags.TAG_RECORD, StrUtils.Obj2Str(record));
								intent.putExtra(Tags.TAG_ABOOLEAN, true);
								context.startActivityForResult(intent, Tags.CODE_EDIT);
							}
						}
					}
				}
			});

			listViewWarp.startRefresh();
			((LinearLayout) view.findViewById(R.id.ll_river_pubinfo)).addView(listViewWarp.getRootView());

			view.findViewById(R.id.tv_seldate).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (selectDialog == null) {
						selectDialog = new YearMonthSelectDialog(context, new YearMonthSelectDialog.Callback() {
							@Override
							public void onYMSelected(int y, int m) {
								year = "" + y;
								month = "" + m;
								refreshDateView();
								loadData(true);
							}
						});
					}
					selectDialog.show();
				}
			});

			refreshDateView();

			// view = listViewWarp.getRootView();
		}
		return view;
	}

	private void refreshDateView() {
		((TextView) view.findViewById(R.id.tv_seldate)).setText(year + "年" + month + "月");
	}

	private YearMonthSelectDialog selectDialog = null;

	private boolean loadData(final boolean refresh) {
		if (refresh)
			listViewWarp.setRefreshing(true);
		if (refresh) {
			list.clear();
			adapter.notifyDataSetInvalidated();
			Log.d(TAG, "loadData: "+river.riverLevel);
		}

		listViewWarp.setNoMoreText(isComp ? "暂无投诉" : "暂无记录");

		if (isComp) {
			((BaseActivity) context).getRequestContext().add("Get_RiverComplaint_List", new Callback<CompPublicitysRes>() {
				@Override
				public void callback(CompPublicitysRes o) {
					listViewWarp.setRefreshing(false);
					if (o != null && o.isSuccess() && o.data != null) {
						if (refresh)
							list.clear();
						for (CompPublicity cp : o.data) {
							list.add(cp);
						}

						// adapter.notifyDataSetChanged();
						adapter.notifyDataSetInvalidated();
					}
					if (list.size() == 0) {
						listViewWarp.setNoMore(true);
					} else {
						listViewWarp.setNoMore(false);
					}
				}
			}, CompPublicitysRes.class, ParamUtils.freeParam(null, "riverId", river.riverId));
		} else {


			((BaseActivity) context).getRequestContext().add("Get_RiverRecord_List", new Callback<RiverRecordListRes>() {
				@Override
				public void callback(RiverRecordListRes o) {
					listViewWarp.setRefreshing(false);
					if (o != null && o.isSuccess() && o.data != null) {
						if (refresh)
							list.clear();
						for (RiverRecord cp : o.data) {
							list.add(cp);
						}

						// adapter.notifyDataSetChanged();
						adapter.notifyDataSetInvalidated();
					}
					if (list.size() == 0) {
						listViewWarp.setNoMore(true);
					} else {
						listViewWarp.setNoMore(false);
					}
				}
			}, RiverRecordListRes.class, ParamUtils.freeParam(null, "riverID", river.riverId,
					"month", month, "year", year,
					"authority", ((BaseActivity) context).getUser().getAuthority()));
		}
		return true;
	}
}
