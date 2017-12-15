package com.zju.hzsz.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.activity.LakeListAcitivity;
import com.zju.hzsz.activity.SearchRiverActivity;
import com.zju.hzsz.adapter.PagerItem;
import com.zju.hzsz.adapter.SimplePagerAdapter;
import com.zju.hzsz.fragment.river.RiverInfoItem;
import com.zju.hzsz.fragment.river.RiverInfoPubItem;
import com.zju.hzsz.fragment.river.RiverPolicyItem;
import com.zju.hzsz.fragment.river.RiverPositionItem;
import com.zju.hzsz.fragment.river.RiverQualityItem;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 断面水质
 * 
 * @author Robin
 * 
 */
public class RiverFragment extends BaseFragment implements OnCheckedChangeListener, OnPageChangeListener {

	enum ShowRiverType {
		DZGSP, YHYC, HDSZ, HDFW, TSXX
	}

	class RiverPagerItem extends PagerItem {
		private River river;
		private RiverInfoItem infoItem = null;
		private RiverQualityItem qualityItem = null;
		private RiverPolicyItem policyItem = null;
		private RiverPositionItem positionItem = null;
		// private RiverCompItem compItem = null;
		private RiverInfoPubItem infoPubItem = null;

		private ShowRiverType showRiverType = ShowRiverType.DZGSP;
		private LinearLayout layout = null;

		public RiverPagerItem(River river) {
			super();
			this.river = river;
		}

		/*
		* 更新河道信息下的各个子页面
		* */
		public void refreshChildView() {
			layout.removeAllViews();
			switch (showRiverType) {
			case DZGSP:
				if (infoItem == null)
					infoItem = new RiverInfoItem(river, getBaseActivity(), new BaseActivity.BooleanCallback() {
						@Override
						public void callback(boolean b) {
							pagerItems.clear();
							for (River r : rivers) {
								pagerItems.add(new RiverPagerItem(r));
							}
							adapter.notifyDataSetChanged();
							refreshTitle();
						}
					});
				layout.addView(infoItem.getView());
				break;
			case YHYC:
				if (policyItem == null)
					policyItem = new RiverPolicyItem(river, getBaseActivity());
				layout.addView(policyItem.getView());
				break;
			case HDSZ:
				if (qualityItem == null)
					qualityItem = new RiverQualityItem(river, getBaseActivity());
				layout.addView(qualityItem.getView());
				break;
			case HDFW:
				if (positionItem == null)
					positionItem = new RiverPositionItem(river, getBaseActivity());
				layout.addView(positionItem.getView());
				break;
			case TSXX:
				// if (compItem == null)
				// compItem = new RiverCompItem(river, getBaseActivity());
				// layout.addView(compItem.getView());

				if (infoPubItem == null)
					infoPubItem = new RiverInfoPubItem(river, getBaseActivity());
				layout.addView(infoPubItem.getView());
				break;
			}
		}

		/*
		* 这个函数其实是多余的 并无实际 作用
		* */

		/*
		* 这个函数并不是多余的 items.get(Position)得到的是RiverPagerItem对象
		* */
		@Override
		public View getView() {
			if (layout == null) {
				layout = new LinearLayout(getBaseActivity());
				refreshChildView();
			}
			return layout;
		}

		public void switchShow(ShowRiverType show) {
			if (this.showRiverType != show) {
				this.showRiverType = show;
				refreshChildView();
			}
		}

		public ShowRiverType getShowRiverType() {
			return showRiverType;
		}
	}

	private List<River> rivers = null;

	private List<PagerItem> pagerItems = new ArrayList<PagerItem>();
	private SimplePagerAdapter adapter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			//当rootView为空时创建View
			rootView = inflater.inflate(R.layout.fragment_river, container, false);
			// rivers.addAll(getBaseActivity().getUser().getCollections());
			//rivers = new List<River>();
			rivers = getBaseActivity().getUser().getCollections();

			//顶栏左边无图片，右边为search图片
			getRootViewWarp().setHeadImage(0, R.drawable.ic_head_search);
			//为RadioGroup绑定监听器
			((RadioGroup) rootView.findViewById(R.id.rg_river_showwith)).setOnCheckedChangeListener(this);
			//为ViewPager绑定监听器
			((ViewPager) rootView.findViewById(R.id.vp_rivers)).setOnPageChangeListener(this);
			//ViewPager的适配器
			adapter = new SimplePagerAdapter(pagerItems) {
				@Override
				public int getItemPosition(Object object) {
					return POSITION_NONE;
				}

				@Override
				public void destroyItem(ViewGroup container, int position, Object object) {
					try {
						//需将object对象强转成View
						container.removeView((View) object);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			((ViewPager) rootView.findViewById(R.id.vp_rivers)).setAdapter(adapter);

			rootView.findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
//					startActivity(new Intent(getBaseActivity(), SearchRiverActivity.class));
					showAreaPop();
				}
			});

