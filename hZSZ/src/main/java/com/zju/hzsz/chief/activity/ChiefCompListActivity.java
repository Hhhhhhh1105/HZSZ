package com.zju.hzsz.chief.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;

import com.zju.hzsz.adapter.PagerItem;
import com.zju.hzsz.adapter.SimplePagerAdapter;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.ChiefComp;
import com.zju.hzsz.model.ChiefCompListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ArrUtils;
import com.zju.hzsz.utils.DipPxUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;
import com.zju.hzsz.view.ListViewWarp.WarpHandler;

public class ChiefCompListActivity extends BaseActivity implements
		OnPageChangeListener, OnCheckedChangeListener {
	public class CompListPager extends PagerItem implements WarpHandler {
		private View view = null;
		private int type = 0;
		private ListViewWarp listViewWarp = null;
		private List<ChiefComp> items = new ArrayList<ChiefComp>();
		private SimpleListAdapter adapter = null;

		private View.OnClickListener btnClk = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() instanceof ChiefComp) {
					ChiefComp comp = (ChiefComp) v.getTag();
					Intent intent = new Intent(ChiefCompListActivity.this,
							ChiefCompDetailActivity.class);

					intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
					intent.putExtra(Tags.TAG_ISCOMP, isComp);
					intent.putExtra(Tags.TAG_HANDLED, type != 0);

					startActivityForResult(intent, Tags.CODE_COMP);
				}
			}
		};

		private SimpleViewInitor initor = new SimpleViewInitor() {
			@Override
			public View initView(Context context, int position,
					View convertView, ViewGroup parent, Object data) {
				if (convertView == null) {
					convertView = LinearLayout.inflate(
							ChiefCompListActivity.this,
							R.layout.item_chief_complaint2, null);
				}
				ViewWarp warp = new ViewWarp(convertView,
						ChiefCompListActivity.this);
				ChiefComp comp = (ChiefComp) data;
				warp.setText(R.id.tv_status, comp.getStatuss());
				warp.setText(R.id.tv_sernum, comp.getSerNum());
				warp.setText(R.id.tv_title, comp.getTheme());
				warp.setText(R.id.tv_content, comp.getContent());
				warp.setText(R.id.tv_time, comp.getDate() != null ? comp
						.getDate().getYMDHM(ChiefCompListActivity.this) : "");

				if (type == 0) {
					((Button) warp.getViewById(R.id.btn_handle))
							.setText(isComp ? "处理投诉" : "处理建议");
				} else {
					((Button) warp.getViewById(R.id.btn_handle))
							.setText("查看处理单");
				}
				((Button) warp.getViewById(R.id.btn_handle)).setTag(comp);
				((Button) warp.getViewById(R.id.btn_handle))
						.setOnClickListener(btnClk);
				return convertView;
			}
		};

		public CompListPager(int type) {
			super();
			this.type = type;
		}

		@Override
		public View getView() {
			if (view == null) {
				view = LinearLayout.inflate(ChiefCompListActivity.this,
						R.layout.confs_chief_complist, null);
				adapter = new SimpleListAdapter(ChiefCompListActivity.this,
						items, initor);
				listViewWarp = new ListViewWarp(ChiefCompListActivity.this,
						adapter, this);
				listViewWarp.getListView().setDivider(
						new ColorDrawable(getResources().getColor(
								R.color.bg_gray)));
				listViewWarp.getListView().setDividerHeight(
						DipPxUtils.dip2px(
								ChiefCompListActivity.this,
								getResources().getDimension(
										R.dimen.padding_medium)));
				((LinearLayout) view.findViewById(R.id.ll_main))
						.addView(listViewWarp.getRootView());

				loadComps(true);
			}
			return view;
		}

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
				listViewWarp.setRefreshing(true);
			else
				listViewWarp.setLoadingMore(true);
			getRequestContext().add(
					isComp ? "Get_ChiefComplain_List" : "Get_ChiefAdvise_List",
					new Callback<ChiefCompListRes>() {
						@Override
						public void callback(ChiefCompListRes o) {
							listViewWarp.setRefreshing(false);
							listViewWarp.setLoadingMore(false);

							if (o != null && o.isSuccess()) {
								if (refresh)
									items.clear();
								for (ChiefComp c : isComp ? o.data.complainSum
										: o.data.adviseSum) {
									items.add(c);
								}
								adapter.notifyDataSetChanged();

								if (items.size() >= o.data.pageInfo.totalCounts) {
									listViewWarp.setNoMore(true);
								}
							}
						}
					}, ChiefCompListRes.class, getPageParam(refresh));
		}

		private final int DefaultPageSize = Constants.DefaultPageSize;

		protected JSONObject getPageParam(boolean refresh) {
			JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1)
					: ParamUtils.pageParam(DefaultPageSize, (items.size()
							+ DefaultPageSize - 1)
							/ DefaultPageSize + 1);
			try {
				j.put("ifDeal", type);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return j;
		}
	}

	private boolean isComp = true;
	private int[] rdids = new int[] { R.id.rb_chief_comp_unhandle,
			R.id.rb_chief_comp_handled };
	private List<PagerItem> pagerItems = null;
	private SimplePagerAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_complist);

		isComp = getIntent().getBooleanExtra(Tags.TAG_ABOOLEAN, true);

		if (isComp) {
			setTitle(R.string.mychiefcomplaint);
		} else {
			setTitle(R.string.mychiefsuggestion);
		}
		initHead(R.drawable.ic_head_back, 0);

		((ViewPager) findViewById(R.id.vp_chief_comp_tab))
				.setOnPageChangeListener(this);
		((RadioGroup) findViewById(R.id.rg_chief_comp))
				.setOnCheckedChangeListener(this);

		pagerItems = new ArrayList<PagerItem>();
		pagerItems.add(new CompListPager(0));
		pagerItems.add(new CompListPager(1));
		adapter = new SimplePagerAdapter(pagerItems);
		((ViewPager) findViewById(R.id.vp_chief_comp_tab)).setAdapter(adapter);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int ix) {
		((RadioButton) findViewById(rdids[ix])).setChecked(true);
		// ((CompListPager) pagerItems.get(((ViewPager)
		// findViewById(R.id.vp_chief_comp_tab)).getCurrentItem())).readyView();
	}

	@Override
	public void onCheckedChanged(RadioGroup rg, int rdid) {
		int ix = ArrUtils.indexOf(rdids, rdid);
		if (ix >= 0) {
			((ViewPager) findViewById(R.id.vp_chief_comp_tab))
					.setCurrentItem(ix);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			((CompListPager) pagerItems
					.get(((ViewPager) findViewById(R.id.vp_chief_comp_tab))
							.getCurrentItem())).loadComps(true);
		}
	}
}
