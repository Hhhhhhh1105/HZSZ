package com.zju.hzsz.fragment.river;

import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.zju.hzsz.R;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.model.IndexValue;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverDataRes;
import com.zju.hzsz.model.RiverQualityDataRes;
import com.zju.hzsz.model.SectionIndex;
import com.zju.hzsz.model.Station;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.IndexENUtils;
import com.zju.hzsz.utils.ObjUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.ValUtils;
import com.zju.hzsz.utils.ViewUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class RiverQualityItem extends BaseRiverPagerItem implements OnCheckedChangeListener {
	public RiverQualityItem(River river, BaseActivity context) {
		super(river, context);
	}

	@Override
	public View getView() {
		if (view == null) {
			view = LinearLayout.inflate(context, R.layout.confs_river_quality, null);
			((TextView) view.findViewById(R.id.tv_name)).setText(river.riverName);
			((TextView) view.findViewById(R.id.tv_level)).setText(ResUtils.getRiverSLevel(river.riverLevel));
			((ImageView) view.findViewById(R.id.iv_quality)).setImageResource(ResUtils.getQuiltySmallImg(river.waterType));

			((RadioGroup) view.findViewById(R.id.rg_indexs)).removeAllViews();
			((RadioGroup) view.findViewById(R.id.rg_segments)).removeAllViews();
			((LinearLayout) view.findViewById(R.id.ll_indexs)).removeAllViews();
			refrehDeepView();
			initedView();
			loadData();
		}
		return view;
	}

	private void refrehDeepView() {
		((TextView) view.findViewById(R.id.tv_level)).setText(ResUtils.getRiverSLevel(river.riverLevel));

		RadioGroup rg_segments = (RadioGroup) view.findViewById(R.id.rg_segments);
		rg_segments.removeAllViews();
		if (river.stations != null && river.stations.length > 0) {
			ViewUtils.initTabLine(context, rg_segments, river.stations, new ViewUtils.NameGetter() {
				@Override
				public String getName(Object o) {
					return ((Station) o).stationName;
				}
			}, this);
		}
		if (river.indexs != null && river.indexs.length > 0) {
			ViewUtils.initIndexTable(context, (LinearLayout) view.findViewById(R.id.ll_indexs), river.indexs);
			ViewUtils.initTabLine(context, (RadioGroup) view.findViewById(R.id.rg_indexs), river.indexs, new ViewUtils.NameGetter() {
				@Override
				public CharSequence getName(Object o) {
					return IndexENUtils.getString(((SectionIndex) o).indexNameEN);
				}
			}, this);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton rb, boolean sel) {
		if (sel) {
			if (rb.getTag() instanceof Station) {
				curStation = (Station) rb.getTag();
				((TextView) view.findViewById(R.id.tv_segment_name)).setText(curStation.stationName + "监测点");
			}
			if (rb.getTag() instanceof SectionIndex) {
				curIndex = (SectionIndex) rb.getTag();
			}
			Log.i("selected", "S:" + (curStation != null ? curStation.stationName : "null") + " I:" + (curIndex != null ? curIndex.indexNameEN : "null"));
			if (curStation != null && curIndex != null) {
				loadStationIndex();
			}
		}
	}

	private Station curStation = null;
	private SectionIndex curIndex = null;

	private void loadStationIndex() {
		loadData();
	}

	private boolean isRiverInfoed() {
		return !(river.stations == null || river.stations.length == 0 || river.indexs == null || river.indexs.length == 0);
	}

	@Override
	public void loadData() {
		if (!isRiverInfoed()) {
			loadRiverInfo();
		} else {
			loadIndexData();
		}
	}

	private void loadRiverInfo() {
		JSONObject p = new JSONObject();
		try {
			p.put("riverId", river.riverId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		setRefreshing(true);
		context.getRequestContext().add("oneriver_data_get", new Callback<RiverDataRes>() {

			@Override
			public void callback(RiverDataRes o) {
				if (o != null && o.isSuccess()) {
					ObjUtils.mergeObj(river, o.data);
					refrehDeepView();
				}
				setRefreshing(false);
			}
		}, RiverDataRes.class, p);
	}

	private void loadIndexData() {
		if (curIndex == null || curStation == null) {
			setRefreshing(false);
			return;
		}

		setRefreshing(true);
		context.getRequestContext().add("riverwaterquality_data_get", new Callback<RiverQualityDataRes>() {

			@Override
			public void callback(RiverQualityDataRes o) {
				if (o != null && o.isSuccess()) {
					ViewUtils.loadIndexChart(context, (LineChart) view.findViewById(R.id.lc_chart), o.data.indexValues, new ViewUtils.NameGetter() {

						@Override
						public CharSequence getName(Object o) {
							IndexValue index = (IndexValue) o;
							return index.getTime.getYM(context);
						}
					});
					ViewUtils.setQuilityLineV(context, (LinearLayout) view.findViewById(R.id.inc_quality_line_v),
							"DO".equals(curIndex.indexNameEN) || "Transp".equals(curIndex.indexNameEN), ValUtils.getYVals(curIndex.indexNameEN));
					if (o.data.indexDatas != null) {
						ViewUtils.initIndexTable(context, (LinearLayout) view.findViewById(R.id.ll_indexs), o.data.indexDatas);
					}
					((ImageView) view.findViewById(R.id.iv_quality)).setImageResource(ResUtils.getQuiltySmallImg(o.data.waterLevel));
				}
				setRefreshing(false);
			}
		}, RiverQualityDataRes.class, ParamUtils.freeParam(null, "stationId", curStation.stationId, "indexId", curIndex.indexId));
	}
}
