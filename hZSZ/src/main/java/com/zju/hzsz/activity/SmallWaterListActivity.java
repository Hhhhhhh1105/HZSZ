package com.zju.hzsz.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
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

public class SmallWaterListActivity extends BaseActivity {

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
        setContentView(R.layout.activity_chief_rivermanage);
        setTitle("所有小微水体");
        initHead(R.drawable.ic_head_back, 0);

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
        }, SmallWaterListRes.class, getPageParam(refresh));
    }

    private final int DefaultPageSize = Constants.DefaultPageSize;

    protected JSONObject getPageParam(boolean refresh) {
        JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1) :
                ParamUtils.pageParam(DefaultPageSize, (smallWaters.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
        return j;
    }
}
