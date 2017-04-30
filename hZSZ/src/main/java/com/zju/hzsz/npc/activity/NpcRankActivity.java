package com.zju.hzsz.npc.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Values;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.chief.activity.YearMonthSelectDialog;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.District;
import com.zju.hzsz.model.NpcRanking;
import com.zju.hzsz.model.NpcRankingRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 人大代表-履职详情页
 * Created by Wangli on 2017/4/22.
 */

public class NpcRankActivity extends BaseActivity {

    //适配器相关
    private SimpleListAdapter adapter = null;
    private ListView lv_ranking;
    private List<NpcRanking> rankings = new ArrayList<NpcRanking>(); //数据列表

    //日期选择相关变量
    public String year = "2017";
    public String month = "4";
    private YearMonthSelectDialog selectDialog = null;

    //区划弹窗相关变量
    private PopupWindow areaPop = null;
    private View areaView = null;
    private District curDistrict = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npc_rank);

        setTitle("代表履职排名");
        initHead(R.drawable.ic_head_back, R.drawable.ic_head_order);

        //将日期设置为当前年月
        year = "" + Calendar.getInstance().get(Calendar.YEAR);
        month = "" + (Calendar.getInstance().get(Calendar.MONTH) + 1);
        refreshDateView();

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_listview);
        swipeRefreshLayout.setEnabled(false);
        lv_ranking = (ListView) findViewById(R.id.lv_ranking);

        //区划选择监听器
        findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAreaPop();
            }
        });

        //日期选择监听器
        findViewById(R.id.tv_seldate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectDialog == null) {
                    selectDialog = new YearMonthSelectDialog(NpcRankActivity.this, new YearMonthSelectDialog.Callback() {
                        @Override
                        public void onYMSelected(int y, int m) {
                            year = "" + y;
                            month = "" + m;
                            refreshDateView();
                            //刷新数据
                            onRefresh();
                        }
                    });
                }
                selectDialog.show();
            }
        });

        for (District d : Values.districtLists) {
            //这里默认是所在区划
            if (getUser().getDistrictId() != 0) {
                if (d.districtId == getUser().getDistrictId())
                    curDistrict = d;
            }
        }
        if (curDistrict == null)
            curDistrict = Values.districtLists[0];

        adapter = new SimpleListAdapter(this, rankings, new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
                if (convertView == null) {
                    convertView = LinearLayout.inflate(context, R.layout.item_npc_ranking, null);
                }
                NpcRanking nr = (NpcRanking) data;

                ViewWarp warp = new ViewWarp(convertView, context);
                warp.setText(R.id.tv_index, (position + 1) + "");
                warp.setText(R.id.tv_name, nr.deputyName);
                warp.setText(R.id.tv_worksum, nr.deputyJobSum + "");

                return convertView;
            }
        });
        lv_ranking.setAdapter(adapter);

        onRefresh();

    }

    //日期更新函数
    private void refreshDateView() {
        ((TextView) findViewById(R.id.tv_seldate)).setText(year + "年" + month + "月");
    }

    //数据更新函数
    public void onRefresh() {

        showOperating();

        //发送请求
        //test
      /*  rankings.clear();
        for (int i = 0; i < 10; i ++) {
            NpcRanking np = new NpcRanking();
            np.setNpcName("张大大");
            np.setNpcWorkSum(10 - i);
            rankings.add(np);
        }*/

        getRequestContext().add("Get_Deputies_RankingList", new Callback<NpcRankingRes>() {
            @Override
            public void callback(NpcRankingRes o) {

                hideOperating();

                if (o != null && o.isSuccess()) {
                    rankings.clear();
                    for (NpcRanking nr : o.data) {
                        rankings.add(nr);
                    }
                    adapter.notifyDataSetChanged();

                    if (o.data.length == 0) {
                        showToast("暂时无排行榜数据");
                    }

                }

            }
        }, NpcRankingRes.class, ParamUtils.freeParam(null, "year", year, "month", month, "districtId", curDistrict.districtId));

    }





    //出现弹窗与背景
    private void showArea() {
        areaPop.showAsDropDown(this.findViewById(R.id.v_poptag));
        findViewById(R.id.v_mask).setVisibility(View.VISIBLE);
    }

    //解除弹窗与背景
    private void dismissArea() {
        areaPop.dismiss();
        findViewById(R.id.v_mask).setVisibility(View.GONE);
    }

    //实现弹窗的主要函数
    public void showAreaPop() {
        if (areaPop == null) {
            areaView = LinearLayout.inflate(this, R.layout.inc_arealist, null);
            LinearLayout ll_areas = (LinearLayout) areaView.findViewById(R.id.ll_areas);

            //点击弹窗中的textView之后更新排行榜
            View.OnClickListener clk = new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dismissArea();
                    curDistrict = (District) arg0.getTag(); //这是数据改变的重点所在
                    ((TextView) findViewById(R.id.tv_rankingtitle)).setText("" + curDistrict.districtName + "代表履职排行榜");

                    //根据区划刷新排行榜数据
                    onRefresh();
                }
            };

            //添加弹窗VIEW并绑定监听器，通过setTag方法实现数据传输
            for (District d : Values.districtLists) {
                View view = LinearLayout.inflate(this, R.layout.item_ranking_area, null);
                ((TextView) view.findViewById(R.id.tv_name)).setText(d.districtName);
                view.setOnClickListener(clk);
                view.setTag(d);
                ll_areas.addView(view);
            }

            ColorDrawable cd = new ColorDrawable(this.getResources().getColor(R.color.gray));
            areaPop = new PopupWindow(areaView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            areaPop.setOutsideTouchable(true);
            areaPop.setFocusable(false);
            areaPop.update();
            areaPop.setBackgroundDrawable(cd);
            areaPop.setTouchInterceptor( new View.OnTouchListener() {

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    if (arg1.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        dismissArea();
                        return false;
                    } else
                        return false;
                }
            });
        }
        showArea();
    }



}
