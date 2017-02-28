package com.zju.hzsz.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.District;
import com.zju.hzsz.model.News;
import com.zju.hzsz.model.NewsSearchDataRes;
import com.zju.hzsz.model.RiverQuickSearchRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangli on 2017/2/27.
 */

public class SearchNewsActivity extends BaseActivity implements CheckBox.OnCheckedChangeListener, TextView.OnEditorActionListener {




    class DistrictWarper {
        public District district;
        public boolean checked;

        public DistrictWarper(District district) {
            super();
            this.district = district;
            this.checked = false;
        }
    }

    private List<DistrictWarper> dwItems = new ArrayList<DistrictWarper>();
    private SimpleListAdapter dwAdpter = null;

    private List<News> newses = new ArrayList<News>();
    private SimpleListAdapter newsAdapter = null;

    private ListViewWarp listViewWarp = null;
    private DistrictWarper curDw = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_searchnews);

        setTitle("搜索新闻");
        initHead(R.drawable.ic_head_back, 0);

        final View.OnClickListener goNewsDetail = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchNewsActivity.this, NewsDetailActivity.class);
                intent.putExtra(Tags.TAG_NEWS, StrUtils.Obj2Str(v.getTag()));
                startActivity(intent);

                //若已读过，则要变成灰色
                News news = (News) v.getTag();
                if (!news.isReaded(getUser())) {
                    news.setReaded(getUser());
                    newsAdapter.notifyDataSetChanged();
                }
            }
        };

        findViewById(R.id.iv_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch(true);
            }
        });

        newsAdapter = new SimpleListAdapter(this, newses, new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

                if (convertView == null){
                    convertView = LinearLayout.inflate(context, R.layout.item_news, null);
                }

                News n = (News) data;

                ((TextView) convertView.findViewById(R.id.tv_distname)).setText(n.rePeople);
                ((TextView) convertView.findViewById(R.id.tv_news_title)).setText(n.theme);
                ((TextView) convertView.findViewById(R.id.tv_news_article)).setText(n.abstarct == null ? "" : n.abstarct);
                ImgUtils.loadImage(context, (ImageView) convertView.findViewById(R.id.iv_picture), StrUtils.getImgUrl(n.picPath));

                int textcolor = getResources().getColor(n.isReaded(getUser()) ? R.color.lightgray : R.color.black);
                ((TextView) convertView.findViewById(R.id.tv_news_title)).setTextColor(textcolor);
                ((TextView) convertView.findViewById(R.id.tv_news_article)).setTextColor(textcolor);

                convertView.setOnClickListener(goNewsDetail);
                convertView.setTag(n);
                return convertView;
            }
        });

        listViewWarp = new ListViewWarp(this, newsAdapter, new ListViewWarp.WarpHandler() {
            @Override
            public boolean onRefresh() {
                return startSearch(true);
            }

            @Override
            public boolean onLoadMore() {
                return startSearch(false);
            }
        });

        ((LinearLayout)findViewById(R.id.ll_contain)).addView(listViewWarp.getRootView());

        ((EditText)findViewById(R.id.et_keyword)).setOnEditorActionListener(this);

        dwAdpter = new SimpleListAdapter(SearchNewsActivity.this, dwItems, new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

                if (convertView == null){
                    convertView = LinearLayout.inflate(context, R.layout.item_keyword, null);
                }

                DistrictWarper dw = (DistrictWarper) data;

                ((CheckBox) convertView).setText(dw.district.districtName);
                ((CheckBox) convertView).setChecked(dw.checked);
                ((CheckBox) convertView).setTag(dw);
                ((CheckBox) convertView).setOnCheckedChangeListener(SearchNewsActivity.this);

                return convertView;
            }
        });

        GridView gv = (GridView) findViewById(R.id.gv_areas);
        gv.setAdapter(dwAdpter);

        getRequestContext().add("riverquicksearch_data_get", new Callback<RiverQuickSearchRes>() {
            @Override
            public void callback(RiverQuickSearchRes o) {
                if (o != null && o.isSuccess()){
                    dwItems.clear();
                    District ds = new District();
                    ds.districtId = 0;
                    ds.districtName = getString(R.string.unlimeited);
                    curDw = new DistrictWarper(ds);
                    dwItems.add(curDw);

                    for (District d : o.data.districtLists){
                        DistrictWarper dw = new DistrictWarper(d);
                        dwItems.add(dw);
                    }

                    //此处可加开始搜索
                    startSearch(true);

                    dwAdpter.notifyDataSetInvalidated();
                }
            }
        }, RiverQuickSearchRes.class, ParamUtils.freeParam(null));

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked){
            curDw = (DistrictWarper) buttonView.getTag();
            for (DistrictWarper d : dwItems){
                d.checked = false;
            }
            curDw.checked = true;
            dwAdpter.notifyDataSetChanged();

            //可在此添加开始搜索的代码：startSearch(true);
        }

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.et_keyword)).getApplicationWindowToken(), 0);
        }

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH){

            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.et_keyword)).getApplicationWindowToken(), 0);
            }

            startSearch(true);
        }
        return false;
    }

    private String getKeyword(){
        return ((EditText) findViewById(R.id.et_keyword)).getText().toString();
    }

    private final int DefaultPageSize = 10; //默认每次加载的条数

    /**
     * 获取分页jsonObeject
     * @param refresh
     * @return
     */
    protected JSONObject getPageParam(boolean refresh){
        return refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize,
                (newses.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
    }

    private boolean startSearch(final boolean refresh){
        JSONObject p = null;
        if (curDw == null || curDw.district == null || curDw.district.districtId == 0){
            p = ParamUtils.freeParam(getPageParam(refresh), "searchContent", getKeyword());
        }else {
            p = ParamUtils.freeParam(getPageParam(refresh), "searchContent", getKeyword(),
                    "districtId", curDw.district.districtId);
        }

        showOperating();

        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);

        getRequestContext().add("Get_SearchInfo_List", new Callback<NewsSearchDataRes>() {
            @Override
            public void callback(NewsSearchDataRes o) {

                listViewWarp.setRefreshing(false);
                listViewWarp.setLoadingMore(false);

                if (o != null && o.isSuccess() && o.data.newsJsons != null){
                    if (refresh)
                        newses.clear();
                    for (News n : o.data.newsJsons){
                        newses.add(n);
                    }

                    newsAdapter.notifyDataSetChanged();
                }

                hideOperating();

                if (newses.size() == 0){
                    showToast("没有找到相关新闻");
                }

                if ((o != null && o.data != null && o.data.newsJsons != null && o.data.pageInfo != null) && (newses.size() >= o.data.pageInfo.totalCounts || o.data.newsJsons.length == 0)) {
                    listViewWarp.setNoMore(true);
                }else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, NewsSearchDataRes.class, p);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (newsAdapter != null){
            newsAdapter.notifyDataSetChanged();
            newsAdapter.notifyDataSetInvalidated();
        }
    }
}
