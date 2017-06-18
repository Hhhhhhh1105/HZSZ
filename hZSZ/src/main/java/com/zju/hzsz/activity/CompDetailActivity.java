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
	private TextView tv_eval_btn;
	private String request;
	private int isRead = 0;

	private boolean ifShowComment = false; //若有deputyId,则说明是从首页点击进入投诉

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_complaition);

		setTitle(R.string.complaintorder);
		initHead(R.drawable.ic_head_back, 0);

		findViewById(R.id.ll_result).setVisibility(View.GONE);
		findViewById(R.id.ll_evalinfo).setVisibility(View.GONE);

		tv_eval_btn = (TextView) findViewById(R.id.tv_eval);
		tv_eval_btn.setVisibility(View.GONE);

		comp = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_COMP), CompSugs.class);
		readOnly = getIntent().getBooleanExtra(Tags.TAG_ABOOLEAN, false);//ture,只读
		ifShowComment = getIntent().getIntExtra("deputyId", 0) == 0 ;

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

		if (comp.isComp()) {

			if (comp.compPersonId > 0) {
				//查看“最新投诉”的投诉单详情
				request = "Get_ChiefComplain_Content";
				params = ParamUtils.freeParam(null, "complianId" , comp.getId());
			} else {
				//人大/河长查看自身的投诉单详情
				request =  "complaintscontent_data_get";
				params = ParamUtils.freeParam(null, "complaintsId" , comp.getId());

				//status 3:已处理 6：追加处理评价  7：已评价，满意
				//人大评价部分的显示 - 得确定是从人大的个人中心进入 - 用deputyId来控制
				//即首页点击进入时传入deputyId,个人中心不传，默认为0，用ifShowComment boolean值表明是否显示
				if (getUser().isNpc() && ifShowComment) {

					if (comp.getStatus() == 1) {
						// 待受理
					} else if (comp.getStatus() == 2) {
						// 已受理
					} else if (comp.getStatus() == 3 || comp.getStatus() == 6) {
						// 已处理, 或者已经追加处理
						tv_eval_btn.setVisibility(View.VISIBLE);
						tv_eval_btn.setText("马上评价>>");
					} else if (comp.getStatus() >= 4) {
						//查看评价
						tv_eval_btn.setVisibility(View.VISIBLE);
						tv_eval_btn.setText("查看评价>>");
					}

					tv_eval_btn.setTag(comp);
					tv_eval_btn.setOnClickListener(btnClk);


					//已阅未阅
					findViewById(R.id.ll_isread).setVisibility(View.VISIBLE);
					((ImageView) findViewById(R.id.iv_status)).setImageResource(comp.isRead == 1 ? R.drawable.im_cp_handled : R.drawable.im_cp_unhandle);
					((TextView) findViewById(R.id.tv_isread)).setTextColor(getResources().getColor(comp.isRead == 1 ? R.color.green : R.color.red));
					((TextView) findViewById(R.id.tv_isread)).setText(comp.isRead == 1 ? R.string.sup_isread : R.string.sup_notread);

				}
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

		if (!ifShowComment && getUser().isNpc()) {
			// 获取评价  --- 这里人大的评价和普通评价分开写了
			//不展示“查看评价”，“马上评价的话”的话
			getRequestContext().add("complaintseval_action_get", new Callback<EvalRes>() {
				@Override
				public void callback(EvalRes o) {
					if (o != null && o.isSuccess()) {
						//为了评价情况正确显示
						if (!(comp.compPersonId > 0)) {
							findViewById(R.id.ll_evalinfo).setVisibility(View.VISIBLE);
							render.renderView(findViewById(R.id.ll_evalinfo), o.data);
						}
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

		//如果有评价的话
		if (compFul.evelLevel > -1) {
			findViewById(R.id.ll_evalinfo).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.tv_eval_evallevel)).setText(compFul.getEvelLevels());
			((TextView) findViewById(R.id.tv_eval_evalremark)).setText(compFul.evelContent);
		} else {
			findViewById(R.id.ll_evalinfo).setVisibility(View.GONE);
		}

		//人大不显示评价模块
		if (getUser().isNpc() && !request.equals("Get_ChiefComplain_Content")) {
			findViewById(R.id.ll_evalinfo).setVisibility(View.GONE);
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

	private View.OnClickListener btnClk = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof CompSugs) {
				CompSugs comp = (CompSugs) v.getTag();
				Intent intent = new Intent(CompDetailActivity.this, EvalCompActivity.class);
				intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
				startActivityForResult(intent, Tags.CODE_COMP);
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Tags.CODE_COMP && resultCode == RESULT_OK) {
			// 重新加载
			loadData();
		}
	}
}
