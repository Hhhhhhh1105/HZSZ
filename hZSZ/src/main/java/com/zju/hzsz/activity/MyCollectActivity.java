package com.zju.hzsz.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.River;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.StrUtils;

public class MyCollectActivity extends BaseActivity {

	private List<River> rivers = null;
	private SimpleListAdapter riversAdapter = null;
	private boolean ordering = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mycollect);

		initHead(R.drawable.ic_head_back, R.drawable.ic_head_order);
		setTitle(R.string.mycollect);

		rivers = getUser().getCollections();

		final View.OnClickListener goRiver = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MyCollectActivity.this, RiverActivity.class);
				intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(v.getTag()));
				startActivity(intent);
			}
		};

		final View.OnClickListener removeClk = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() instanceof River) {
					final River river = (River) v.getTag();

					river.toggleCare(MyCollectActivity.this, new BaseActivity.BooleanCallback() {
						@Override
						public void callback(boolean b) {
							riversAdapter.notifyDataSetChanged();
						}
					});

					/*
					 * showOperating();
					 * 
					 * getRequestContext().add("careriver_action_delete", new
					 * Callback<SimpleRes>() {
					 * 
					 * @Override public void callback(SimpleRes o) { if (o !=
					 * null && o.isSuccess()) { rivers.remove(river);
					 * riversAdapter.notifyDataSetChanged(); } hideOperating();
					 * } }, SimpleRes.class, ParamUtils.freeParam(null,
					 * "riverId", river.riverId));
					 */
				}
			}
		};

		final View.OnClickListener sortUpClk = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() instanceof Integer) {
					int ix = ((Integer) v.getTag()).intValue();
					if (ix > 0) {
						River r = rivers.get(ix);
						rivers.remove(ix);
						rivers.add(ix - 1, r);

						riversAdapter.notifyDataSetChanged();
					}
				}
			}
		};

		final View.OnClickListener sortDownClk = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() instanceof Integer) {
					int ix = ((Integer) v.getTag()).intValue();
					if (ix < (rivers.size() - 1)) {
						River r = rivers.get(ix);
						rivers.remove(ix);
						rivers.add(ix + 1, r);

						riversAdapter.notifyDataSetChanged();
					}
				}
			}
		};
		riversAdapter = new SimpleListAdapter(this, rivers, new SimpleViewInitor() {

			@Override
			public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
				if (convertView == null) {
					convertView = LinearLayout.inflate(context, R.layout.item_collect_river, null);
				}
				River river = (River) data;
				((TextView) convertView.findViewById(R.id.tv_name)).setText(river.riverName);
				// ((TextView)
				// convertView.findViewById(R.id.tv_number)).setText(StrUtils.renderText(MyCollectActivity.this,
				// R.string.fmt_river_numver, river.riverSerialNum));

				((TextView) convertView.findViewById(R.id.tv_number)).setText(river.districtName);

				String img = StrUtils.getImgUrl(river.getImgUrl());
				ImgUtils.loadImage(MyCollectActivity.this, ((ImageView) convertView.findViewById(R.id.iv_picture)), img);

				ImageButton btn = (ImageButton) convertView.findViewById(R.id.ib_remove);
				btn.setTag(river);
				btn.setOnClickListener(removeClk);

				convertView.setOnClickListener(goRiver);
				convertView.setTag(river);

				if (ordering) {
					// 排序
					convertView.findViewById(R.id.ll_sort_btns).setVisibility(View.VISIBLE);
					convertView.findViewById(R.id.ib_remove).setVisibility(View.GONE);

					View bup = convertView.findViewById(R.id.btn_sort_up);
					View bdown = convertView.findViewById(R.id.btn_sort_down);
					bup.setTag(Integer.valueOf(position));
					bdown.setTag(Integer.valueOf(position));

					bup.setOnClickListener(sortUpClk);
					bdown.setOnClickListener(sortDownClk);

					bup.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
					bdown.setVisibility(position < (rivers.size() - 1) ? View.VISIBLE : View.GONE);
				} else {
					convertView.findViewById(R.id.ll_sort_btns).setVisibility(View.GONE);
					convertView.findViewById(R.id.ib_remove).setVisibility(View.VISIBLE);
				}

				return convertView;
			}
		});

		((ListView) findViewById(R.id.lv_rivers)).setAdapter(riversAdapter);

		findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ordering = !ordering;
				riversAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (riversAdapter != null)
			riversAdapter.notifyDataSetChanged();
	}
}
