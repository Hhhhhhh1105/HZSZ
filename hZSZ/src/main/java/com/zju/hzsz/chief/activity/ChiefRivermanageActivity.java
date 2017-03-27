package com.zju.hzsz.chief.activity;

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
import com.zju.hzsz.model.LowLevelRiver;
import com.zju.hzsz.model.LowLevelRiverListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangli on 2017/3/22.
 */

public class ChiefRivermanageActivity extends BaseActivity {

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;

    private List<LowLevelRiver> rivers = new ArrayList<LowLevelRiver>();

    private View.OnClickListener lowLeverRiverClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getTag() != null) {
                Intent intent = new Intent(ChiefRivermanageActivity.this, com.zju.hzsz.chief.activity.ChiefLowLevelPubActivity.class);
                intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(v.getTag()));
                startActivity(intent);
            }
        }
    };

    private SimpleViewInitor riverInitor = new SimpleViewInitor() {
        @Override
        public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

            if (convertView == null) {
                convertView = LinearLayout.inflate(context, R.layout.item_chief_rivermanage, null);
            }

            LowLevelRiver river = (LowLevelRiver) data;
            //设置河道名字 河道等级
            ((TextView) convertView.findViewById(R.id.tv_name)).setText(river.riverName);
            ((TextView) convertView.findViewById(R.id.tv_level)).setText(ResUtils.getRiverSLittleLevel(river.riverLevel));
            //设置河道标签 监听函数
            convertView.setTag(river);
            convertView.setOnClickListener(lowLeverRiverClick);
            return convertView;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_rivermanage);
        setTitle("所有管辖河道");
        initHead(R.drawable.ic_head_back, 0);

        adapter = new SimpleListAdapter(this, rivers, riverInitor);
        listViewWarp = new ListViewWarp(this, adapter, new ListViewWarp.WarpHandler() {

            @Override
            public boolean onRefresh() {
                loadRivers(true);
                return true;
            }

            @Override
            public boolean onLoadMore() {
                loadRivers(false);
                return true;
            }
        });

        ((LinearLayout) findViewById(R.id.ll_main)).addView(listViewWarp.getRootView());

        //用于进入页面时加载
        loadRivers(true);

    }

    private void loadRivers(final boolean refresh) {
        showOperating();
        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);
        getRequestContext().add("Get_lowLevelRiver_List", new Callback<LowLevelRiverListRes>() {
            @Override
            public void callback(LowLevelRiverListRes o) {
                listViewWarp.setRefreshing(false);
                listViewWarp.setLoadingMore(false);

                if (o != null && o.isSuccess()) {
                    if (refresh)
                        rivers.clear();
                    for (LowLevelRiver r : o.data.lowLevelRivers) {
                        rivers.add(r);
                    }

                    adapter.notifyDataSetChanged();
                }
                hideOperating();
                if ((o != null && o.data != null && o.data.lowLevelRivers != null) && (o.data.pageInfo != null && rivers.size() >= o.data.pageInfo.totalCounts || o.data.lowLevelRivers.length == 0)) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, LowLevelRiverListRes.class, getPageParam(refresh));
    }

    private final int DefaultPageSize = Constants.DefaultPageSize;

    protected JSONObject getPageParam(boolean refresh) {
        JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (rivers.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
        try{
            j.put("authority", getUser().getAuthority());
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return j;
    }
}