			if (rivers.size() == 0) {
				getRivers();
			} else {
				pagerItems.clear();
				for (River r : rivers) {
					pagerItems.add(new RiverPagerItem(r));
				}

				adapter.notifyDataSetChanged();
				refreshTitle();
			}
		}
		return rootView;
	}

	private void getRivers() {
		showOperating();
		getRequestContext().add("river_list_get", new Callback<RiverListRes>() {

			@Override
			public void callback(RiverListRes o) {
				if (o != null && o.isSuccess()) {
					// 更新
					for (River r : o.data) {
						if (!rivers.contains(r))
							rivers.add(r);
					}

					pagerItems.clear();
					for (River r : rivers) {
						pagerItems.add(new RiverPagerItem(r));
					}
					adapter.notifyDataSetChanged();

					((ViewPager) rootView.findViewById(R.id.vp_rivers)).setCurrentItem(0);
					if (rivers.size() > 0)
						onPageSelected(0);
					refreshTitle();
				}
				hideOperating();
			}
		}, RiverListRes.class, null);
	}

	@Override
	public void onCheckedChanged(RadioGroup rg, int rdid) {
		if (pagerItems.size() == 0)
			return;

		RiverPagerItem pagerItem = (RiverPagerItem) pagerItems.get(((ViewPager) rootView.findViewById(R.id.vp_rivers)).getCurrentItem());

		switch (rdid) {
		case R.id.rb_river_dzgsp:
			pagerItem.switchShow(ShowRiverType.DZGSP);
			break;
		case R.id.rb_river_yhyc:
			pagerItem.switchShow(ShowRiverType.YHYC);
			break;
		case R.id.rb_river_hdsz:
			pagerItem.switchShow(ShowRiverType.HDSZ);
			break;
		case R.id.rb_river_hdfw:
			pagerItem.switchShow(ShowRiverType.HDFW);
			break;
		case R.id.rb_river_tsxx:
			pagerItem.switchShow(ShowRiverType.TSXX);
			break;
		default:
			break;
		}
	}

	/*
	* 若发生页面滑动时，直接使用河流的基本信息显示类型
	* */
	@Override
	public void onPageSelected(int ix) {
		getRootViewWarp().setHeadTitle(StrUtils.renderText(getBaseActivity(), R.string.fmt_riverinfo, rivers.get(ix).riverName));
		((RadioButton) rootView.findViewById(R.id.rb_river_dzgsp)).setChecked(true);

		if (ix < pagerItems.size()) {
			RiverPagerItem pagerItem = (RiverPagerItem) pagerItems.get(ix);
			if (pagerItem.getShowRiverType() != ShowRiverType.DZGSP) {
				pagerItem.switchShow(ShowRiverType.DZGSP);
				adapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	private void refreshTitle() {
		if (rootView.findViewById(R.id.vp_rivers) != null) {
			int ix = ((ViewPager) rootView.findViewById(R.id.vp_rivers)).getCurrentItem();
			if (rivers != null && ix >= 0 && ix < rivers.size()) {
				getRootViewWarp().setHeadTitle(StrUtils.renderText(getBaseActivity(), R.string.fmt_riverinfo, rivers.get(ix).riverName));
			}
		}
	}

	public void whenVisibilityChanged(boolean isVisibleToUser) {
		if (isVisibleToUser && adapter != null && rivers.size() != pagerItems.size()) {

			refreshTitle();

			if (rivers.size() == 0) {
				getRivers();
			} else {
				// 更新
				pagerItems.clear();
				for (River r : rivers) {
					pagerItems.add(new RiverPagerItem(r));
				}
				adapter.notifyDataSetChanged();
			}
		}
	}

	private PopupWindow areaPop = null;
	private View areaView = null;
	private String[] selectRiverOrLaker = {"搜河道", "搜湖泊"};

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

			//为popupwindow中的每个item绑定的监听器
			View.OnClickListener clk = new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dismissArea();
					switch (arg0.getTag().toString()){
						case "搜河道":
//							Toast.makeText(getActivity(),arg0.getTag().toString(),Toast.LENGTH_LONG).show();
							startActivity(new Intent(getBaseActivity(), SearchRiverActivity.class));
							break;
						case "搜湖泊":
//							Toast.makeText(getActivity(),arg0.getTag().toString(),Toast.LENGTH_LONG).show();
							startActivity(new Intent(getBaseActivity(), LakeListAcitivity.class));
							break;
						default: break;
					}
				}
			};

			//将各个区信息添加至线性布局中
			for (String slectString : selectRiverOrLaker) {
				View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_ranking_area, null);
				((TextView) view.findViewById(R.id.tv_name)).setText(slectString);
				view.setOnClickListener(clk);
				view.setTag(slectString);
				ll_areas.addView(view);
			}

			ColorDrawable cd = new ColorDrawable(getBaseActivity().getResources().getColor(R.color.gray));
			//areaView为要显示的View
			areaPop = new PopupWindow(areaView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
