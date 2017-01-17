package com.zju.hzsz.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.clz.RootViewWarp;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.Ranking;
import com.zju.hzsz.utils.StrUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class RankingFragment extends BaseFragment implements OnClickListener {
	enum OrderBy {
		Complaint, Satisfaction, Unhandle
	}

	class RankingComp implements Comparator<Ranking> {
		public OrderBy orderBy = OrderBy.Complaint;//默认以投诉量排序
		public boolean desc = true; //默认降序

		@Override
		public int compare(Ranking r1, Ranking r2) {
			int v = 0;
			switch (orderBy) {
			case Complaint:
				v = r1.complaintsSum - r2.complaintsSum;
				break;
			case Satisfaction:
				// v = r1.satisfactionRatio - r2.satisfactionRatio;
				break;
			case Unhandle:
				v = r1.comUnDeal - r2.comUnDeal;
				break;
			default:
				break;
			}
			return desc ? -v : v;
		}
	}

	private static String[] RIVERS = new String[] { "丁杭港", "七号港", "三号港", "东风港", "九号港", "九沙河", "二号港", "五会港", "五号港", "麦庙港", };
	private static String[] AREAS = new String[] { "西湖区", "拱野区", "萧山区", "江干区", "滨江区", "余杭区", "上城区", "临安区" };

	private List<Ranking> rankings = new ArrayList<Ranking>();
	private SimpleListAdapter adapter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_ranking, container, false);
			RootViewWarp warp = getRootViewWarp();
			warp.setHeadImage(0, R.drawable.ic_head_order);
			warp.setHeadTitle(R.string.ranking);

			warp.getViewById(R.id.iv_head_right).setOnClickListener(this);
			warp.getViewById(R.id.tv_complaint_count).setOnClickListener(this);
			warp.getViewById(R.id.tv_satisfaction_count).setOnClickListener(this);
			warp.getViewById(R.id.tv_unhandle).setOnClickListener(this);

			ListView lv_ranking = (ListView) rootView.findViewById(R.id.lv_ranking);
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
					warp.setText(R.id.tv_satisfaction_count, "" + (r.satisfactionRatio * 100.0) + "%");
					warp.setText(R.id.tv_unhandle, "" + r.comUnDeal);
					return convertView;
				}
			});
			lv_ranking.setAdapter(adapter);

			refreshRanking();
		}
		return rootView;
	}

	private void refreshRanking() {
		rankings.clear();
		Random rd = new Random(System.currentTimeMillis());
		for (String s : RIVERS) {
			Ranking r = new Ranking();
			r.riverName = s;
			r.complaintsSum = rd.nextInt(100);
			r.satisfactionRatio = rd.nextInt(100);
			r.comUnDeal = rd.nextInt(50);
			rankings.add(r);
		}
		Collections.sort(rankings, comp);
		adapter.notifyDataSetChanged();
		getRootViewWarp().setText(R.id.tv_rankingtitle, StrUtils.renderText(getBaseActivity(), R.string.fmt_arearanking, curAreaName));
	}

	private PopupWindow areaPop = null;
	private View areaView = null;

	private void showArea() {
		areaPop.showAsDropDown(getBaseActivity().findViewById(R.id.iv_head_right));
		rootView.findViewById(R.id.v_mask).setVisibility(View.VISIBLE);
	}

	private void dismissArea() {
		areaPop.dismiss();
		rootView.findViewById(R.id.v_mask).setVisibility(View.GONE);
	}

	private String curAreaName = "西湖区";
	private RankingComp comp = new RankingComp();

	private void showAreaPop() {
		if (areaPop == null) {
			LinearLayout ll_areas = new LinearLayout(getBaseActivity());
			ll_areas.setOrientation(LinearLayout.VERTICAL);
			areaView = ll_areas;

			View.OnClickListener clk = new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					dismissArea();
					curAreaName = arg0.getTag().toString();
					refreshRanking();
				}
			};

			Random rd = new Random(System.currentTimeMillis());
			for (String s : AREAS) {
				View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_ranking_area, null);
				((TextView) view.findViewById(R.id.tv_name)).setText(s);
				((TextView) view.findViewById(R.id.tv_count)).setText("" + (rd.nextInt(20) + 5));
				view.setOnClickListener(clk);
				view.setTag(s);
				ll_areas.addView(view);
			}

			ColorDrawable cd = new ColorDrawable(getBaseActivity().getResources().getColor(R.color.gray));
			areaPop = new PopupWindow(areaView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			areaPop.setOutsideTouchable(true);
			areaPop.setFocusable(false);
			areaPop.update();
			areaPop.setBackgroundDrawable(cd);
			areaPop.setTouchInterceptor(new View.OnTouchListener() {

				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					if (arg1.getAction() == MotionEvent.ACTION_OUTSIDE) {
						dismissArea();
						return true;
					} else
						return false;
				}
			});
		}
		if (areaPop.isShowing())
			dismissArea();
		else
			showArea();
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.iv_head_right:
			showAreaPop();
			break;
		case R.id.tv_complaint_count:
		case R.id.tv_satisfaction_count:
		case R.id.tv_unhandle:
			int id = arg0.getId();
			OrderBy by = id == R.id.tv_complaint_count ? OrderBy.Complaint : (id == R.id.tv_satisfaction_count ? OrderBy.Satisfaction : OrderBy.Unhandle);
			if (comp.orderBy == by) {
				comp.desc = !comp.desc;
			} else {
				comp.orderBy = by;
				comp.desc = false;
			}
			Collections.sort(rankings, comp);
			adapter.notifyDataSetChanged();
			break;
		}
	}
}
