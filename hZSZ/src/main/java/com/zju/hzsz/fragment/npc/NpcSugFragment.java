package com.zju.hzsz.fragment.npc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.activity.NpcMemberActivity;
import com.zju.hzsz.fragment.BaseFragment;
import com.zju.hzsz.model.DeputySupervise;
import com.zju.hzsz.model.NpcSugListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.npc.activity.NpcSugDetailActivity;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 人大代表-“监督河长”
 * Created by Wangli on 2017/4/23.
 */

public class NpcSugFragment extends BaseFragment {

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;
    private List<Object> list = new ArrayList<Object>();
    JSONObject params = null;
    int deputyId = 0;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (((TextView) getActivity().findViewById(R.id.tv_head_title)).getText().equals("代表监督详情页"))
            deputyId = NpcMemberActivity.npc.deputyId;


        adapter = new SimpleListAdapter(getBaseActivity(), list, new SimpleViewInitor() {

            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

                convertView = LinearLayout.inflate(context, R.layout.item_npc_sug, null);

                final DeputySupervise ds = (DeputySupervise) data;
                getBaseActivity().getViewRender().renderView(convertView, ds);

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getBaseActivity(), NpcSugDetailActivity.class);
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
                return loadData(true);
            }

            @Override
            public boolean onLoadMore() {
                loadData(false);
                return true;
            }
        });

        listViewWarp.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                if (pos < list.size()) {
                    Object o = list.get(pos);

                  /*  RiverRecord record = (RiverRecord) o;
                    if (record != null) {
                        Intent intent = new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefEditRecordActivity.class);
                        intent.putExtra(Tags.TAG_RECORD, StrUtils.Obj2Str(record));
                        intent.putExtra(Tags.TAG_ABOOLEAN, true);
                        getBaseActivity().startActivityForResult(intent, Tags.CODE_EDIT);
                    }*/

                }
            }
        });

        listViewWarp.startRefresh();

        if (this.rootView == null) {
            rootView = listViewWarp.getRootView();
        }


        return rootView;
    }


    private boolean loadData(final boolean refresh) {

        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);


        if (deputyId != 0) {
            params = ParamUtils.freeParam(getPageParam(refresh), "deputyId", deputyId);
        } else {
            params = ParamUtils.freeParam(getPageParam(refresh));
        }


        getBaseActivity().getRequestContext().add("Get_DeputySupervise_List", new Callback<NpcSugListRes>() {
            @Override
            public void callback(NpcSugListRes o) {

                listViewWarp.setLoadingMore(false);
                listViewWarp.setRefreshing(false);

                if (o != null && o.isSuccess() && o.data != null) {
                    if (refresh)
                        list.clear();
                    for (DeputySupervise ds : o.data.deputySuperviseJsons) {
                        list.add(ds);
                    }

                     adapter.notifyDataSetChanged();
                }

                if ((o != null && o.data != null && o.data.deputySuperviseJsons != null) && (o.data.pageInfo != null && list.size() >= o.data.pageInfo.totalCounts || o.data.deputySuperviseJsons.length == 0)) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }

            }
        }, NpcSugListRes.class, params);

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
