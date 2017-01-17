package com.zju.hzsz.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.CompSugs;
import com.zju.hzsz.model.CompDataRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.DipPxUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;
import com.zju.hzsz.view.ListViewWarp.WarpHandler;

public class CompListActivity extends BaseActivity implements WarpHandler {
	ListViewWarp lvw = null;
	List<CompSugs> comps = new ArrayList<CompSugs>();
	SimpleListAdapter adapter = null;
	private boolean isComp = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complist);

		isComp = getIntent().getBooleanExtra(Tags.TAG_ABOOLEAN, true);

		if (isComp) {
			setTitle(R.string.mycomplaint);
		} else {
			setTitle(R.string.mysuggestion);
		}
		initHead(R.drawable.ic_head_back, 0);

		adapter = new SimpleListAdapter(this, comps, new SimpleViewInitor() {

			@Override
			public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
				if (convertView == null) {

					convertView = LinearLayout.inflate(CompListActivity.this, R.layout.item_complaint, null);
				}
				CompSugs comp = (CompSugs) data;
				ViewWarp warp = new ViewWarp(convertView, CompListActivity.this);
				warp.setText(R.id.tv_title, comp.getTheme());
				warp.setText(R.id.tv_content, comp.getContent());
				warp.setText(R.id.tv_sernum, comp.getNum());
				boolean showevl = comp.getStatus() >= 3;
				// showevl = true;
				if (comp.getStatus() == 1) {
					// 待受理
				} else if (comp.getStatus() == 2) {
					// 已受理
				} else if (comp.getStatus() == 3 || comp.getStatus() == 6) {
					// 已处理, 或者已经追加处理
					warp.setText(R.id.btn_evaluatenow, R.string.evaluatenow);
				} else if (comp.getStatus() >= 4) {
					// 已评价
					warp.setText(R.id.btn_evaluatenow, R.string.evaluated);
				}

				warp.getViewById(R.id.btn_evaluatenow).setTag(comp);
				warp.getViewById(R.id.btn_evaluatenow).setVisibility(showevl ? View.VISIBLE : View.GONE);
				warp.getViewById(R.id.btn_evaluatenow).setOnClickListener(btnClk);

				warp.getViewById(R.id.btn_vieworder).setTag(comp);
				warp.getViewById(R.id.btn_vieworder).setOnClickListener(btnClk);

				convertView.setTag(comp);
				convertView.setOnClickListener(btnClk);

				return convertView;
			}
		});

		lvw = new ListViewWarp(this, adapter, this);

		((LinearLayout) findViewById(R.id.ll_main)).removeAllViews();

		((LinearLayout) findViewById(R.id.ll_main)).addView(lvw.getRootView());
		lvw.getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.bg_gray)));
		lvw.getListView().setDividerHeight(DipPxUtils.dip2px(this, getResources().getDimension(R.dimen.padding_medium)));
		loadComps(true);
	}

	private View.OnClickListener btnClk = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof CompSugs) {
				CompSugs comp = (CompSugs) v.getTag();
				if (v.getId() == R.id.btn_evaluatenow) {
					Intent intent = new Intent(CompListActivity.this, EvalCompActivity.class);
					intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
					startActivityForResult(intent, Tags.CODE_COMP);
				} else if (v.getId() == R.id.btn_vieworder) {
					Intent intent = new Intent(CompListActivity.this, CompDetailActivity.class);
					intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
					startActivity(intent);
				} else {
					Log.e("NET", "clk" + comp);
				}
			}
		}
	};

	@Override
	public boolean onRefresh() {
		loadComps(true);
		return true;
	}

	@Override
	public boolean onLoadMore() {
		loadComps(false);
		return true;
	}

	private void loadComps(final boolean refresh) {
		if (refresh)
			lvw.setRefreshing(true);
		else
			lvw.setLoadingMore(true);
		getRequestContext().add(isComp ? "complaints_list_get" : "advice_list_get", new Callback<CompDataRes>() {
			@Override
			public void callback(CompDataRes o) {
				lvw.setRefreshing(false);
				lvw.setLoadingMore(false);

				if (o != null && o.isSuccess()) {
					CompSugs[] list = o.data.complaints != null ? o.data.complaints : o.data.advises;
					if (list != null) {
						if (refresh) {
							comps.clear();
						}
						for (CompSugs c : list) {
							comps.add(c);
						}
						if (o.data.pageInfo.isNoMore())
							lvw.setNoMore(true);

						adapter.notifyDataSetChanged();
					}
				}
			}
		}, CompDataRes.class, getPageParam(refresh));
	}

	private final int DefaultPageSize = Constants.DefaultPageSize;

	protected JSONObject getPageParam(boolean refresh) {
		return refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (comps.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Tags.CODE_COMP && resultCode == RESULT_OK) {
			// 重新加载
			loadComps(true);
		}
	}
}
