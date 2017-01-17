package com.zju.hzsz.view;


import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zju.hzsz.R;

public class ListViewWarp {
	public interface WarpHandler {
		public boolean onRefresh();

		public boolean onLoadMore();
	}

	public enum ListStatus {
		None, Refreshing, LoadingMore, Error
	}

	protected static final String TAG = "ListViewWarp";

	private Context context;
	private WarpHandler handler;
	private View rootView;
	private ListView listView;
	private ListAdapter adapter;
	private SwipeRefreshLayout swipeRefreshLayout;
	private View footerView;
	int lastItem;
	private boolean noMore = false;
	private ListStatus status = ListStatus.None;

	public ListViewWarp(Context context, ListAdapter adapter, WarpHandler handler) {
		this.context = context;
		this.handler = handler;
		this.adapter = adapter;
	}

	@SuppressWarnings("deprecation")
	private void readyView() {
		if (rootView == null) {
			rootView = LinearLayout.inflate(context, R.layout.comp_listview, null);
			swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_listview);
			swipeRefreshLayout.setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_red_light);
			swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					if (status != ListStatus.LoadingMore && status != ListStatus.Refreshing) {
						Log.i(TAG, "onRefresh");
						startRefresh();//将listStatus状态更新为ListStatus.Refreshing，并展示进度条
					} else {
						setRefreshing(false);//隐藏刷新进度条
					}
				}
			});

			listView = (ListView) rootView.findViewById(R.id.lv_listview);

			footerView = LinearLayout.inflate(context, R.layout.comp_listview_footer, null);
			setFooterView(true);
			listView.addFooterView(footerView);

			listView.setAdapter(adapter);
			//监听是否滑动尽头，是的话就加载新的内容
			listView.setOnScrollListener(new AbsListView.OnScrollListener() {
				/*
				 * scrollState值：
				 * 当屏幕停止滚动时为SCROLL_STATE_IDLE = 0；
				 * 当屏幕滚动且用户使用的触碰或手指还在屏幕上时为SCROLL_STATE_TOUCH_SCROLL = 1；
				 * 由于用户的操作，屏幕产生惯性滑动时为SCROLL_STATE_FLING = 2
				 */
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						if (view.getLastVisiblePosition() == view.getCount() - 1) {
							if (status != ListStatus.LoadingMore && status != ListStatus.Refreshing) {
								Log.i(TAG, "onLoadMore");
								startLoadMore();
							}
						}
					}
				}

				/*
				 * firstVisibleItem:表示在现时屏幕第一个ListItem(部分显示的ListItem也算)在整个ListView的位置(下标从0开始)
				 * visibleItemCount:表示在现时屏幕可以见到的ListItem(部分显示的ListItem也算)总数
				 * totalItemCount:表示ListView的ListItem总数
				 * listView.getFirstVisiblePosition()表示在现时屏幕第一个ListItem(第一个ListItem部分显示也算)在整个ListView的位置(下标从0开始)
				 * listView.getLastVisiblePosition()表示在现时屏幕最后一个ListItem(最后ListItem要完全显示出来才算)在整个ListView的位置(下标从0开始)
				 */
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					lastItem = firstVisibleItem + visibleItemCount - 1;
				}
			});
		}
	}

	public View getRootView() {
		readyView();
		return rootView;
	}

	public ListView getListView() {
		readyView();
		return listView;
	}

	public void setLoadingMore(boolean b) {
		if (swipeRefreshLayout != null && !noMore) {
			if (b) {
				((TextView) footerView.findViewById(R.id.tv_status)).setText("正在加载更多..."); //文字
				footerView.findViewById(R.id.pb_loading).setVisibility(View.VISIBLE); //进度条
				setFooterView(true);
			} else {
				setFooterView(false);
			}
		}
		status = b ? ListStatus.LoadingMore : ListStatus.None;
	}

	//显示或隐藏进度条并设置ListStatus状态
	public void setRefreshing(boolean b) {
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setRefreshing(b);
		}
		status = b ? ListStatus.Refreshing : ListStatus.None;
	}

	//开始Refresh
	public void startRefresh() {
		noMore = false;
		status = ListStatus.Refreshing;
		setRefreshing(handler != null && handler.onRefresh());
	}

	//如果还有数据则LodingMore
	public void startLoadMore() {
		if (!noMore) {
			status = ListStatus.LoadingMore;
			setLoadingMore(handler != null && handler.onLoadMore());
		}
	}

	public boolean isNoMore() {
		return noMore;
	}

	private String noMoreText = "暂时没有数据!";

	public void setNoMore(boolean noMore) {
		if (noMore) {
			setLoadingMore(false);
		} else {
			setFooterView(true);
		}
		this.noMore = noMore;

		//如果没有数据时不显示进度条显示“暂时没有数据”
		if (footerView != null) {
			if (adapter.getCount() == 0) {
				((TextView) footerView.findViewById(R.id.tv_status)).setText(noMoreText);
				footerView.findViewById(R.id.pb_loading).setVisibility(View.GONE);
				setFooterView(true);
			} else {
				setFooterView(false);
			}
		}
	}

	public ListStatus getStatus() {
		return status;
	}

	public void setStatus(ListStatus status) {
		this.status = status;
		if (this.status == ListStatus.Error && footerView != null) {
			((TextView) footerView.findViewById(R.id.tv_status)).setText("加载失败!");
			footerView.findViewById(R.id.pb_loading).setVisibility(View.GONE);
			setFooterView(true);
		}
	}

	public LinearLayout getRootLinearLayout() {
		return (LinearLayout) getRootView().findViewById(R.id.ll_root);
	}

	private void setFooterView(boolean isshow) {
		if (isshow) {
			footerView.setVisibility(View.VISIBLE);
			// listView.addFooterView(footerView);
			// getRootLinearLayout().addView(footerView);
		} else {
			footerView.setVisibility(View.INVISIBLE);
			// listView.removeFooterView(footerView);
			// getRootLinearLayout().removeView(footerView);
		}
	}

	public void setNoMoreText(String text) {
		this.noMoreText = text;
	}
}
