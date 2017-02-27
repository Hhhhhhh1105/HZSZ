package com.zju.hzsz.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Values;
import com.zju.hzsz.adapter.PagerItem;
import com.zju.hzsz.adapter.SimplePagerAdapter;
import com.zju.hzsz.clz.RootViewWarp;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.District;
import com.zju.hzsz.model.Ranking;
import com.zju.hzsz.model.RankingRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ArrUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class TabRankingFragment extends BaseFragment implements OnPageChangeListener, OnCheckedChangeListener {
	//排行版页面：listView部分
	class RankingPager extends PagerItem implements OnRefreshListener {
		View view = null;
		private Context context;
		private int typeid;//没用
		private boolean sortType = true; //排序方式：升序or降序
		private int sortParamsId = 1; //排序参数 1- 2- 3-
		List<Ranking> rankings = new ArrayList<Ranking>();//数据列表
		private SimpleListAdapter adapter = null;//listView所用适配器

		public RankingPager(Context context, int typeid) {
			super();
			this.context = context;
			this.typeid = typeid;
		}

		@Override
		public View getView() {
			if (view == null) {
				view = LinearLayout.inflate(context, R.layout.fragment_ranking2_item, null);

				SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_listview);
				ViewUtils.setSwipeRefreshLayoutColorScheme(swipeRefreshLayout);

				swipeRefreshLayout.setOnRefreshListener(this);

				ListView lv_ranking = (ListView) view.findViewById(R.id.lv_ranking);
				adapter = new SimpleListAdapter(getBaseActivity(), rankings, new SimpleViewInitor() {

					@Override
					public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
						if (convertView == null) {
							convertView = LinearLayout.inflate(context, R.layout.item_ranking, null);
						}
						Ranking r = (Ranking) data;
						ViewWarp warp = new ViewWarp(convertView, context);
						warp.setText(R.id.tv_index, (position + 1) + "");
						warp.setText(R.id.tv_name, r.riverName);
						warp.setText(R.id.tv_complaint_count, "" + r.complaintsSum);
						warp.setText(R.id.tv_satisfaction_count, "" + ((int) (r.satisfactionRatio * 100)) + "%");
						warp.setText(R.id.tv_unhandle, "" + r.comUnDeal);
						return convertView;
					}
				});
				lv_ranking.setAdapter(adapter);

				View.OnClickListener clk = new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						int sbtype = 1;
						switch (arg0.getId()) {
						case R.id.tv_complaint_count:
							sbtype = 1;
							break;
						case R.id.tv_satisfaction_count:
							sbtype = 2;
							break;
						case R.id.tv_unhandle:
							sbtype = 3;
							break;
						default:
							break;
						}
						if (sortParamsId == sbtype) {
							sortType = !sortType; //改变排序方式
						} else {
							sortParamsId = sbtype; //改变排序标准
						}
						onRefresh(); //点击之后需刷新页面
					}
				};

				view.findViewById(R.id.tv_complaint_count).setOnClickListener(clk);
				view.findViewById(R.id.tv_satisfaction_count).setOnClickListener(clk);
				view.findViewById(R.id.tv_unhandle).setOnClickListener(clk);

				onRefresh(); //初始要刷新页面
			}
			return view;
		}

		@Override
		public void onRefresh() {
			setRefreshing(true);

			getRequestContext().add("complaints_rankinglist_get", new Callback<RankingRes>() {

				@Override
				public void callback(RankingRes o) {
					if (o != null && o.isSuccess()) {
						rankings.clear();
						for (Ranking r : o.data) {
							rankings.add(r);
						}
						adapter.notifyDataSetChanged();
					}
					setRefreshing(false);
				}

			}, RankingRes.class, ParamUtils.freeParam(null, "timePeriod", typeid, "districtId",
					curDistrict.districtId, "sortParamsId", sortParamsId, "sortType", sortType ? 0 : 1,
					"latitude", getBaseActivity().getLatitude(), "longitude", getBaseActivity().getLongitude()));
		}

		private void setRefreshing(boolean b) {
			((SwipeRefreshLayout) view.findViewById(R.id.srl_listview)).setRefreshing(b);
		}
	}

	private List<PagerItem> pagerItems = null;
	private SimplePagerAdapter pagerAdapter = null;
	private int[] rdids = new int[] { R.id.rb_thismonth, R.id.rb_lastmonth }; //已经无用

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
//			rootView = inflater.inflate(R.layout.fragment_ranking2, container, false);
			rootView = inflater.inflate(R.layout.fragment_ranking3, container, false);

			if (Values.districtLists == null || Values.districtLists.length == 0) {
				getBaseActivity().showToast("没有选区列表数据");
				return rootView;
			}

			RootViewWarp warp = getRootViewWarp();
			warp.setHeadImage(0, R.drawable.ic_head_order);
			warp.setHeadTitle(R.string.ranking);

			warp.getViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					showAreaPop();
				}
			});

			//排行版一开始默认是拱墅区的排名情况
			for (District d : Values.districtLists) {
				if (d.districtName.contains("拱墅")) {
					curDistrict = d;
				}
			}
			if (curDistrict == null)
				curDistrict = Values.districtLists[0];

			getRootViewWarp().setText(R.id.tv_rankingtitle, StrUtils.renderText(getBaseActivity(), R.string.fmt_arearanking, curDistrict.districtName));

			pagerItems = new ArrayList<PagerItem>();
			pagerItems.add(new RankingPager(getBaseActivity(), 0));
