package com.zju.hzsz.fragment.npc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.CompDetailActivity;
import com.zju.hzsz.fragment.BaseFragment;
import com.zju.hzsz.model.CompPublicity;
import com.zju.hzsz.model.CompPublicitysRes;
import com.zju.hzsz.model.CompSugs;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import java.util.ArrayList;
import java.util.List;

/**
 * 人大代表=投诉举报列表
 * Created by Wangli on 2017/4/19.
 */

public class NpcCompFragment extends BaseFragment {

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;
    private List<CompPublicity> list = new ArrayList<CompPublicity>();

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        if (listViewWarp == null) {
            adapter = new SimpleListAdapter(getBaseActivity(), list, new SimpleViewInitor() {
                @Override
                public View initView(Context context, int position, View convertView, ViewGroup viewGroup, Object data) {
                    if (convertView == null) {
                        convertView = LinearLayout.inflate(getBaseActivity(), R.layout.item_npc_comp, null);
                    }

                    getBaseActivity().getViewRender().renderView(convertView, data);

                    CompPublicity cp = (CompPublicity) data;
                    ((ImageView) convertView.findViewById(R.id.iv_status)).setImageResource(cp.isHandled() ? R.drawable.im_cp_handled : R.drawable.im_cp_unhandle);
                    ((TextView) convertView.findViewById(R.id.tv_status)).setTextColor(getBaseActivity().getResources().getColor(cp.isHandled() ? R.color.green : R.color.red));

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
                }//不加载更多，一次性全部载入。
            });

            listViewWarp.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                    if (pos < list.size()) {
                        CompPublicity cp = list.get(pos);
                        CompSugs comp = new CompSugs();
                        comp.complaintsId = cp.getId();
                        comp.complaintsPicPath = cp.getCompPicPath();
                        comp.compStatus = cp.compStatus;
                        comp.compTheme = cp.compTheme;
                        comp.complaintsContent = cp.compContent;

                        Intent intent = new Intent(getBaseActivity(), CompDetailActivity.class);
                        intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
                        intent.putExtra(Tags.TAG_ABOOLEAN, true);
                        startActivity(intent);
                    }
                }
            });

            listViewWarp.startRefresh();

            rootView = listViewWarp.getRootView();
        }

        return rootView;
    }

    //加载数据函数
    private boolean loadData(final boolean refresh) {
        if (refresh)
            listViewWarp.setRefreshing(true);
        getRequestContext().add("Get_LastComplaint_List", new Callback<CompPublicitysRes>() {
            @Override
            public void callback(CompPublicitysRes o) {
                listViewWarp.setRefreshing(false);//得到数据后就停止刷新
                if (o != null && o.isSuccess() && o.data != null) {
                    if (refresh)
                        list.clear();
                    for (CompPublicity cp : o.data) {
                        // cp.compPicPath =
                        // "http://simg.sinajs.cn/blog7style/images/common/godreply/btn.png";
                        list.add(cp);
                    }

                    adapter.notifyDataSetChanged();
                }
                if (list.size() == 0)
                    listViewWarp.setNoMore(true);
            }
        }, CompPublicitysRes.class, ParamUtils.freeParam(null));
        return true;
    }


}
