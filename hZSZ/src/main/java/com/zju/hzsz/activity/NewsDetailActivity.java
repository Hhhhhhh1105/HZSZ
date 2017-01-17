package com.zju.hzsz.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.webkit.WebView;

import com.sin.android.sinlibs.utils.AssetsUtils;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.News;
import com.zju.hzsz.model.NewsRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ObjUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.utils.ViewUtils;

public class NewsDetailActivity extends BaseActivity {
	private News news = null;
	private WebView wv_main = null;
	private SwipeRefreshLayout swipeRefreshLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newsdetail);
		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.news_detail);
		news = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_NEWS), News.class);
		if (news != null) {
			wv_main = (WebView) findViewById(R.id.wv_main);
			swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_main);
			ViewUtils.setSwipeRefreshLayoutColorScheme(swipeRefreshLayout);
			swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

				@Override
				public void onRefresh() {
					refreshNews();
				}
			});
			refreshNews();
		}
	}

	private void refreshNews() {
		if (news != null) {
			swipeRefreshLayout.setRefreshing(true);
			getRequestContext().add("info_content_get", new Callback<NewsRes>() {
				@Override
				public void callback(NewsRes o) {
					if (o != null && o.isSuccess()) {
						ObjUtils.mergeObj(news, o.data);
						String html = AssetsUtils.readAssetTxt(NewsDetailActivity.this, "newsdetail_tpl.html");
						html = html.replace("{{title}}", news.theme);

						String body = news.content;
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
						html = html.replace("{{creatorname}}", news.creatorname != null ? news.creatorname : "");
						html = html.replace("{{updatetime}}", news.update_time != null ? news.update_time.getUpdateYMDHM(NewsDetailActivity.this) : "");

						String img = StrUtils.getImgUrl(news.picPath);
						html = html.replace("{{image}}", img != null ? img : "");
						wv_main.getSettings().setDefaultTextEncodingName("UTF-8");
						wv_main.loadDataWithBaseURL(Constants.SerUrl, html, "text/html", "UTF-8", null);
					}
					swipeRefreshLayout.setRefreshing(false);
				}
			}, NewsRes.class, ParamUtils.freeParam(null, "id", news.id, "type", news.type));
		}
	}
}
