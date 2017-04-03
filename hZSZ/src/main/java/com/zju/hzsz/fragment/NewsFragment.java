package com.zju.hzsz.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.activity.NewsDetailActivity;
import com.zju.hzsz.activity.SearchNewsActivity;
import com.zju.hzsz.adapter.PagerItem;
import com.zju.hzsz.model.District;
import com.zju.hzsz.model.News;
import com.zju.hzsz.model.NewsDataRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻公告
 * 
 * @author Robin
 * 
 */
public class NewsFragment extends TabPagerListFragment {

	class NewsPagerItem extends TabPagerListFragment.ListPagerItem {
		private String newsType;

		public NewsPagerItem(SimpleViewInitor initor, String newsType) {
			super(initor);
			this.newsType = newsType;
		}

		//onRefresh时状态为ture，onLoadMore时状态为false
		@Override
		protected void loadData(final boolean refresh) {
			JSONObject p = getPageParam(refresh);//refresh为true加载默认条数当前数据为第一页，false时当前数据为当前页
			try {
				p.put("newsType", newsType);
				p.put("districtId", curDistrict == null ? getBaseActivity().getUser().getDistrictId() : curDistrict.districtId);
				p.put("longtitude", getBaseActivity().getLongitude());
				p.put("latitude", getBaseActivity().getLatitude());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			getRequestContext().add("info_list_get", new Callback<NewsDataRes>() {
				@Override
				public void callback(NewsDataRes o) {
					setLoadResult(refresh, o != null && o.isSuccess(), o != null && o.data != null ? o.data.pageInfo : null, o != null && o.data != null ? o.data.newsJsons : null);
				}
			}, NewsDataRes.class, p);
		}

		@Override
		protected void onViewInited() {
			super.onViewInited();
			this.listViewWarp.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					if (position < datas.size()) {
						Intent intent = new Intent(getBaseActivity(), NewsDetailActivity.class);
						News news = (News) datas.get(position);
						intent.putExtra(Tags.TAG_NEWS, StrUtils.Obj2Str(news));
						startActivity(intent);

						if (!news.isReaded(getBaseActivity().getUser())) {
							news.setReaded(getBaseActivity().getUser());
							adapter.notifyDataSetChanged();
						}
					}
				}
			});
		}

		public void startRefresh() {
			listViewWarp.setRefreshing(true);
			loadData(true);
		}
	}

	public NewsFragment() {
		super(R.layout.fragment_news, R.id.vp_news, R.id.rg_newstype);
	}

	@Override
	protected int[] getTabIds() {
		return new int[] { R.id.rb_news, R.id.rb_notice, R.id.rb_dayqa };
	}

	private List<PagerItem> pagerItems = null;

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		// getRootViewWarp().setHeadTabText(R.string.news_news,
		// R.string.news_notice);
		//Log.i("latiFromBA", Double.toString(getBaseActivity().getLongitude()));
		getRootViewWarp().setHeadImage(R.drawable.ic_head_search, R.drawable.ic_head_order);
		getRootViewWarp().setHeadTitle("新闻公告");
		getRootViewWarp().getViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (pagerItems != null) {
					// ((NewsPagerItem)
					// pagerItems.get(vpPagers.getCurrentItem())).startRefresh();
					showAreaPop();
				}
			}
		});

		getRootViewWarp().getViewById(R.id.iv_head_left).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//根据当前的页面选择需要进行搜索的新闻类型
				int newsType = 1;
				int currentItem = vpPagers.getCurrentItem();
				switch (currentItem){
					case 0:
						newsType = 1;
						break;
					case 1:
						newsType = 2;
						break;
					case 2:
						newsType = 6;
						break;
					default:
						newsType = 1;
						break;
				}

				Intent intent = new Intent(getBaseActivity(), SearchNewsActivity.class);
				intent.putExtra("newsType", newsType);
				startActivity(intent);
			}
		});

		return rootView;
	}

	@Override
	protected List<PagerItem> getPagerItems() {
		if (pagerItems == null) {
			pagerItems = new ArrayList<PagerItem>();

			SimpleViewInitor initor = new SimpleViewInitor() {

				@Override
				public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
					News n = (News) data;
					if (convertView == null) {
						convertView = LinearLayout.inflate(context, R.layout.item_news, null);
					}
					((TextView) convertView.findViewById(R.id.tv_distname)).setText(n.rePeople);
					((TextView) convertView.findViewById(R.id.tv_news_title)).setText(n.theme);
					((TextView) convertView.findViewById(R.id.tv_news_article)).setText(n.abstarct == null ? "" : n.abstarct);
					ImgUtils.loadImage(context, (ImageView) convertView.findViewById(R.id.iv_picture), StrUtils.getImgUrl(n.picPath));

					int textcolor = getActivity().getResources().getColor(n.isReaded(getBaseActivity().getUser()) ? R.color.lightgray : R.color.black);
					((TextView) convertView.findViewById(R.id.tv_news_title)).setTextColor(textcolor);
					((TextView) convertView.findViewById(R.id.tv_news_article)).setTextColor(textcolor);
					return convertView;
				}
			};

			// pagerItems.add(new NewsPagerItem(initor, "info_list_get"));
			// pagerItems.add(new NewsPagerItem(initor, "notice_list_get"));
			// pagerItems.add(new NewsPagerItem(initor, "notice_list_get"));

			pagerItems.add(new NewsPagerItem(initor, "1"));
			pagerItems.add(new NewsPagerItem(initor, "2"));
			pagerItems.add(new NewsPagerItem(initor, "6"));

			// ViewUtils.bindTabsViewPagers((RadioGroup)f, vpPagers, new int[] {
			// R.id.rb_news, R.id.rb_notice, R.id.rb_dayqa });

			if (Values.districtLists == null || Values.districtLists.length == 0) {
				getBaseActivity().showToast("没有选区列表数据");
			}
		}
		return pagerItems;
	}

	private PopupWindow areaPop = null;
	private View areaView = null;
	private District curDistrict = null;

	private void showArea() {
		//相对控件的位置（正左下方），无偏移
		areaPop.showAsDropDown(getBaseActivity().findViewById(R.id.v_poptag));
		rootView.findViewById(R.id.v_mask).setVisibility(View.VISIBLE);
	}

	private void dismissArea() {
		areaPop.dismiss();
		rootView.findViewById(R.id.v_mask).setVisibility(View.GONE);
	}

	//若点击右上方按键，则执行此函数
	private void showAreaPop() {
		if (areaPop == null) {
			areaView = LinearLayout.inflate(getBaseActivity(), R.layout.inc_arealist, null);
			LinearLayout ll_areas = (LinearLayout) areaView.findViewById(R.id.ll_areas);

			//为popupwindow中的每个item绑定的监听器，点击之后设置curDistrict，并刷新viewpager界面，重新loadData
			View.OnClickListener clk = new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dismissArea();
					curDistrict = (District) arg0.getTag();
					((NewsPagerItem) pagerItems.get(vpPagers.getCurrentItem())).startRefresh();
				}
			};
			District[] districtLists = null;
			//在MainFragment就已经给Values.districtLists赋值
			if (Values.districtLists != null) {
				districtLists = new District[Values.districtLists.length + 1];
				District all = new District();
				all.districtId = 0;
				all.districtName = "全市";
				districtLists[0] = all;
				for (int i = 0; i < Values.districtLists.length; ++i) {
					districtLists[i + 1] = Values.districtLists[i];
				}
			}

			//将各个区信息添加至线性布局中
			for (District d : districtLists) {
				View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_ranking_area, null);
				((TextView) view.findViewById(R.id.tv_name)).setText(d.districtName);
				view.setOnClickListener(clk);
				view.setTag(d);
				ll_areas.addView(view);
			}

			ColorDrawable cd = new ColorDrawable(getBaseActivity().getResources().getColor(R.color.gray));
			//areaView为要显示的View
			areaPop = new PopupWindow(areaView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			areaPop.setOutsideTouchable(true);
			areaPop.setFocusable(false);
			areaPop.update();
			areaPop.setBackgroundDrawable(cd);
			//设置此项则下面的捕获window外touch事件就无法触发
			areaPop.setTouchInterceptor(new View.OnTouchListener() {

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
