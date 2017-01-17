package com.zju.hzsz.chief.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.Duban;
import com.zju.hzsz.model.DubanListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.DipPxUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

public class ChiefDubanListActivity extends BaseActivity {
	private ListViewWarp listViewWarp = null;
	private List<Duban> items = new ArrayList<Duban>();
	private SimpleListAdapter adapter = null;

	private View.OnClickListener btnClk = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof Duban) {
				Duban duban = (Duban) v.getTag();
				Intent intent = new Intent(ChiefDubanListActivity.this, ChiefDubanDetailActivity.class);
				intent.putExtra(Tags.TAG_DUBAN, StrUtils.Obj2Str(duban));
				// startActivity(intent);
				startActivityForResult(intent, Tags.CODE_DUBAN);
			}
		}
	};

	private View.OnClickListener hndClk = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof Duban) {
				Duban duban = (Duban) v.getTag();
				Intent intent = new Intent(ChiefDubanListActivity.this, ChiefCompDetailActivity.class);

				intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(duban));
				intent.putExtra(Tags.TAG_ISCOMP, true);
				intent.putExtra(Tags.TAG_HANDLED, false);

				startActivityForResult(intent, Tags.CODE_COMP);
			}
		}
	};

	private ViewRender viewRender = new ViewRender();

	private SimpleViewInitor initor = new SimpleViewInitor() {
		@Override
		public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
			if (convertView == null) {
				convertView = LinearLayout.inflate(ChiefDubanListActivity.this, R.layout.item_chief_duban, null);
			}

			Duban duban = (Duban) data;
			viewRender.renderView(convertView, duban);

			((Button) convertView.findViewById(R.id.btn_viewduban)).setTag(duban);
			((Button) convertView.findViewById(R.id.btn_viewduban)).setOnClickListener(btnClk);

			((Button) convertView.findViewById(R.id.btn_handle)).setTag(duban);
			((Button) convertView.findViewById(R.id.btn_handle)).setOnClickListener(hndClk);
			int status = duban.getStatus();
			boolean show = status == 1 || status == 2 || status == 4 || status == 5;
			convertView.findViewById(R.id.btn_handle).setVisibility(show ? View.VISIBLE : View.GONE);
			return convertView;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_dubanlist);
		setTitle("督办单列表");
		initHead(R.drawable.ic_head_back, 0);

		adapter = new SimpleListAdapter(this, items, initor);
		listViewWarp = new ListViewWarp(this, adapter, new ListViewWarp.WarpHandler() {

			@Override
			public boolean onRefresh() {
				loadDate(true);
				return true;
			}

			@Override
			public boolean onLoadMore() {
				loadDate(false);
				return true;
			}
		});
		listViewWarp.getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.bg_gray)));
		listViewWarp.getListView().setDividerHeight(DipPxUtils.dip2px(this, getResources().getDimension(R.dimen.padding_medium)));
		((LinearLayout) findViewById(R.id.ll_main)).addView(listViewWarp.getRootView());

		loadDate(true);
	}

	private void loadDate(final boolean refresh) {
		if (refresh)
			listViewWarp.setRefreshing(true);
		else
			listViewWarp.setLoadingMore(true);
		getRequestContext().add("Get_Duban_List", new Callback<DubanListRes>() {
			@Override
			public void callback(DubanListRes o) {
				listViewWarp.setRefreshing(false);
				listViewWarp.setLoadingMore(false);

				if (o != null && o.isSuccess()) {
					if (refresh)
						items.clear();
					for (Duban d : o.data) {
						items.add(d);
					}
					adapter.notifyDataSetChanged();

					listViewWarp.setNoMore(true);
				}
			}
		}, DubanListRes.class, ParamUtils.freeParam(null));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			loadDate(true);
		}
	}
}
