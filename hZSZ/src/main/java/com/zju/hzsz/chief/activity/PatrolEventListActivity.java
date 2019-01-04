package com.zju.hzsz.chief.activity;

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
import com.zju.hzsz.model.PatrolEvent;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ArrUtils;
import com.zju.hzsz.utils.DipPxUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;
import com.zju.hzsz.view.ListViewWarp.WarpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatrolEventListActivity extends BaseActivity implements
		OnPageChangeListener, OnCheckedChangeListener {
	public class CompListPager extends PagerItem implements WarpHandler {
		private View view = null;
		private int type = 0;
		private ListViewWarp listViewWarp = null;
		private List<PatrolEvent> items = new ArrayList<PatrolEvent>();
		private SimpleListAdapter adapter = null;

		//处理投诉单按钮
		private View.OnClickListener btnClk = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() instanceof PatrolEvent) {
					PatrolEvent comp = (PatrolEvent) v.getTag();
					Intent intent = new Intent(PatrolEventListActivity.this,
							PatrolEventDetailActivity.class);

					intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
//					intent.putExtra(Tags.TAG_ISCOMP, true);

					intent.putExtra(Tags.TAG_HANDLED, type != 0);    //0是未处理，1是已处理

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
							PatrolEventListActivity.this,
							R.layout.item_chief_complaint2, null);
				}
				ViewWarp warp = new ViewWarp(convertView,
						PatrolEventListActivity.this);
				PatrolEvent comp = (PatrolEvent) data;
				warp.setText(R.id.tv_status, type!=0?"已处理":"未处理");  //处理状态
				warp.setText(R.id.tv_sernum, comp.eventSerNum);   //编号
				warp.setText(R.id.tv_title,"问题河湖："+comp.riverName);
				warp.setText(R.id.tv_content, "上报人："+comp.create_userName);
				warp.setText(R.id.tv_time, comp.upload_time != null ? comp
						.upload_time.getYMDHM(PatrolEventListActivity.this) : "");

				if (type == 0) {
					((Button) warp.getViewById(R.id.btn_handle))
							.setText(isComp ? "处理问题" : "处理建议");         //0是未处理，1是已处理
				} else {
					((Button) warp.getViewById(R.id.btn_handle))
							.setText("查看已处理问题");
				}
				((Button) warp.getViewById(R.id.btn_handle)).setTag(comp);
				((Button) warp.getViewById(R.id.btn_handle))
						.setOnClickListener(btnClk);
				return convertView;
			}
		};

		//CompListPager类的构造函数
		public CompListPager(int type) {
			super();
			this.type = type;
		}

		@Override
		public View getView() {
			if (view == null) {
				view = LinearLayout.inflate(PatrolEventListActivity.this,
						R.layout.confs_chief_complist, null);
				adapter = new SimpleListAdapter(PatrolEventListActivity.this,
						items, initor);
				listViewWarp = new ListViewWarp(PatrolEventListActivity.this,
						adapter, this);
				listViewWarp.getListView().setDivider(
						new ColorDrawable(getResources().getColor(
								R.color.bg_gray)));
				listViewWarp.getListView().setDividerHeight(
						DipPxUtils.dip2px(
								PatrolEventListActivity.this,
								getResources().getDimension(
										R.dimen.padding_medium)));
				((LinearLayout) view.findViewById(R.id.ll_main))
						.addView(listViewWarp.getRootView());

				loadComps(true);
			}
			return view;
		}

		//下拉刷新
		@Override
		public boolean onRefresh() {
			loadComps(true);
			return true;
		}

		//上拉加载
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
					"Get_Event_List",
					new Callback<ChiefCompListRes>() {
						@Override
						public void callback(ChiefCompListRes o) {
							listViewWarp.setRefreshing(false);
							listViewWarp.setLoadingMore(false);

							if (o != null && o.isSuccess()) {
								if (refresh)
									items.clear();
								for (PatrolEvent c :  o.data.patrolEvents) {
									items.add(c);
								}
								adapter.notifyDataSetChanged();

								//无更多
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

	private boolean isComp = true; //默认值为true
	private int[] rdids = new int[] { R.id.rb_chief_comp_unhandle,
			R.id.rb_chief_comp_handled };
	private List<PagerItem> pagerItems = null;
	private SimplePagerAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_complist);
		setTitle("问题处理");

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
