package com.zju.hzsz.chief.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.CompDetailActivity;
import com.zju.hzsz.model.CompPublicity;
import com.zju.hzsz.model.CompPublicitysRes;
import com.zju.hzsz.model.CompSugs;
import com.zju.hzsz.model.LowLevelRiver;
import com.zju.hzsz.model.RiverRecord;
import com.zju.hzsz.model.RiverRecordListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Wangli on 2017/3/24.
 */

public class ChiefLowLevelPubActivity extends BaseActivity {

    private LowLevelRiver river = null;
    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;
    private List<Object> list = new ArrayList<Object>();
    private boolean isComp = true;

    public String year = "2017";
    public String month = "1";
    private YearMonthSelectDialog selectDialog = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_lowlevelpub);

        initHead(R.drawable.ic_head_back, 0);
        //获取在河道列表所点击的河道具体信息
        river = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_RIVER), LowLevelRiver.class);
        //设置页面标题
        setTitle(river.riverName);
        //设置当前年月
        year = "" + Calendar.getInstance().get(Calendar.YEAR);
        month = "" + (Calendar.getInstance().get(Calendar.MONTH) + 1);

        //为RadioGroup绑定监听器
        ((RadioGroup) findViewById(R.id.rg_river_pubinfo_showwith)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int rid) {
                isComp = rid == R.id.rb_river_infopub_tsxx;
                findViewById(R.id.ll_recodrhead).setVisibility(isComp ? View.GONE : View.VISIBLE);
                //加载数据
                loadData(true);
            }
        });

        //适配器
        adapter = new SimpleListAdapter(this, list, new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
                //判断是投诉建议还是巡查记录
                boolean isComp = data instanceof CompPublicity;
                //根据投诉建议和巡查记录给定相应的布局
                if (isComp && (convertView == null || convertView.findViewById(R.id.ll_comp_container) == null)) {
                    convertView = LinearLayout.inflate(context, R.layout.item_river_comppublicity, null);
                } else if (!isComp && (convertView == null || convertView.findViewById(R.id.ll_comp_container) != null)) {
                    convertView = LinearLayout.inflate(context, R.layout.item_record_3fied, null);
                }
                //根据投诉建议和巡查记录给布局中的元素赋值
                if (isComp) {
                    getViewRender().renderView(convertView, data);
                    CompPublicity cp = (CompPublicity) data;
                    ((ImageView) convertView.findViewById(R.id.iv_status)).setImageResource(cp.isHandled() ? R.drawable.im_cp_handled : R.drawable.im_cp_unhandle);
                    ((TextView) convertView.findViewById(R.id.tv_status)).setTextColor(context.getResources().getColor(cp.isHandled() ? R.color.green :R.color.red));
                } else {
                    RiverRecord record = (RiverRecord) data;
                    getViewRender().renderView(convertView, record);
                }
                return convertView;
            }
        });

        //listViewWarp
        listViewWarp = new ListViewWarp(this, adapter, new ListViewWarp.WarpHandler() {
            @Override
            public boolean onRefresh() {
                return loadData(true);
            }

            @Override
            public boolean onLoadMore() {
                return false;
            }
        });

        listViewWarp.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                if (pos < list.size()) {
                    Object o = list.get(pos);
                    if (o instanceof CompPublicity) {
                        CompPublicity cp = (CompPublicity) o;
                        CompSugs compSugs = new CompSugs();
                        compSugs.complaintsId = cp.getId();
                        compSugs.complaintsPicPath = cp.getCompPicPath();
                        compSugs.compStatus = cp.compStatus;

                        Intent intent = new Intent(ChiefLowLevelPubActivity.this, CompDetailActivity.class);
                        intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(compSugs));
                        intent.putExtra(Tags.TAG_ABOOLEAN, true);
                        startActivity(intent);
                    } else if (o instanceof RiverRecord) {
                        RiverRecord record = (RiverRecord) o;
                        if (record != null) {
                            Intent intent = new Intent(ChiefLowLevelPubActivity.this, ChiefEditRecordActivity.class);
                            intent.putExtra(Tags.TAG_RECORD, StrUtils.Obj2Str(record));
                            intent.putExtra(Tags.TAG_ABOOLEAN, true);
                            startActivityForResult(intent, Tags.CODE_EDIT);
                        }
                    }
                }
            }
        });

        listViewWarp.startRefresh();
        ((LinearLayout) findViewById(R.id.ll_river_pubinfo)).addView(listViewWarp.getRootView());

        findViewById(R.id.tv_seldate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( selectDialog == null) {
                    selectDialog = new YearMonthSelectDialog(ChiefLowLevelPubActivity.this, new YearMonthSelectDialog.Callback() {
                        @Override
                        public void onYMSelected(int y, int m) {
                            year = "" + y;
                            month = "" + m;
                            refreshDateView();
                            loadData(true);
                        }
                    });
                }
                selectDialog.show();
            }
        });

        refreshDateView();
    }

    /**
     * 更新日期函数
     */
    private void refreshDateView() {
        ((TextView) findViewById(R.id.tv_seldate)).setText(year + "年" + month + "月");
    }

    /**
     * 加载数据
     * @param refresh
     * @return
     */
    private boolean loadData(final boolean refresh) {
        if (refresh)
            listViewWarp.setRefreshing(true);
        if (refresh){
            list.clear();
            adapter.notifyDataSetInvalidated();
        }

        listViewWarp.setNoMoreText(isComp ? "暂无投诉" : "暂无记录");

        if (isComp) {
            //点击了投诉建议
            getRequestContext().add("Get_RiverComplaint_List", new Callback<CompPublicitysRes>() {
                @Override
                public void callback(CompPublicitysRes o) {
                    listViewWarp.setRefreshing(false);
                    if (o != null && o.isSuccess() && o.data != null) {
                        if (refresh)
                            list.clear();
                        for (CompPublicity cp : o.data) {
                            list.add(cp);
                        }

                        adapter.notifyDataSetInvalidated();
                    }

                    if (list.size() == 0) {
                        listViewWarp.setNoMore(true);
                    } else {
                        listViewWarp.setNoMore(false);
                    }
                }
            }, CompPublicitysRes.class, ParamUtils.freeParam(null, "riverId", river.riverId));
        } else {
            //点击了巡河记录
            getRequestContext().add("Get_RiverRecord_List", new Callback<RiverRecordListRes>() {
                @Override
                public void callback(RiverRecordListRes o) {
                    listViewWarp.setRefreshing(false);
                    if (o != null && o.isSuccess() && o.data != null) {
                        if (refresh)
                            list.clear();
                        for (RiverRecord rc : o.data) {
                            list.add(rc);
                        }

                        adapter.notifyDataSetInvalidated();
                    }

                    if (list.size() == 0)
                        listViewWarp.setNoMore(true);
                    else
                        listViewWarp.setNoMore(false);
                }
            }, RiverRecordListRes.class, ParamUtils.freeParam(null, "riverID", river.riverId,
                    "month", month, "year" , year,
                    "authority", getUser().getAuthority()));
        }
        return true;
    }
}
