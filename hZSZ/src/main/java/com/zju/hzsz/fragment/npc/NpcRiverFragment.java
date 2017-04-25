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
import com.zju.hzsz.Tags;
import com.zju.hzsz.chief.activity.YearMonthSelectDialog;
import com.zju.hzsz.fragment.BaseFragment;
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
 * 人大代表巡河记录界面
 * Created by Wangli on 2017/4/19.
 */

public class NpcRiverFragment extends BaseFragment {

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;
    private List<Object> list = new ArrayList<Object>();

    public String year = "2017";
    public String month = "4";
    private YearMonthSelectDialog selectDialog = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //将日期设置为当前年月
        year = "" + Calendar.getInstance().get(Calendar.YEAR);
        month = "" + (Calendar.getInstance().get(Calendar.MONTH) + 1);

        adapter = new SimpleListAdapter(getBaseActivity(), list, new SimpleViewInitor() {

            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

                convertView = LinearLayout.inflate(context, R.layout.item_npc_record, null);

                RiverRecord record = (RiverRecord) data;
                getBaseActivity().getViewRender().renderView(convertView, record);

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
                return false;
            }
        });

        listViewWarp.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                if (pos < list.size()) {
                    Object o = list.get(pos);

                    RiverRecord record = (RiverRecord) o;
                    if (record != null) {
                        Intent intent = new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefEditRecordActivity.class);
                        intent.putExtra(Tags.TAG_RECORD, StrUtils.Obj2Str(record));
                        intent.putExtra(Tags.TAG_ABOOLEAN, true);
                        getBaseActivity().startActivityForResult(intent, Tags.CODE_EDIT);
                    }

                }
            }
        });

        listViewWarp.startRefresh();

        if (this.rootView == null) {
            View view = LinearLayout.inflate(getBaseActivity(), R.layout.inc_seldate, null);
            listViewWarp.getListView().addHeaderView(view);
            rootView = listViewWarp.getRootView();
        }

        rootView.findViewById(R.id.tv_seldate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectDialog == null) {
                    selectDialog = new YearMonthSelectDialog(getBaseActivity(), new YearMonthSelectDialog.Callback() {
                        @Override
                        public void onYMSelected(int y, int m) {
                            year = "" + y;
                            month = "" + m;
                            refreshDateView();
                            //刷新数据
                            loadData(true);
                        }
                    });
                }
                selectDialog.show();
            }
        });

        refreshDateView();


        return rootView;
    }

    //日期更新函数
    private void refreshDateView() {
        ((TextView) rootView.findViewById(R.id.tv_seldate)).setText(year + "年" + month + "月");
    }

    private boolean loadData(final boolean refresh) {
        if (refresh)
            listViewWarp.setRefreshing(true);
        if (refresh) {
            list.clear();
            adapter.notifyDataSetInvalidated();
        }


        getBaseActivity().getRequestContext().add("Get_RiverRecord_List", new Callback<RiverRecordListRes>() {
            @Override
            public void callback(RiverRecordListRes o) {
                listViewWarp.setRefreshing(false);
                if (o != null && o.isSuccess() && o.data != null) {
                    if (refresh)
                        list.clear();
                    for (RiverRecord cp : o.data) {
                        list.add(cp);
                    }

                    // adapter.notifyDataSetChanged();
                    adapter.notifyDataSetInvalidated();
                }
                if (list.size() == 0) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, RiverRecordListRes.class, ParamUtils.freeParam(null, "riverID", 378,
                "month", month, "year", year,
                "authority", 11));


        return true;
    }
}
