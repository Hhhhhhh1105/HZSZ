package com.zju.hzsz.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.District;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverDataRes;
import com.zju.hzsz.model.RiverListRes;
import com.zju.hzsz.model.RiverQuickSearchRes;
import com.zju.hzsz.model.RiverSearchDataRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.net.Constants;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.ObjUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

public class SearchRiverActivity extends BaseActivity implements OnCheckedChangeListener, OnEditorActionListener {
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
	private SimpleListAdapter dwAdapter = null;

	private List<River> rivers = new ArrayList<River>();
	private SimpleListAdapter riversAdapter = null;

	private ListViewWarp listViewWarp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_searchriver);
		setTitle(R.string.searchriver);
		initHead(R.drawable.ic_head_back, 0);

		final View.OnClickListener goRiver = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SearchRiverActivity.this, RiverActivity.class);
				intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(v.getTag()));
				startActivity(intent);
			}
		};

		final View.OnClickListener backRiver = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final River river = (River) v.getTag();
				showOperating("加载河道数据...");
				getRequestContext().add("oneriver_data_get", new Callback<RiverDataRes>() {

					@Override
					public void callback(RiverDataRes o) {
						hideOperating();
						if (o != null && o.isSuccess()) {
							ObjUtils.mergeObj(river, o.data);
							Intent intent = new Intent();
							intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(river));
							setResult(RESULT_OK, intent);
							finish();
						}
					}
				}, RiverDataRes.class, ParamUtils.freeParam(null, "riverId", river.riverId));
			}
		};

		findViewById(R.id.iv_search).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startSearch(true);
			}
		});

		final View.OnClickListener togFollow = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() instanceof River) {
					final River river = (River) v.getTag();

					river.toggleCare(SearchRiverActivity.this, new BaseActivity.BooleanCallback() {
						@Override
						public void callback(boolean b) {
							riversAdapter.notifyDataSetChanged();
						}
					});

					/*
					 * showOperating();
					 * 
					 * getRequestContext().add(river.isCared(getUser()) ?
					 * "careriver_action_delete" : "careriver_action_add", new
					 * Callback<SimpleRes>() {
					 * 
					 * @Override public void callback(SimpleRes o) { if (o !=
					 * null && o.isSuccess()) { // river.ifCare = !river.ifCare;
					 * river.setCared(getUser(), !river.isCared(getUser()));
					 * riversAdapter.notifyDataSetChanged(); } hideOperating();
					 * } }, SimpleRes.class, ParamUtils.freeParam(null,
					 * "riverId", river.riverId));
					 */
				}
			}
		};

		riversAdapter = new SimpleListAdapter(this, rivers, new SimpleViewInitor() {

			@Override
			public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
				if (convertView == null) {
					convertView = LinearLayout.inflate(context, R.layout.item_river, null);
				}
				River river = (River) data;
				((TextView) convertView.findViewById(R.id.tv_name)).setText(river.riverName);
				((TextView) convertView.findViewById(R.id.tv_level)).setText(ResUtils.getRiverSLittleLevel(river.riverLevel));
				String img = StrUtils.getImgUrl(river.getImgUrl());
				ImgUtils.loadImage(SearchRiverActivity.this, ((ImageView) convertView.findViewById(R.id.iv_picture)), img);

				if (isSelectRiver) {
					convertView.findViewById(R.id.btn_follow).setVisibility(View.GONE);
				}

				if (isAllRiver) {
					convertView.findViewById(R.id.btn_follow).setVisibility(View.GONE);
					convertView.findViewById(R.id.iv_quality).setVisibility(View.VISIBLE);
					((ImageView) convertView.findViewById(R.id.iv_quality)).setImageResource(ResUtils.getQuiltyImg(river.waterType));
				}

				Button btn = (Button) convertView.findViewById(R.id.btn_follow);
				btn.setText(river.isCared(getUser()) ? R.string.unfollow : R.string.follow);
				btn.setBackgroundResource(river.isCared(getUser()) ? R.drawable.btn_gray_white : R.drawable.btn_green_white);
				btn.setTag(river);
				btn.setOnClickListener(togFollow);

				if (curDw == null || curDw.district == null || curDw.district.districtId == 0) {
					((TextView) (convertView.findViewById(R.id.tv_distname))).setText(river.districtName);
					(convertView.findViewById(R.id.tv_distname)).setVisibility(View.VISIBLE);
				} else {
					(convertView.findViewById(R.id.tv_distname)).setVisibility(View.GONE);
				}

				convertView.setOnClickListener(isSelectRiver ? backRiver : goRiver);
				convertView.setTag(river);
				return convertView;
			}
		});

		listViewWarp = new ListViewWarp(this, riversAdapter, new ListViewWarp.WarpHandler() {

			@Override
			public boolean onRefresh() {
				return startSearch(true);
			}

			@Override
			public boolean onLoadMore() {
				return startSearch(false);
			}
		});

		((LinearLayout) findViewById(R.id.ll_contain)).addView(listViewWarp.getRootView());

		// ((ListView) findViewById(R.id.ll_rivers)).setAdapter(riversAdapter);
		((EditText) findViewById(R.id.et_keyword)).setOnEditorActionListener(this);

		dwAdapter = new SimpleListAdapter(SearchRiverActivity.this, dwItems, new SimpleViewInitor() {

			@Override
			public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
				if (convertView == null) {
					convertView = LinearLayout.inflate(context, R.layout.item_keyword, null);
				}
				DistrictWarper dw = (DistrictWarper) data;

				((CheckBox) convertView).setText(dw.district.districtName);

				((CheckBox) convertView).setChecked(dw.checked);
				((CheckBox) convertView).setTag(dw);
				((CheckBox) convertView).setOnCheckedChangeListener(SearchRiverActivity.this);
				return convertView;
			}
		});

		GridView gl = (GridView) findViewById(R.id.gv_areas);
		gl.setAdapter(dwAdapter);

		getRequestContext().add("riverquicksearch_data_get", new Callback<RiverQuickSearchRes>() {
			@Override
			public void callback(RiverQuickSearchRes o) {
				if (o != null && o.isSuccess()) {
					dwItems.clear();
					District ds = new District();
					ds.districtId = 0;
					ds.districtName = getString(R.string.unlimeited);
					curDw = new DistrictWarper(ds);
					dwItems.add(curDw);

					for (District d : o.data.districtLists) {
						DistrictWarper dw = new DistrictWarper(d);
						dwItems.add(dw);
						// if (curDw == null && d.districtName.contains("上城")) {
						// curDw = dw;
						// curDw.checked = true;
						// }
					}
					if (curDw != null)
						startSearch(true);
					// curDw = dwItems.get(0);
					// curDw.checked = true;
					dwAdapter.notifyDataSetInvalidated();
				}
			}
		}, RiverQuickSearchRes.class, ParamUtils.freeParam(null));

		// add in 1.2
		if (getIntent().getIntExtra(Tags.TAG_CODE, 0) == Tags.CODE_SELECTRIVER) {
			setTitle(getIntent().getStringExtra(Tags.TAG_TITLE));
			isSelectRiver = true;
		}

		if (getIntent().getIntExtra(Tags.TAG_CODE, 0) == Tags.CODE_ALLRIVER) {
			setTitle(getIntent().getStringExtra(Tags.TAG_TITLE));
			isAllRiver = true;
		}
	}

	private boolean isSelectRiver = false;
	private boolean isAllRiver = false;

	private DistrictWarper curDw = null;

	private String getKeyword() {
		return ((EditText) findViewById(R.id.et_keyword)).getText().toString();
	}

	private final int DefaultPageSize = Constants.DefaultPageSize;

	protected JSONObject getPageParam(boolean refresh) {
		return refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (rivers.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
	}

	private boolean startSearch(final boolean refresh) {
		JSONObject p = null;
		if (curDw == null || curDw.district == null || curDw.district.districtId == 0) {
			p = ParamUtils.freeParam(getPageParam(refresh), "searchContent", getKeyword());
		} else {
			p = ParamUtils.freeParam(getPageParam(refresh), "searchContent", getKeyword(), "searchDistrictId", curDw.district.districtId);
		}
		showOperating();

		if (refresh)
			listViewWarp.setRefreshing(true);
		else
			listViewWarp.setLoadingMore(true);

		getRequestContext().add("Get_NewRiverSearch_Data", new Callback<RiverSearchDataRes>() {
			@Override
			public void callback(RiverSearchDataRes o) {
				listViewWarp.setRefreshing(false);
				listViewWarp.setLoadingMore(false);

				if (o != null && o.isSuccess() && o.data != null && o.data.riverSums != null) {
					if (refresh)
						rivers.clear();
					for (River r : o.data.riverSums) {
						rivers.add(r);
					}

					riversAdapter.notifyDataSetChanged();
				}
				hideOperating();
				if (rivers.size() == 0) {
					showToast(R.string.no_searched_river);
				}
				if ((o != null && o.data != null && o.data.riverSums != null && o.data.pageInfo != null) && (rivers.size() >= o.data.pageInfo.totalCounts || o.data.riverSums.length == 0)) {
					listViewWarp.setNoMore(true);
				} else {
					listViewWarp.setNoMore(false);
				}
			}
		}, RiverSearchDataRes.class, p);
		return true;
	}

	@SuppressWarnings("unused")
	private void startSearchOld(final boolean refresh) {
		JSONObject p = null;
		if (curDw == null || curDw.district == null || curDw.district.districtId == 0) {
			p = ParamUtils.freeParam(null, "searchContent", getKeyword());
		} else {
			p = ParamUtils.freeParam(null, "searchContent", getKeyword(), "searchDistrictId", curDw.district.districtId);
		}
		showOperating();
		getRequestContext().add("riversearch_data_get", new Callback<RiverListRes>() {
			@Override
			public void callback(RiverListRes o) {
				if (o != null && o.isSuccess()) {
					if (refresh)
						rivers.clear();
					for (River r : o.data) {
						rivers.add(r);
					}

					riversAdapter.notifyDataSetChanged();
				}
				hideOperating();
				if (rivers.size() == 0) {
					showToast(R.string.no_searched_river);
				}
			}
		}, RiverListRes.class, p);
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if (arg1) {
			curDw = (DistrictWarper) arg0.getTag();
			for (DistrictWarper d : dwItems) {
				d.checked = false;
			}
			curDw.checked = true;
			dwAdapter.notifyDataSetChanged();

			startSearch(true);
		}
		// ((EditText) findViewById(R.id.et_keyword)).requestFocus();
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.et_keyword)).getApplicationWindowToken(), 0);
		}
	}

	@Override
	public boolean onEditorAction(TextView arg0, int actId, KeyEvent arg2) {
		if (actId == EditorInfo.IME_ACTION_SEARCH) {
			startSearch(true);
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (riversAdapter != null) {
			riversAdapter.notifyDataSetInvalidated();
			riversAdapter.notifyDataSetChanged();
		}
	}
}
