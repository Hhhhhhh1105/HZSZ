package com.zju.hzsz.utils;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.zju.hzsz.R;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.IndexValue;
import com.zju.hzsz.model.SectionIndex;

public class ViewUtils {
	public static void setSwipeRefreshLayoutColorScheme(SwipeRefreshLayout swipeRefreshLayout) {
		swipeRefreshLayout.setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_red_light);
	}

	public interface NameGetter {
		public CharSequence getName(Object o);
	}

	public static <T> void initTabLine(Context context, RadioGroup rg_tabs, T[] items, NameGetter getter, android.widget.CompoundButton.OnCheckedChangeListener cklsn) {
		// int padding = DipPxUtils.dip2px(context,
		// context.getResources().getDimension(R.dimen.padding_small));
		int dp1px = DipPxUtils.dip2px(context, -context.getResources().getDimension(R.dimen.linew));
		// int tab_min_w = DipPxUtils.dip2px(context,
		// context.getResources().getDimension(R.dimen.tab_min_w));

		rg_tabs.removeAllViews();
		for (int i = 0; i < items.length; ++i) {
			T item = items[i];
			boolean isfirst = i == 0;
			boolean islast = i == (items.length - 1);

			RadioButton rb = (RadioButton) LinearLayout.inflate(context, R.layout.rb_smalltab, null);// new
																										// RadioButton(context);
			rb.setText(getter != null ? getter.getName(item) : item.toString());
			rb.setTag(item);
			// rb.setPadding(padding, padding, padding, padding);
			rb.setButtonDrawable(android.R.color.transparent);
			RadioGroup.LayoutParams lp = new LayoutParams(context, null);
			if (isfirst && islast) {
				rb.setBackgroundResource(R.drawable.shape_tabbtn_o_bg);
			} else if (isfirst) {
				rb.setBackgroundResource(R.drawable.shape_tabbtn_l_bg);
			} else if (islast) {
				rb.setBackgroundResource(R.drawable.shape_tabbtn_r_bg);
				lp.setMargins(dp1px, 0, 0, 0);
			} else {
				rb.setBackgroundResource(R.drawable.shape_tabbtn_m_bg);
				lp.setMargins(dp1px, 0, 0, 0);
			}
			rb.setLayoutParams(lp);
			// rb.setTextColor(context.getResources().getColorStateList(R.color.btn_black_white));
			// rb.setTextAppearance(context,
			// android.R.attr.textAppearanceSmall);
			// rb.setMinimumWidth(tab_min_w);
			// rb.setGravity(Gravity.CENTER);
			if (cklsn != null)
				rb.setOnCheckedChangeListener(cklsn);
			rg_tabs.addView(rb);

			if (isfirst) {
				rb.setChecked(true);
				if (cklsn != null) {
					cklsn.onCheckedChanged(rb, true);
				}
			}
		}
	}

	public static void initIndexTable(Context context, LinearLayout ll_indexs, SectionIndex[] items) {
		ll_indexs.removeAllViews();
		for (int i = 0; i < items.length; i += 2) {
			LinearLayout line = new LinearLayout(context);
			line.setOrientation(LinearLayout.HORIZONTAL);

			line.addView(genIndexLine(context, items[i], true));
			line.addView((i + 1) < items.length ? genIndexLine(context, items[i + 1], false) : null);

			if ((i + 2) <= items.length) {
				// not last
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
				lp.setMargins(0, 0, 0, DipPxUtils.dip2px(context, context.getResources().getDimension(R.dimen.linew)));
				line.setLayoutParams(lp);
			}

			ll_indexs.addView(line);

		}
	}

	private static View genIndexLine(Context context, SectionIndex data, boolean isLeft) {
		View view = LinearLayout.inflate(context, R.layout.item_section_indexone, null);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
		if (isLeft) {
			lp.setMargins(0, 0, DipPxUtils.dip2px(context, context.getResources().getDimension(R.dimen.linew)), 0);
		}
		view.setLayoutParams(lp);
		if (data != null) {
			ViewWarp warp = new ViewWarp(view, context);
			warp.setText(R.id.tv_index_value, StrUtils.floatS2Str(data.indexValue));

			warp.setText(R.id.tv_index_en, IndexENUtils.getString(data.indexNameEN));

			warp.setText(R.id.tv_index_ch, "" + data.indexNameCH);
			((TextView) warp.getViewById(R.id.tv_index_value)).setTextColor(context.getResources().getColor(ResUtils.getQuiltyColor(data.indexLevel)));
		} else {
			view.setVisibility(View.INVISIBLE);
		}
		return view;
	}

	public static void loadIndexChart(Context context, LineChart lc_chart, IndexValue[] indexs) {
		loadIndexChart(context, lc_chart, indexs, false, null);
	}

	public static void loadIndexChart(Context context, LineChart lc_chart, IndexValue[] indexs, NameGetter getter) {
		loadIndexChart(context, lc_chart, indexs, false, getter);
	}

	public static void loadIndexChart(Context context, LineChart lc_chart, IndexValue[] indexs, boolean showDate, NameGetter getter) {
		ArrayList<String> xValues = new ArrayList<String>();
		ArrayList<Entry> yValues = new ArrayList<Entry>();
		for (int i = 0; i < indexs.length; ++i) {
			IndexValue index = indexs[i];
			if (getter != null) {
				xValues.add(getter.getName(index).toString());
			} else {
				if (showDate) {
					xValues.add(index.getTime.getLittleDate(context));
				} else {
					xValues.add(index.getTime.getHoursMinutes(context));
				}
			}
			yValues.add(new Entry(Float.parseFloat(index.indexMapValue), i));
		}

		LineDataSet lineDataSet = new LineDataSet(yValues, "测试折线图" /* 显示在比例图上 */);
		lineDataSet.setLineWidth(1.75f); // 线宽
		lineDataSet.setCircleSize(3f);// 显示的圆形大小

		lineDataSet.setColor(context.getResources().getColor(R.color.quality_1));// 显示颜色
		lineDataSet.setCircleColor(Color.BLUE);// 圆形的颜色
		lineDataSet.setHighLightColor(Color.WHITE); // 高亮的线的颜色
		ArrayList<LineDataSet> lineDataSets = new ArrayList<LineDataSet>();
		lineDataSets.add(lineDataSet); // add the datasets

		lineDataSet.setFillColor(context.getResources().getColor(R.color.quality_1));
		lineDataSet.setFillAlpha(30);
		lineDataSet.setDrawFilled(true);
		lineDataSet.setDrawValues(false);

		LineData lineData = new LineData(xValues, lineDataSets);

		lc_chart.setData(lineData);
		lc_chart.setTouchEnabled(false);
		lc_chart.getXAxis().setPosition(XAxisPosition.BOTTOM);
		lc_chart.getAxisRight().setEnabled(false);
		lc_chart.getXAxis().setDrawGridLines(false);
		lc_chart.setDrawGridBackground(true);

		lc_chart.getAxisLeft().enableGridDashedLine(100, 1, 1);
		lc_chart.getAxisLeft().setAxisMinValue(0);
		lc_chart.getAxisLeft().setAxisMaxValue(6);
		lc_chart.getAxisLeft().setDrawLabels(false);
		lc_chart.getAxisLeft().setPosition(YAxisLabelPosition.INSIDE_CHART);

		lc_chart.getLegend().setEnabled(false);
		lc_chart.setDescription("");
		lc_chart.animateX(500);
		lc_chart.getXAxis().setPosition(XAxisPosition.BOTTOM);
		lc_chart.getXAxis().setAvoidFirstLastClipping(true);
	}

	public interface CellInitor<T> {
		public View initView(Context context, View view, T data);
	}

	public static <T> void addTwoColToLinearLayout(Context context, LinearLayout ll, T[] datas, CellInitor<T> initor) {
		int dp1px = DipPxUtils.dip2px(context, -context.getResources().getDimension(R.dimen.linew));
		for (int i = 0; i < datas.length; i += 2) {
			View vl = initor.initView(context, null, datas[i]);
			View vr = initor.initView(context, null, (i + 1) < datas.length ? datas[i] : null);
			LinearLayout row = new LinearLayout(context);

			LinearLayout.LayoutParams lpl = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			vl.setLayoutParams(lpl);
			LinearLayout.LayoutParams lpr = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			lpr.setMargins(dp1px, 0, 0, 0);
			vr.setLayoutParams(lpr);
			row.setOrientation(LinearLayout.HORIZONTAL);

			if (!((i + 1) < datas.length)) {
				vr.setVisibility(View.INVISIBLE);
			}

			row.addView(vl);
			row.addView(vr);
			ll.addView(row);
		}
	}

	public static void setQuilityLineV(Context context, LinearLayout ll_quality_contain, boolean desc, String yvals[]) {
		LinearLayout ll_quality_line_ycolors = (LinearLayout) ll_quality_contain.findViewById(R.id.ll_quality_line_ycolors);

		for (int i = 0; i < 6; ++i) {
			View v = ll_quality_line_ycolors.getChildAt(i + 1);
			int color = context.getResources().getColor(desc ? ResUtils.getQuiltyColor(i + 1) : ResUtils.getQuiltyColor(6 - i));
			v.setBackgroundColor(color);
		}

		LinearLayout ll_quality_line_yvalues = (LinearLayout) ll_quality_contain.findViewById(R.id.ll_quality_line_yvalues);
		for (int i = 0; i < 7; ++i) {
			TextView tv = (TextView) ll_quality_line_yvalues.getChildAt(i);
			tv.setText(yvals[i]);
		}

		// DisplayMetrics dm = new DisplayMetrics();
		//
		// ((Activity)
		// context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		// ll_quality_contain.setLayoutParams(new
		// LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)
		// (dm.widthPixels * 0.5)));
	}

	public static void replaceInView(ViewGroup vg, String t, String r) {
		int count = vg.getChildCount();
		for (int i = 0; i < count; ++i) {
			View v = vg.getChildAt(i);
			if (v instanceof ViewGroup) {
				replaceInView((ViewGroup) v, t, r);
			} else if (v instanceof TextView) {
				TextView tv = (TextView) v;
				String s = tv.getText().toString();
				tv.setText(s.replace(t, r));
			}
		}
	}
}