//			pagerItems.add(new RankingPager(getBaseActivity(), 1));

			// pagerItems.add(new RiverPositionItem(river, this));
			((RadioGroup) rootView.findViewById(R.id.rg_ranking_date)).setOnCheckedChangeListener(this);
			((ViewPager) rootView.findViewById(R.id.vp_ranking_tab)).setOnPageChangeListener(this);
			pagerAdapter = new SimplePagerAdapter(pagerItems);
			((ViewPager) rootView.findViewById(R.id.vp_ranking_tab)).setAdapter(pagerAdapter);
		}
		return rootView;
	}

	//去掉本月和上月选项后这几个函数没有用
	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int ix) {
		((RadioButton) rootView.findViewById(rdids[ix])).setChecked(true);
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int rdid) {
		int ix = ArrUtils.indexOf(rdids, rdid);
		if (ix >= 0 && ((ViewPager) rootView.findViewById(R.id.vp_ranking_tab)).getCurrentItem() != ix) {
			// pagerItems.get(ix).getView();
			((ViewPager) rootView.findViewById(R.id.vp_ranking_tab)).setCurrentItem(ix);
		}
	}

	private PopupWindow areaPop = null;
	private View areaView = null;
	private District curDistrict = null;

	//出现弹窗与背景
	private void showArea() {
		areaPop.showAsDropDown(getBaseActivity().findViewById(R.id.v_poptag));
		rootView.findViewById(R.id.v_mask).setVisibility(View.VISIBLE);
	}

	//解除弹窗与背景
	private void dismissArea() {
		areaPop.dismiss();
		rootView.findViewById(R.id.v_mask).setVisibility(View.GONE);
	}

	//实现弹窗的主要函数
	public void showAreaPop() {
		if (areaPop == null) {
			areaView = LinearLayout.inflate(getBaseActivity(), R.layout.inc_arealist, null);
			LinearLayout ll_areas = (LinearLayout) areaView.findViewById(R.id.ll_areas);

			//点击弹窗中的textView之后更新排行榜
			View.OnClickListener clk = new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dismissArea();
					curDistrict = (District) arg0.getTag(); //这是数据改变的重点所在
					getRootViewWarp().setText(R.id.tv_rankingtitle, StrUtils.renderText(getBaseActivity(), R.string.fmt_arearanking, curDistrict.districtName));
					for (PagerItem rp : pagerItems) {
						((RankingPager) rp).onRefresh();//adapter.notifyDataSetChanged()改变的是listView中的数据，而不包括标题
					}
				}
			};

			//添加弹窗VIEW并绑定监听器，通过setTag方法实现数据传输
			for (District d : Values.districtLists) {
				View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_ranking_area, null);
				((TextView) view.findViewById(R.id.tv_name)).setText(d.districtName);
				view.setOnClickListener(clk);
				view.setTag(d);
				ll_areas.addView(view);
			}

			ColorDrawable cd = new ColorDrawable(getBaseActivity().getResources().getColor(R.color.gray));
			areaPop = new PopupWindow(areaView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			areaPop.setOutsideTouchable(true);
			areaPop.setFocusable(false);
			areaPop.update();
			areaPop.setBackgroundDrawable(cd);
			areaPop.setTouchInterceptor( new View.OnTouchListener() {

				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					if (arg1.getAction() == MotionEvent.ACTION_OUTSIDE) {
						dismissArea();
						return false;
					} else
						return false;
				}
			});
		}
		showArea();
	}
}
