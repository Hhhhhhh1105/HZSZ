package com.zju.hzsz.chief.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.Mail;
import com.zju.hzsz.model.MailsDataRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

public class ChiefMailListActivity extends BaseActivity {
	private final int DefaultPageSize = Constants.DefaultPageSize;
	protected List<Mail> datas = new ArrayList<Mail>();

	private SimpleListAdapter adapter = null;
	private ListViewWarp listViewWarp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_maillist);
		initHead(R.drawable.ic_head_back, 0);
		setTitle("我的信箱");

		adapter = new SimpleListAdapter(this, datas, new SimpleViewInitor() {

			@Override
			public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
				if (convertView == null) {
					convertView = LinearLayout.inflate(context, R.layout.item_chief_mail, null);
				}
				getViewRender().renderView(convertView, data);
				Mail mail = (Mail) data;
				((TextView) convertView.findViewById(R.id.tv_theme)).setTextColor(getResources().getColor(mail.isReaded() ? R.color.lightgray : R.color.black));

				return convertView;
			}
		});

		listViewWarp = new ListViewWarp(this, adapter, new ListViewWarp.WarpHandler() {

			@Override
			public boolean onRefresh() {
				return loadMailList(true);
			}

			@Override
			public boolean onLoadMore() {
				return loadMailList(false);
			}
		});

		((LinearLayout) findViewById(R.id.ll_main)).addView(listViewWarp.getRootView());
		listViewWarp.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if (pos < datas.size()) {
					Mail m = datas.get(pos);
					Intent intent = new Intent(ChiefMailListActivity.this, ChiefMailDetailActivity.class);
					intent.putExtra(Tags.TAG_MAIL, StrUtils.Obj2Str(m));
					startActivityForResult(intent, Tags.CODE_MAIL);
				}
			}
		});
		loadMailList(true);
	}

	protected JSONObject getPageParam(boolean refresh) {
		return refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (datas.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
	}

	private boolean loadMailList(final boolean refresh) {
		if (refresh)
			listViewWarp.setRefreshing(true);
		else
			listViewWarp.setLoadingMore(true);
		getRequestContext().add("Get_Mail_List", new Callback<MailsDataRes>() {
			@Override
			public void callback(MailsDataRes o) {
				listViewWarp.setRefreshing(false);
				listViewWarp.setLoadingMore(false);
				if (o != null && o.isSuccess() && o.data != null && o.data.mailLsit != null) {
					if (refresh)
						datas.clear();
					for (Mail m : o.data.mailLsit) {
						datas.add(m);
					}
					adapter.notifyDataSetChanged();
				}

				if ((o != null && o.data != null && o.data.mailLsit != null) && (o.data.pageInfo == null || datas.size() >= o.data.pageInfo.totalCounts || o.data.mailLsit.length == 0)) {
					listViewWarp.setNoMore(true);
				}
			}
		}, MailsDataRes.class, ParamUtils.freeParam(getPageParam(refresh)));
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Tags.CODE_MAIL && resultCode == RESULT_OK) {
			loadMailList(true);
		}
	}
}
