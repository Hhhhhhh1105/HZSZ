package com.zju.hzsz.fragment.lake;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.zju.hzsz.R;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.model.IndexValue;
import com.zju.hzsz.model.Lake;
import com.zju.hzsz.model.LakeDataRes;
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

/**
 * Created by ZJTLM4600l on 2017/12/20.
 */

public class LakeQualityItem extends BaseLakePagerItem implements CompoundButton.OnCheckedChangeListener {
    public LakeQualityItem(Lake lake, BaseActivity context) {
        super(lake, context);
    }

    @Override
    public View getView() {
        if (view == null) {
            view = LinearLayout.inflate(context, R.layout.confs_river_quality, null);
            ((TextView) view.findViewById(R.id.tv_name)).setText(lake.lakeName);
            ((TextView) view.findViewById(R.id.tv_level)).setText(ResUtils.getLakeSLevel(lake.lakeLevel));
            ((ImageView) view.findViewById(R.id.iv_quality)).setImageResource(ResUtils.getQuiltySmallImg(lake.waterType));

            ((RadioGroup) view.findViewById(R.id.rg_indexs)).removeAllViews();
            ((RadioGroup) view.findViewById(R.id.rg_segments)).removeAllViews();
            ((LinearLayout) view.findViewById(R.id.ll_indexs)).removeAllViews();


            if (view.findViewById(R.id.srl_main) instanceof SwipeRefreshLayout) {
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_main);
                ViewUtils.setSwipeRefreshLayoutColorScheme(swipeRefreshLayout);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                    @Override
                    public void onRefresh() {
                        loadData();
                    }
                });
            }


            refrehDeepView();
            initedView();
            loadData();

        }
        return view;
    }

    @Override
    protected void initedView() {
        loadData();
    }



    private void refrehDeepView() {
        ((TextView) view.findViewById(R.id.tv_level)).setText(ResUtils.getLakeSLevel(lake.lakeLevel));

        RadioGroup rg_segments = (RadioGroup) view.findViewById(R.id.rg_segments);
        rg_segments.removeAllViews();
        if (lake.stations != null && lake.stations.length > 0) {
            ViewUtils.initTabLine(context, rg_segments, lake.stations, new ViewUtils.NameGetter() {
                @Override
                public String getName(Object o) {
                    return ((Station) o).stationName;
                }
            }, this);
        }
        if (lake.indexs != null && lake.indexs.length > 0) {
            ViewUtils.initIndexTable(context, (LinearLayout) view.findViewById(R.id.ll_indexs), lake.indexs);
            ViewUtils.initTabLine(context, (RadioGroup) view.findViewById(R.id.rg_indexs), lake.indexs, new ViewUtils.NameGetter() {
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
        return !(lake.stations == null || lake.stations.length == 0 || lake.indexs == null || lake.indexs.length == 0);
    }

    @Override
    public void loadData() {
        if (!isRiverInfoed()) {
            //获取湖泊站点信息
            loadLakeInfo();
        } else {
            //获取湖泊水质信息
            loadIndexData();
        }
    }

    private void loadLakeInfo() {
        JSONObject p = new JSONObject();
        try {
            p.put("lakeId", lake.lakeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRefreshing(true);
        context.getRequestContext().add("Get_OneLake_Data", new Callback<LakeDataRes>() {

            @Override
            public void callback(LakeDataRes o) {
                if (o != null && o.isSuccess()) {
                    ObjUtils.mergeObj(lake, o.data);
                    refrehDeepView();
                }
                setRefreshing(false);
            }
        }, LakeDataRes.class, p);
    }



    private void loadIndexData() {
        if (curIndex == null || curStation == null) {
            setRefreshing(false);
            return;
        }

        setRefreshing(true);
        context.getRequestContext().add("Get_LakeWaterQualityIndex_Data", new Callback<RiverQualityDataRes>() {//市级上是湖泊水质，和河道水质属性类型相同

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

                        //如果是透明度或氧化还原电位，则不显示分段条
                        if ("ORP".equals(curIndex.indexNameEN) || "Transp".equals(curIndex.indexNameEN)) {
                            view.findViewById(R.id.ll_quality_line_ycolors).setVisibility(View.INVISIBLE);
                        } else {
                            view.findViewById(R.id.ll_quality_line_ycolors).setVisibility(View.VISIBLE);
                        }
                }

                setRefreshing(false);

            }
        }, RiverQualityDataRes.class, ParamUtils.freeParam(null, "stationSerNumber",curStation.stationId, "indexId", curIndex.indexId));
    }
}
