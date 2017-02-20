package com.zju.hzsz.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.activity.RiverActivity;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverSearchDataRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RiverListFragment extends BaseFragment {
	SimpleListAdapter adapter = null;
	List<River> rivers = new ArrayList<River>();
	ListViewWarp listViewWarp = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (this.rootView == null) {
			final View.OnClickListener togFollow = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (v.getTag() instanceof River) {
						final River river = (River) v.getTag();

						river.toggleCare(getBaseActivity(), new BaseActivity.BooleanCallback() {
							@Override
							public void callback(boolean b) {
								adapter.notifyDataSetChanged();
							}
						});
					}
				}
			};

			final View.OnClickListener goRiver = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getBaseActivity(), RiverActivity.class);
					intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(v.getTag()));
					startActivity(intent);
				}
			};

			adapter = new SimpleListAdapter(getBaseActivity(), rivers, new SimpleViewInitor() {

				@Override
				public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
					if (convertView == null) {
						convertView = LinearLayout.inflate(context, R.layout.item_river_detail, null);
					}
					River river = (River) data;
					((TextView) convertView.findViewById(R.id.tv_name)).setText(river.riverName);
					((TextView) convertView.findViewById(R.id.tv_level)).setText(ResUtils.getRiverSLittleLevel(river.riverLevel));
					/*String img = StrUtils.getImgUrl(river.getImgUrl());
					ImgUtils.loadImage(getBaseActivity(), ((ImageView) convertView.findViewById(R.id.iv_picture)), img);*/

//					convertView.findViewById(R.id.btn_follow).setVisibility(View.VISIBLE);
					convertView.findViewById(R.id.iv_quality).setVisibility(View.VISIBLE);
					((ImageView) convertView.findViewById(R.id.iv_quality)).setImageResource(ResUtils.getQuiltyImg(river.waterType));

					/*Button btn = (Button) convertView.findViewById(R.id.btn_follow);
					btn.setText(river.isCared(getBaseActivity().getUser()) ? R.string.unfollow : R.string.follow);
					btn.setBackgroundResource(river.isCared(getBaseActivity().getUser()) ? R.drawable.btn_gray_white : R.drawable.btn_green_white);
					btn.setTag(river);
					btn.setOnClickListener(togFollow);*/

					((TextView) (convertView.findViewById(R.id.tv_distname))).setText(river.districtName);
					(convertView.findViewById(R.id.tv_distname)).setVisibility(View.VISIBLE);

					convertView.setOnClickListener(goRiver);
					convertView.setTag(river);
					return convertView;
				}
			});
			listViewWarp = new ListViewWarp(getBaseActivity(), adapter, new ListViewWarp.WarpHandler() {

				@Override
				public boolean onRefresh() {
					return startLoad(true);
				}

				@Override
				public boolean onLoadMore() {
					return startLoad(false);
				}
			});

			rootView = listViewWarp.getRootView();

			onHeadRefresh();

		}

		return rootView;
	}

	public void onHeadRefresh() {
		startLoad(true);
	}

//	private final int DefaultPageSize = 7;
	private int size = 9;
	private int DefaultPageSize = 12;

	protected JSONObject getPageParam(boolean refresh) {
		if(refresh){
			size ++;
			if (size >= 10){
				size = 6;
			}
		}
		else {
			size --;
			if(size <= 6){
				size = 10;
			}
		}
//		DefaultPageSize = size;
		return refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (rivers.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
	}

	private boolean startLoad(final boolean refresh) {
		showOperating();
		if (refresh)
			listViewWarp.setRefreshing(true);
		else
			listViewWarp.setLoadingMore(true);
		getRequestContext().add("Get_NewRiverSearch_Data", new Callback<RiverSearchDataRes>() {
			@Override
			public void callback(RiverSearchDataRes o) {
				listViewWarp.setRefreshing(false);
				listViewWarp.setLoadingMore(false);

				if (o != null && o.isSuccess() && o.data != null && o.data.riverSums != null) {
					System.out.println("NET: o的值" + o);
					if (refresh)
						rivers.clear();
					for (River r : o.data.riverSums) {
						rivers.add(r);
					}
					adapter.notifyDataSetChanged();
				}
				hideOperating();
				if ((o != null && o.data != null && o.data.riverSums != null) && (o.data.pageInfo != null && rivers.size() >= o.data.pageInfo.totalCounts || o.data.riverSums.length == 0)) {
					listViewWarp.setNoMore(true);
				} else {
					listViewWarp.setNoMore(false);
				}
			}
		}, RiverSearchDataRes.class, getPageParam(refresh));
		return true;
	}
}
