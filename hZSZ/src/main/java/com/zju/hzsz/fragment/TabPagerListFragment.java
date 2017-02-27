package com.zju.hzsz.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.adapter.PagerItem;
import com.zju.hzsz.adapter.SimplePagerAdapter;
import com.zju.hzsz.model.PageInfo;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.view.ListViewWarp;
import com.zju.hzsz.view.ListViewWarp.ListStatus;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 断面水质
 * 
 * @author Robin
 * 
 */
public abstract class TabPagerListFragment extends BaseFragment implements OnCheckedChangeListener, OnPageChangeListener {
	public abstract class ListPagerItem extends PagerItem {
		private View view;
		protected ListViewWarp listViewWarp = null;
		protected List<Object> datas = null;
		protected SimpleListAdapter adapter;  //ListView的适配器
		private SimpleViewInitor initor; //该类中的initView()方法实现适配器中的getView()方法

		private final int DefaultPageSize = Constants.DefaultPageSize; //20
//		private final int DefaultPageSize = 10; //20

		public ListPagerItem(SimpleViewInitor initor) {
			this.initor = initor;
		}

		@Override
		public View getView() {
			if (view == null) {
				datas = new ArrayList<Object>();
				adapter = new SimpleListAdapter(getBaseActivity(), datas, initor);
				listViewWarp = new ListViewWarp(getBaseActivity(), adapter, new ListViewWarp.WarpHandler() {

					@Override
					public boolean onRefresh() {
						loadData(true);
						return true;
					}

					@Override
					public boolean onLoadMore() {
						loadData(false);
						return true;
					}
				});
				view = listViewWarp.getRootView();
				listViewWarp.startLoadMore();

				onViewInited();
			}
			return view;
		}

		protected void onViewInited() {

		}

		abstract protected void loadData(final boolean refresh);

		protected JSONObject getPageParam(boolean refresh) {
			return refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (datas.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
		}

		protected void setLoadResult(boolean refresh, boolean success, PageInfo pageInfo, Object[] ndata) {
			if (success) {
				if (refresh)
					datas.clear();
				if (ndata != null) {
					for (Object o : ndata) {
						datas.add(o);
					}
					adapter.notifyDataSetChanged();
				}
				//判断是否还有数据未加载入listView
				if (pageInfo != null && datas != null && (ndata.length == 0 || (pageInfo.currentPage >= pageInfo.totalPages) || (pageInfo.totalPages * pageInfo.pageSize <= datas.size())))
					listViewWarp.setNoMore(true);
				else if (ndata != null)
					listViewWarp.setNoMore(false);
			}
			if (refresh)
				listViewWarp.setRefreshing(false);
			else
				listViewWarp.setLoadingMore(false);
			if (!success)
				listViewWarp.setStatus(ListStatus.Error);
		}
	}

	protected ViewPager vpPagers = null;
	protected RadioGroup rgTabs = null;

	protected int fragmentLayoutId = 0;
	protected int viewPagerId = 0;
	protected int radioGroupId = 0;

	public TabPagerListFragment(int fragmentLayoutId, int viewPagerId, int radioGroupId) {
		super();
		this.fragmentLayoutId = fragmentLayoutId;
		this.viewPagerId = viewPagerId;
		this.radioGroupId = radioGroupId;
	}

	protected void onViewCreated() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(fragmentLayoutId, container, false);
			vpPagers = (ViewPager) rootView.findViewById(viewPagerId);
			if (getPagerItems() != null)
				vpPagers.setAdapter(new SimplePagerAdapter(getPagerItems()));

			vpPagers.setOnPageChangeListener(this);

			rgTabs = (RadioGroup) rootView.findViewById(radioGroupId);
			rgTabs.setOnCheckedChangeListener(this);
			onViewCreated();
		}
		return rootView;
	}

	//当RadioButton改变时，设置ViewPager的当前页面
	@Override
	public void onCheckedChanged(RadioGroup rg, int rid) {
		int rids[] = getTabIds();
		for (int i = 0; i < rids.length; ++i) {
			if (rids[i] == rid) {
				if (vpPagers.getCurrentItem() != i)
					vpPagers.setCurrentItem(i);
				break;
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	//当ViewPager的页面滑动时 去改变所选中的RadioButton
	@Override
	public void onPageSelected(int ix) {
		int rids[] = getTabIds();
		if (rgTabs.getCheckedRadioButtonId() != rids[ix]) {
			rgTabs.check(rids[ix]);
		}
	}

	abstract protected int[] getTabIds();

	abstract protected List<PagerItem> getPagerItems();
}
