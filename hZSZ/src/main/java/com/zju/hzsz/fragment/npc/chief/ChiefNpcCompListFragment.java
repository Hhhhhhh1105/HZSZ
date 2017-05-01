package com.zju.hzsz.fragment.npc.chief;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.adapter.PagerItem;
import com.zju.hzsz.adapter.SimplePagerAdapter;
import com.zju.hzsz.chief.activity.ChiefCompDetailActivity;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.fragment.BaseFragment;
import com.zju.hzsz.model.ChiefComp;
import com.zju.hzsz.model.ChiefCompListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ArrUtils;
import com.zju.hzsz.utils.DipPxUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * 河长个人中心-代表监督-人大投诉举报
 * Created by Wangli on 2017/4/30.
 */

public class ChiefNpcCompListFragment extends BaseFragment implements ViewPager.OnPageChangeListener, RadioGroup.OnCheckedChangeListener{

    private boolean isComp = true;
    private int[] rdids = new int[] { R.id.rb_chief_comp_unhandle,
            R.id.rb_chief_comp_handled };
    private List<PagerItem> pagerItems = null;
    private SimplePagerAdapter adapter = null;

    //投诉列表页
    public class CompListPager extends PagerItem implements ListViewWarp.WarpHandler {

        private View view = null;
        private int type = 0;
        private ListViewWarp listViewWarp = null;
        private List<ChiefComp> items = new ArrayList<ChiefComp>();
        private SimpleListAdapter adapter = null;

        //点击处理投诉单
        private View.OnClickListener btnClk = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() instanceof ChiefComp) {
                    ChiefComp comp = (ChiefComp) view.getTag();
                    Intent intent = new Intent(getBaseActivity(), ChiefCompDetailActivity.class);
                    intent.putExtra(Tags.TAG_ISNPCCOMP, true);//代表是河长的投诉
                    intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
                    intent.putExtra(Tags.TAG_ISCOMP, isComp);
                    intent.putExtra(Tags.TAG_HANDLED, type != 0);

                    startActivityForResult(intent, Tags.CODE_COMP);
                }
            }
        };

        private SimpleViewInitor initor = new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position, View convertView, ViewGroup viewGroup, Object data) {
                if (convertView == null) {
                    convertView = LinearLayout.inflate(getBaseActivity(), R.layout.item_chief_complaint2, null);
                }
                ViewWarp warp = new ViewWarp(convertView, getBaseActivity());
                ChiefComp comp = (ChiefComp) data;
                warp.setText(R.id.tv_status, comp.getStatuss());  //处理状态
                warp.setText(R.id.tv_sernum, comp.getSerNum());   //投诉单编号
                warp.setText(R.id.tv_title, comp.getTheme());   //投诉单标题
                warp.setText(R.id.tv_content, comp.getContent());  //投诉单内容
                warp.setText(R.id.tv_time, comp.getDate() != null ? comp
                        .getDate().getYMDHM(getBaseActivity()) : "");    //投诉时间

                if (type == 0) {
                    ((Button) warp.getViewById(R.id.btn_handle))
                            .setText(isComp ? "处理投诉" : "处理建议");         //0是未处理，1是已处理
                } else {
                    ((Button) warp.getViewById(R.id.btn_handle))
                            .setText("查看处理单");
                }
                ((Button) warp.getViewById(R.id.btn_handle)).setTag(comp);
                ((Button) warp.getViewById(R.id.btn_handle))
                        .setOnClickListener(btnClk);
                return convertView;
            }
        };

        //CompListPager类的构造函数
        public CompListPager(int type) {
            super();
            this.type = type;
        }

        @Override
        public View getView() {
            if (view == null) {
                view = LinearLayout.inflate(getBaseActivity(),
                        R.layout.confs_chief_complist, null);
                adapter = new SimpleListAdapter(getBaseActivity(),
                        items, initor);
                listViewWarp = new ListViewWarp(getBaseActivity(),
                        adapter, this);
                listViewWarp.getListView().setDivider(
                        new ColorDrawable(getResources().getColor(
                                R.color.bg_gray)));
                listViewWarp.getListView().setDividerHeight(
                        DipPxUtils.dip2px(
                                getBaseActivity(),
                                getResources().getDimension(
                                        R.dimen.padding_medium)));
                ((LinearLayout) view.findViewById(R.id.ll_main))
                        .addView(listViewWarp.getRootView());

                loadComps(true);
            }
            return view;
        }

        @Override
        public boolean onRefresh() {
            loadComps(true);
            return true;
        }

        @Override
        public boolean onLoadMore() {
            loadComps(false);
            return true;
        }

        private void loadComps(final boolean refresh) {
            if (refresh)
                listViewWarp.setRefreshing(true);
            else
                listViewWarp.setLoadingMore(true);
            getRequestContext().add(
                    "Get_ChiefDeputyComplain_List", new Callback<ChiefCompListRes>() {
                        @Override
                        public void callback(ChiefCompListRes o) {
                            listViewWarp.setRefreshing(false);
                            listViewWarp.setLoadingMore(false);

                            if (o != null && o.isSuccess()) {
                                if (refresh)
                                    items.clear();
                                for (ChiefComp c : o.data.complainSum) {
                                    items.add(c);
                                }
                                adapter.notifyDataSetChanged();

                                //无更多
                                if (items.size() >= o.data.pageInfo.totalCounts) {
                                    listViewWarp.setNoMore(true);
                                }
                            }
                        }
                    }, ChiefCompListRes.class, getPageParam(refresh));
        }

        private final int DefaultPageSize = Constants.DefaultPageSize;

        protected JSONObject getPageParam(boolean refresh) {
            JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1)
                    : ParamUtils.pageParam(DefaultPageSize, (items.size()
                    + DefaultPageSize - 1)
                    / DefaultPageSize + 1);
            try {
                j.put("ifDeal", type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return j;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (this.rootView == null) {
            rootView = LinearLayout.inflate(getBaseActivity(), R.layout.fragment_chief_npccomp, null);
        }


        ((ViewPager) rootView.findViewById(R.id.vp_chief_comp_tab))
                .setOnPageChangeListener(this);
        ((RadioGroup) rootView.findViewById(R.id.rg_chief_comp))
                .setOnCheckedChangeListener(this);

        pagerItems = new ArrayList<PagerItem>();
        pagerItems.add(new CompListPager(0));   //未处理
        pagerItems.add(new CompListPager(1));   //已处理
        adapter = new SimplePagerAdapter(pagerItems);
        ((ViewPager) rootView.findViewById(R.id.vp_chief_comp_tab)).setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        ((RadioButton) rootView.findViewById(rdids[position])).setChecked(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int rdid) {
        int ix = ArrUtils.indexOf(rdids, rdid);
        if (ix >= 0) {
            ((ViewPager) rootView.findViewById(R.id.vp_chief_comp_tab))
                    .setCurrentItem(ix);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ((CompListPager) pagerItems
                    .get(((ViewPager) rootView.findViewById(R.id.vp_chief_comp_tab))
                            .getCurrentItem())).loadComps(true);
        }
    }
}
