package com.zju.hzsz.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.District;
import com.zju.hzsz.model.RiverQuickSearchRes;
import com.zju.hzsz.model.SmallWater;
import com.zju.hzsz.model.SmallWaterListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 小微水体列表页
 * Created by Wangli on 2017/3/28.
 */

public class SmallWaterListActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, TextView.OnEditorActionListener{


    class DistrictWarper{
        public District district;
        public boolean checked;

        public DistrictWarper(District district) {
            super();
            this.district = district;
            this.checked = false;
        }
    }

    private List<DistrictWarper> dwItems = new ArrayList<DistrictWarper>();
    private SimpleListAdapter dwAdapter = null;
    private DistrictWarper curDw = null;

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;

    private List<SmallWater> smallWaters = new ArrayList<SmallWater>();

    //点击单个item，进行跳转
    View.OnClickListener smallWaterClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getTag() != null) {
                Intent intent = new Intent(SmallWaterListActivity.this, SmallWaterActivity.class);
                intent.putExtra(Tags.TAG_SMALLWATER, StrUtils.Obj2Str(view.getTag()));
                startActivity(intent);
            }
        }
    };

    private SimpleViewInitor smallWaterInitor = new SimpleViewInitor() {
        @Override
        public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

            if (convertView == null) {
                convertView = LinearLayout.inflate(context, R.layout.item_smallwater, null);
            }

            SmallWater sw = (SmallWater) data;

            ((TextView) convertView.findViewById(R.id.tv_smallwater_name)).setText(sw.waterName);
            ((TextView) convertView.findViewById(R.id.tv_smallwater_position)).setText(sw.position);

            convertView.setTag(sw);
            convertView.setOnClickListener(smallWaterClick);
            return convertView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //直接用管辖河道列表页的UI布局
        setContentView(R.layout.activity_smallwater_list);
        setTitle("所有小微水体");
        initHead(R.drawable.ic_head_back, 0);

        //=======================dw部分==============================================

        ((EditText) findViewById(R.id.et_keyword)).setOnEditorActionListener(this); //按键盘处的搜索也可运行startSearch函数

        //区划的适配器
        dwAdapter = new SimpleListAdapter(SmallWaterListActivity.this, dwItems, new SimpleViewInitor() {

            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
                if (convertView == null) {
                    convertView = LinearLayout.inflate(context, R.layout.item_keyword, null);
                }
                DistrictWarper dw = (DistrictWarper) data;

                ((CheckBox) convertView).setText(dw.district.districtName); //view是一个checkBox

                ((CheckBox) convertView).setChecked(dw.checked); //默认都是false
                ((CheckBox) convertView).setTag(dw);
                ((CheckBox) convertView).setOnCheckedChangeListener(SmallWaterListActivity.this);
                return convertView;
            }
        });

        //区划布局
        GridView gl = (GridView) findViewById(R.id.gv_areas);
        gl.setAdapter(dwAdapter);

        getRequestContext().add("riverquicksearch_data_get", new Callback<RiverQuickSearchRes>() {
            @Override
            public void callback(RiverQuickSearchRes o) {
                if (o != null && o.isSuccess()) {
                    //第一行第一列的“不限”
                    dwItems.clear();
                    District ds = new District();
                    ds.districtId = 0;
                    ds.districtName = getString(R.string.unlimeited);
                    curDw = new DistrictWarper(ds);
                    dwItems.add(curDw);

                    for (District d : o.data.districtLists) {
                        DistrictWarper dw = new DistrictWarper(d); //id + name
                        dwItems.add(dw);
                        // if (curDw == null && d.districtName.contains("上城")) {
                        // curDw = dw;
                        // curDw.checked = true;
                        // }
                    }
                    if (curDw != null)
                        loadSmallWaters(true);
                    // curDw = dwItems.get(0);
                    // curDw.checked = true;
                    dwAdapter.notifyDataSetInvalidated();
                }
            }
        }, RiverQuickSearchRes.class, ParamUtils.freeParam(null));

        adapter = new SimpleListAdapter(this, smallWaters, smallWaterInitor);
        listViewWarp = new ListViewWarp(this, adapter, new ListViewWarp.WarpHandler() {
            @Override
            public boolean onRefresh() {
                loadSmallWaters(true);
                return true;
            }

            @Override
            public boolean onLoadMore() {
                loadSmallWaters(false);
                return true;
            }
        });

        ((LinearLayout) findViewById(R.id.ll_main)).addView(listViewWarp.getRootView());

        loadSmallWaters(true);
    }

    private void loadSmallWaters(final boolean refresh) {
        JSONObject p = null;
        if (curDw == null || curDw.district == null || curDw.district.districtId == 0) {
            //若没有选择区划
            p = ParamUtils.freeParam(getPageParam(refresh), "searchContent", getKeyword());
        } else {
            //若选择了区划，则增加区划id
            p = ParamUtils.freeParam(getPageParam(refresh), "searchContent", getKeyword(), "districtId", curDw.district.districtId);
        }

        showOperating();
        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);
        getRequestContext().add("Get_SmallWaterList", new Callback<SmallWaterListRes>() {
            @Override
            public void callback(SmallWaterListRes o) {

                listViewWarp.setLoadingMore(false);
                listViewWarp.setRefreshing(false);

                if (o != null && o.isSuccess()) {
                    if (refresh) {
                        smallWaters.clear();
                    }
                    for (SmallWater sw : o.data.smallWaterSums) {
                        smallWaters.add(sw);
                    }

                    adapter.notifyDataSetChanged();

                }

                hideOperating();
                if ((o != null && o.data != null && o.data.smallWaterSums != null) && (o.data.pageInfo != null && smallWaters.size() >= o.data.pageInfo.totalCounts || o.data.smallWaterSums.length == 0)) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }

            }
        }, SmallWaterListRes.class, p);
    }

    /**
     * 区划选择
     * @param arg0
     * @param arg1
     */
    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        if (arg1) {
            curDw = (DistrictWarper) arg0.getTag();
            for (DistrictWarper d : dwItems) {
                d.checked = false;
            }
            curDw.checked = true;
            dwAdapter.notifyDataSetChanged();

            //选择区划区划之后则开始搜索
            loadSmallWaters(true);
        }
        // ((EditText) findViewById(R.id.et_keyword)).requestFocus();
        //当选择区划之后则通过InputMethodManager隐藏键盘
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.et_keyword)).getApplicationWindowToken(), 0);
        }
    }

    /**
     * 在EditText输入后，不点击Button进行请求，而是直接点击软键盘上的"回车"，那么也应该能够正常响应请求
     * @param arg0
     * @param actId 搜索键
     * @param arg2
     * @return
     */
    @Override
    public boolean onEditorAction(TextView arg0, int actId, KeyEvent arg2) {
        if (actId == EditorInfo.IME_ACTION_SEARCH) {
            loadSmallWaters(true);
        }
        return false;
    }


    private final int DefaultPageSize = Constants.DefaultPageSize;

    protected JSONObject getPageParam(boolean refresh) {
        JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1) :
                ParamUtils.pageParam (DefaultPageSize, (smallWaters.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
        return j;
    }

    /**
     * 得到搜索关键词
     * @return 关键词
     */
    private String getKeyword() {
        return ((EditText) findViewById(R.id.et_keyword)).getText().toString();
    }
}
