package com.zju.hzsz.npc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.activity.NewsDetailActivity;
import com.zju.hzsz.model.News;
import com.zju.hzsz.model.NewsDataRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 人大代表-规范法规页面
 * Created by Wangli on 2017/4/22.
 */

public class NpcLegalListActivity extends BaseActivity {

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;
    private List<News> list = new ArrayList<News>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npclegallist);

        setTitle("规范法规");
        initHead(R.drawable.ic_head_back, 0);

        final View.OnClickListener goNewsDetail = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NpcLegalListActivity.this, NewsDetailActivity.class);
                intent.putExtra(Tags.TAG_NEWS, StrUtils.Obj2Str(v.getTag()));
                startActivity(intent);

                //若已读过，则要变成灰色
                News news = (News) v.getTag();
                if (!news.isReaded(getUser())) {
                    news.setReaded(getUser());
                    adapter.notifyDataSetChanged();
                }
            }
        };

        if (listViewWarp == null) {
            adapter = new SimpleListAdapter(this, list, new SimpleViewInitor() {

                @Override
                public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
                    News n = (News) data;
                    if (convertView == null) {
                        convertView = LinearLayout.inflate(context, R.layout.item_news, null);
                    }
                    ((TextView) convertView.findViewById(R.id.tv_distname)).setText(n.rePeople);
                    ((TextView) convertView.findViewById(R.id.tv_news_title)).setText(n.theme);
                    ((TextView) convertView.findViewById(R.id.tv_news_article)).setText(n.abstarct == null ? "" : n.abstarct);
                    ImgUtils.loadImage(context, (ImageView) convertView.findViewById(R.id.iv_picture), StrUtils.getImgUrl(n.picPath));

                    int textcolor = getResources().getColor(n.isReaded(getUser()) ? R.color.lightgray : R.color.black);
                    ((TextView) convertView.findViewById(R.id.tv_news_title)).setTextColor(textcolor);
                    ((TextView) convertView.findViewById(R.id.tv_news_article)).setTextColor(textcolor);

                    convertView.setTag(n);
                    convertView.setOnClickListener(goNewsDetail);
                    return convertView;
                }
            });

            listViewWarp = new ListViewWarp(this, adapter, new ListViewWarp.WarpHandler() {
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

            listViewWarp.startRefresh();

        }

        ((LinearLayout) findViewById(R.id.ll_main)).addView(listViewWarp.getRootView());
        loadData(true);


    }

    private void loadData(final boolean refresh) {

        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);

        getRequestContext().add("info_list_get", new Callback<NewsDataRes>() {

            @Override
            public void callback(NewsDataRes o) {

                listViewWarp.setLoadingMore(false);
                listViewWarp.setRefreshing(false);

                if (o != null && o.isSuccess() && o.data != null) {
                    if (refresh)
                        list.clear();
                    for (News n : o.data.newsJsons) {
                        list.add(n);
                    }

                    adapter.notifyDataSetChanged();
                }

                if ((o != null && o.data != null && o.data.newsJsons != null) && (o.data.pageInfo != null && list.size() >= o.data.pageInfo.totalCounts || o.data.newsJsons.length == 0)) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, NewsDataRes.class, ParamUtils.freeParam(getPageParam(refresh), "newsType", 8));
    }

    //分页函数
    private final int DefaultPageSize = Constants.DefaultPageSize;

    protected JSONObject getPageParam(boolean refresh) {
        JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1) :
                ParamUtils.pageParam (DefaultPageSize, (list.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
        return j;
    }
}
