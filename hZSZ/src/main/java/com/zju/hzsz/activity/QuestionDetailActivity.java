package com.zju.hzsz.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.webkit.WebView;

import com.sin.android.sinlibs.utils.AssetsUtils;
import com.zju.hzsz.R;
import com.zju.hzsz.model.Question;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.utils.ViewUtils;

/**
 * 常见问题详情页
 * Created by Wangli on 2017/6/20.
 */

public class QuestionDetailActivity extends BaseActivity {

    private Question question = null;
    private WebView wv_main = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_newsdetail);
        initHead(R.drawable.ic_head_back, 0);
        setTitle("常见问题详情");

        question = StrUtils.Str2Obj(getIntent().getStringExtra("question"), Question.class);
        if (question != null) {
            wv_main = (WebView) findViewById(R.id.wv_main);
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_main);
            ViewUtils.setSwipeRefreshLayoutColorScheme(swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {
                    refreshQuestion();
                }
            });
            refreshQuestion();
        }
    }

    private void refreshQuestion() {
        if (question != null) {
                swipeRefreshLayout.setRefreshing(true);

                String html = AssetsUtils.readAssetTxt(QuestionDetailActivity.this, "newsdetail_tpl.html");
                html = html.replace("{{title}}", question.title);

                String body = question.androidContent;
                if (!body.contains("<") || !body.contains(">")) {
                    // not html
                    StringBuffer sb = new StringBuffer();
                    for (String s : body.split("\n")) {
                        sb.append("<p>");
                        sb.append(s);
                        sb.append("</p>");
                    }
                    body = sb.toString();
                }
                html = html.replace("{{body}}", body);
                html = html.replace("{{creatorname}}", "");
                html = html.replace("{{updatetime}}", "");


                wv_main.getSettings().setDefaultTextEncodingName("UTF-8");
                wv_main.loadDataWithBaseURL(Constants.SerUrl, html, "text/html", "UTF-8", null);
            }
        swipeRefreshLayout.setRefreshing(false);
    }
}
