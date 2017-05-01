package com.zju.hzsz.fragment.npc.chief;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.fragment.BaseFragment;
import com.zju.hzsz.model.ChiefNpcSugListRes;
import com.zju.hzsz.model.DeputySupervise;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.npc.activity.ChiefNpcSugDetailActivity;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 河长个人中心-代表监督-人大监督建议
 * Created by Wangli on 2017/4/30.
 */

public class ChiefNpcSugListFragment extends BaseFragment {

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;
    private List<Object> list = new ArrayList<Object>();
    JSONObject params = null;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //适配器：每个监督条目的UI及数据填充
        adapter = new SimpleListAdapter(getBaseActivity(), list, new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position, View convertView, ViewGroup viewGroup, Object data) {
                if (convertView == null) {
                    convertView = LinearLayout.inflate(context, R.layout.item_chief_npcsug, null);
                }

                final DeputySupervise ds = (DeputySupervise) data;
                getBaseActivity().getViewRender().renderView(convertView, ds);

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getBaseActivity(), ChiefNpcSugDetailActivity.class);
                        intent.putExtra("npcsug", StrUtils.Obj2Str(ds));
                        startActivity(intent);
                    }
                });

                return convertView;
            }

        });

        listViewWarp = new ListViewWarp(getBaseActivity(), adapter, new ListViewWarp.WarpHandler() {
            @Override
            public boolean onRefresh() {
                loadData(true);
                return true;
            }

            @Override
            public boolean onLoadMore() {
                loadData(false);
                return true;
            }
        });

        //用于第一次的数据刷新
        listViewWarp.startRefresh();

        if (this.rootView == null) {
            View view = LinearLayout.inflate(getBaseActivity(), R.layout.inc_chiefsug, null);
            listViewWarp.getListView().addHeaderView(view);
            rootView = listViewWarp.getRootView();
        }
        return rootView;

    }


    private boolean loadData(final boolean refresh) {
        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);

        getBaseActivity().getRequestContext().add("Get_ChiefDeputySupervise_List", new Callback<ChiefNpcSugListRes>() {
            @Override
            public void callback(ChiefNpcSugListRes o) {

                listViewWarp.setLoadingMore(false);
                listViewWarp.setRefreshing(false);

                if (o != null && o.isSuccess() && o.data != null) {
                    if (refresh)
                        list.clear();
                    for (DeputySupervise ds : o.data.deputySuperviseSum) {
                        list.add(ds);
                    }
                    adapter.notifyDataSetChanged();
                }

                if ((o != null && o.data != null && o.data.deputySuperviseSum != null) && (o.data.pageInfo != null && list.size() >= o.data.pageInfo.totalCounts || o.data.deputySuperviseSum.length == 0)) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }

            }
        }, ChiefNpcSugListRes.class, ParamUtils.freeParam(getPageParam(refresh), "ifDeal", "0"));

        return true;
    }

    //分页函数
    private final int DefaultPageSize = Constants.DefaultPageSize;

    protected JSONObject getPageParam(boolean refresh) {
        JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1) :
                ParamUtils.pageParam (DefaultPageSize, (list.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
        return j;
    }

}
