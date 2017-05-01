package com.zju.hzsz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.CompDetailRes;
import com.zju.hzsz.model.CompFul;
import com.zju.hzsz.model.CompSugs;
import com.zju.hzsz.model.EvalRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CompDetailActivity extends BaseActivity {
	private CompSugs comp = null;
	private CompFul compFul = null;
	private boolean readOnly = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_complaition);

		setTitle(R.string.complaintorder);
		initHead(R.drawable.ic_head_back, 0);

		findViewById(R.id.ll_result).setVisibility(View.GONE);
		findViewById(R.id.ll_evalinfo).setVisibility(View.GONE);

		comp = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_COMP), CompSugs.class);
		readOnly = getIntent().getBooleanExtra(Tags.TAG_ABOOLEAN, false);//ture,只读

		if (comp != null) {
			if (!comp.isComp()) {
				//在comp中就可以判断其是投诉还是建议
				// 建议
				changeToSugs((ViewGroup) findViewById(R.id.ll_root));
			}
			initPhotoView(comp.getPicPath());//已经无法访问
			loadData();
		} else {
			showToast("没有处理单信息");
			finish();
		}
	}

	private String[] imgUrls = null;

	//点击图片之后跳转到PhotoViewActivity.class
	private View.OnClickListener clkGotoPhotoView = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof Integer) {
				Intent intent = new Intent(CompDetailActivity.this, PhotoViewActivity.class);
				intent.putExtra("URLS", imgUrls);
				intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
				startActivity(intent);
			}
		}

	};

	private void initPhotoView(String urls) {
		List<String> list = new ArrayList<String>();
		if (urls != null) {
			for (String url : urls.split(";")) {
				String s = url.trim();
				if (s.length() > 0) {
					list.add(StrUtils.getImgUrl(s));
				}
			}
		}
		if (list.size() > 0) {
			imgUrls = new String[list.size()];
			imgUrls = list.toArray(imgUrls);
			findViewById(R.id.hsv_photos).setVisibility(View.VISIBLE);
			LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_photos);
			ll_photos.removeAllViews();

			for (int i = 0; i < imgUrls.length; ++i) {
				String url = imgUrls[i];
				View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
				ImgUtils.loadImage(CompDetailActivity.this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
				view.setTag(i);
				view.setOnClickListener(clkGotoPhotoView);
				ll_photos.addView(view);
			}

		} else {
			findViewById(R.id.hsv_photos).setVisibility(View.GONE);
		}
	}

	private void changeToSugs(ViewGroup vg) {
		int count = vg.getChildCount();
		for (int i = 0; i < count; ++i) {
			View v = vg.getChildAt(i);
			if (v instanceof ViewGroup) {
				changeToSugs((ViewGroup) v);
			} else if (v instanceof TextView) {
				TextView tv = (TextView) v;
				String s = tv.getText().toString();
				tv.setText(s.replace("投诉", "建议"));
			}
		}
	}

	private void loadData() {
		showOperating();

		JSONObject params;
		String request;

		if (comp.isComp()) {
			//人大/河长查看自身的投诉单详情
			request =  "complaintscontent_data_get";
			params = ParamUtils.freeParam(null, "complaintsId" , comp.getId());
			if (comp.compPersonId > 0) {
				//人大/河长查看最新投诉的投诉单详情
				request = "Get_ChiefComplain_Content";
				params = ParamUtils.freeParam(null, "complianId" , comp.getId());
			}

		} else {
			request = "advicecontent_data_get";
			params = ParamUtils.freeParam(null, "adviceId", comp.getId());
		}

		getRequestContext().add(request, new Callback<CompDetailRes>() {
			@Override
			public void callback(CompDetailRes o) {
				hideOperating();
				if (o != null && o.isSuccess() && o.data != null) {
					compFul = o.data;
					compFul.isComp = comp.isComp();
					initResultPhotoView(compFul.dealPicPath);
					refreshView();
					initPhotoView(compFul.getPicPath());
				}
			}
		}, CompDetailRes.class, params);

		if (comp.getStatus() >= 3) {
			// 获取评价
			getRequestContext().add(comp.isComp() ? "complaintseval_action_get" : "adviceeval_action_get", new Callback<EvalRes>() {
				@Override
				public void callback(EvalRes o) {
					if (o != null && o.isSuccess()) {
						findViewById(R.id.ll_evalinfo).setVisibility(View.VISIBLE);
						render.renderView(findViewById(R.id.ll_evalinfo), o.data);
					}
				}
			}, EvalRes.class, params);
		}
	}

	private void refreshView() {
		ViewWarp warp = new ViewWarp(findViewById(R.id.sv_main), this);

		warp.getViewById(R.id.ll_compinfo).setVisibility(View.VISIBLE);

		warp.setText(R.id.tv_sernum, compFul.getSerNum());
		warp.setText(R.id.tv_datetime, compFul.getTime() != null ? compFul.getTime().getYMDHM(this) : "");
		warp.setText(R.id.tv_user_name, compFul.ifAnonymous != 0 && readOnly ? "***" : compFul.getPersonName());
		warp.setText(R.id.tv_user_telno, compFul.ifAnonymous != 0 && readOnly ? "*****" : compFul.getTeleNum());
		warp.setText(R.id.tv_rivername, compFul.getRiverName());
		warp.setText(R.id.tv_comp_theme, compFul.getTheme());

		String s = compFul.getContent();
		warp.setText(R.id.tv_comp_content, s);

		warp.getViewById(R.id.ll_status).setVisibility(View.VISIBLE);
		warp.setText(R.id.tv_handlestatus, ResUtils.getHandleStatus(comp.getStatus()));

		if (comp.getStatus() >= 3) {
			// 已处理
			warp.getViewById(R.id.ll_result).setVisibility(View.VISIBLE);
			warp.setText(R.id.tv_hnd_name, compFul.dealPersonName);
			warp.setText(R.id.tv_hnd_telno, compFul.dealTeleNum);
			warp.setText(R.id.tv_hnd_datetime, compFul.dealTime != null ? compFul.dealTime.getYMDHM(this) : "");
			warp.setText(R.id.tv_hnd_result, compFul.getDealResultText());
		} else {
			warp.getViewById(R.id.ll_result).setVisibility(View.GONE);
		}

		if (compFul.evelContent != null && compFul.evelContent.length() > 0) {
			findViewById(R.id.ll_evalinfo).setVisibility(View.VISIBLE);
			render.renderView(findViewById(R.id.ll_evalinfo), compFul);
		}
	}

	private ViewRender render = new ViewRender();

	String[] imgResultUrls = null;
	private View.OnClickListener clkResultGotoPhotoView = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof Integer) {
				Intent intent = new Intent(CompDetailActivity.this, PhotoViewActivity.class);
				intent.putExtra("URLS", imgResultUrls);
				intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
				startActivity(intent);
			}
		}
	};

	private void initResultPhotoView(String urls) {
		List<String> list = new ArrayList<String>();
		if (urls != null) {
			for (String url : urls.split(";")) {
				String s = url.trim();
				if (s.length() > 0) {
					list.add(StrUtils.getImgUrl(s));
				}
			}
		}
		if (list.size() > 0) {
			imgResultUrls = new String[list.size()];
			imgResultUrls = list.toArray(imgResultUrls);
			findViewById(R.id.hsv_result_photos).setVisibility(View.VISIBLE);
			LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_result_photos);
			ll_photos.removeAllViews();

			for (int i = 0; i < imgResultUrls.length; ++i) {
				String url = imgResultUrls[i];
				View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
				ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
				view.setTag(i);
				view.setOnClickListener(clkResultGotoPhotoView);
				ll_photos.addView(view);
			}

		} else {
			findViewById(R.id.hsv_result_photos).setVisibility(View.GONE);
		}
	}
}
