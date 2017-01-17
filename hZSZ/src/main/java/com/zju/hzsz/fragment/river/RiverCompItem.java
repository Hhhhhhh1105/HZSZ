package com.zju.hzsz.fragment.river;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.activity.CompDetailActivity;
import com.zju.hzsz.model.CompPublicity;
import com.zju.hzsz.model.CompPublicitysRes;
import com.zju.hzsz.model.CompSugs;
import com.zju.hzsz.model.River;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

public class RiverCompItem extends BaseRiverPagerItem {
	private ListViewWarp listViewWarp = null;
	private SimpleListAdapter adapter = null;
	private List<CompPublicity> list = new ArrayList<CompPublicity>();

	public RiverCompItem(River river, BaseActivity context) {
		super(river, context);
	}

	@Override
	public View getView() {
		if (view == null) {
			adapter = new SimpleListAdapter(context, list, new SimpleViewInitor() {

				@Override
				public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
					if (convertView == null) {
						convertView = LinearLayout.inflate(context, R.layout.item_river_comppublicity, null);
					}
					((BaseActivity) context).getViewRender().renderView(convertView, data);

					CompPublicity cp = (CompPublicity) data;
					((ImageView) convertView.findViewById(R.id.iv_status)).setImageResource(cp.isHandled() ? R.drawable.im_cp_handled : R.drawable.im_cp_unhandle);

					((TextView) convertView.findViewById(R.id.tv_status)).setTextColor(context.getResources().getColor(cp.isHandled() ? R.color.green : R.color.red));

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
						CompPublicity cp = list.get(pos);
						CompSugs comp = new CompSugs();
						comp.complaintsId = cp.getId();
						comp.complaintsPicPath = cp.getCompPicPath();
						comp.compStatus = cp.compStatus;
						comp.compTheme = cp.compTheme;
						
						Intent intent = new Intent(context, CompDetailActivity.class);
						intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
						intent.putExtra(Tags.TAG_ABOOLEAN, true);
						context.startActivity(intent);
					}
				}
			});

			listViewWarp.startRefresh();

			view = listViewWarp.getRootView();
		}
		return view;
	}

	private boolean loadData(final boolean refresh) {
		if (refresh)
			listViewWarp.setRefreshing(true);
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

					adapter.notifyDataSetChanged();
				}
				if (list.size() == 0)
					listViewWarp.setNoMore(true);
			}
		}, CompPublicitysRes.class, ParamUtils.freeParam(null, "riverId", river.riverId));
		return true;
	}
}
