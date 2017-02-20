package com.zju.hzsz.fragment.river;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.activity.RiverPositionActivity;
import com.zju.hzsz.activity.SugOrComtActivity;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverDataRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.DipPxUtils;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.ObjUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class RiverInfoItem extends BaseRiverPagerItem {
	private BaseActivity.BooleanCallback careCkb = null;

	public RiverInfoItem(River river, BaseActivity context, BaseActivity.BooleanCallback careCkb) {
		super(river, context);
		this.careCkb = careCkb;
	}

	/*
	* 河道联系人 点击拨打电话
	* */
	private View.OnClickListener telclik = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				final String tel = v.getTag().toString().trim();
				if (tel.length() > 0) {
					Dialog dlg = context.createMessageDialog(context.getString(R.string.tips), StrUtils.renderText(context, R.string.fmt_make_call_query, tel), context.getString(R.string.call), context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tel));
							context.startActivity(intent);
						}
					}, null, null);
					dlg.show();
				}
			}
		}
	};

	private BaseActivity.LoginCallback cbkTosug = new BaseActivity.LoginCallback() {
		@Override
		public void loginCallback(boolean logined) {
			if (logined) {
				readyToSugOrCom(false);
			}
		}
	};
	private View.OnClickListener sugclik = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			context.setLoginCallback(cbkTosug);
			if (context.checkUserAndLogin("请到个人中心进行注册或登录，使用个人账号登录后再进行建议。")) {
				readyToSugOrCom(false);
			}
		}
	};

	private BaseActivity.LoginCallback cbkTocom = new BaseActivity.LoginCallback() {
		@Override
		public void loginCallback(boolean logined) {
			if (logined) {
				readyToSugOrCom(true);
			}
		}
	};

	private View.OnClickListener comclik = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			context.setLoginCallback(cbkTocom);
			if (context.checkUserAndLogin("请到个人中心进行注册或登录，使用个人账号登录后再进行投诉。")) {
				readyToSugOrCom(true);
			}
		}
	};

	private void readyToSugOrCom(final boolean isCom) {
		if (river.isPiecewise()) {
			String[] names = new String[river.townRiverChiefs.length];
			for (int i = 0; i < names.length; ++i) {
				names[i] = river.townRiverChiefs[i].townRiverName;
			}
			Dialog alertDialog = new AlertDialog.Builder(context).setTitle(R.string.river_select_segment).setItems(names, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startToWithRiverSegmtntIx(which, isCom);
				}
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).create();
			alertDialog.show();
		} else {
			startToWithRiverSegmtntIx(-1, isCom);
		}
	}

	private void startToWithRiverSegmtntIx(int ix, boolean isCom) {
		Intent intent = new Intent(context, SugOrComtActivity.class);
		intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(river));
		intent.putExtra(Tags.TAG_INDEX, ix);
		intent.putExtra(Tags.TAG_ABOOLEAN, isCom);
		context.startActivity(intent);
	}

	@Override
	public View getView() {
		if (view == null) {
			view = LinearLayout.inflate(context, R.layout.confs_river_info, null);

			view.findViewById(R.id.iv_complaint).setOnClickListener(comclik);
			view.findViewById(R.id.iv_suggestion).setOnClickListener(sugclik);

			view.findViewById(R.id.tv_goposition).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (river != null) {
						Intent intent = new Intent(context, RiverPositionActivity.class);
						intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(river));
						context.startActivity(intent);
					}
				}
			});

			initedView();
			loadData();
		}
		return view;
	}

	@Override
	public void loadData() {
		JSONObject p = new JSONObject();
		try {
			p.put("riverId", river.riverId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		setRefreshing(true);
		context.getRequestContext().add("oneriver_data_get", new Callback<RiverDataRes>() {

			@Override
			public void callback(RiverDataRes o) {
				if (o != null && o.isSuccess()) {
					ObjUtils.mergeObj(river, o.data);
					try {
						refreshView();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				setRefreshing(false);
			}
		}, RiverDataRes.class, p);
	}

	private void refreshView() {
		if (river != null) {
			ViewWarp warp = new ViewWarp(view, context);
			warp.setText(R.id.tv_river_name, river.riverName);
			warp.setText(R.id.tv_river_code, river.riverSerialNum);
			warp.setText(R.id.tv_river_start, river.startingPoint);
			warp.setText(R.id.tv_river_owner, river.districtName);
			warp.setText(R.id.tv_river_end, river.endingPoint);
			warp.setText(R.id.tv_river_level, ResUtils.getRiverSLittleLevel(river.riverLevel));
			warp.setText(R.id.tv_river_length, StrUtils.renderText(context, R.string.fmt_legnth_km, StrUtils.floatS2Str(river.riverLength)));
			warp.setText(R.id.tv_responsibility, river.responsibility);
			warp.setText(R.id.tv_river_target, river.target);

			warp.setImage(R.id.iv_love, river.isCared(context.getUser()) ? R.drawable.ic_loved : R.drawable.ic_love);

			View.OnClickListener clk = new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {

					river.toggleCare(context, new BaseActivity.BooleanCallback() {
						@Override
						public void callback(boolean b) {
							(new ViewWarp(view, context)).setImage(R.id.iv_love, river.isCared(context.getUser()) ? R.drawable.ic_loved : R.drawable.ic_love);
							if (careCkb != null) {
								careCkb.callback(river.isCared(context.getUser()));
							}
						}
					});

					/*
					 * context.showOperating();
					 * 
					 * context.getRequestContext().add(river.isCared(context.getUser
					 * ()) ? "careriver_action_delete" : "careriver_action_add",
					 * new Callback<SimpleRes>() {
					 * 
					 * @Override public void callback(SimpleRes o) { if (o !=
					 * null && o.isSuccess()) {
					 * river.setCared(context.getUser(),
					 * !river.isCared(context.getUser())); (new ViewWarp(view,
					 * context)).setImage(R.id.iv_love,
					 * river.isCared(context.getUser()) ? R.drawable.ic_loved :
					 * R.drawable.ic_love); } context.hideOperating(); } },
					 * SimpleRes.class, ParamUtils.freeParam(null, "riverId",
					 * river.riverId));
					 */
				}
			};

			warp.getViewById(R.id.iv_love).setOnClickListener(clk);
			warp.getViewById(R.id.tv_love).setOnClickListener(clk);

			String imgurl = StrUtils.getImgUrl(river.getImgUrl());
			ImgUtils.loadImage(context, ((ImageView) view.findViewById(R.id.iv_picture)), imgurl, R.drawable.im_riverbox, R.drawable.im_riverbox);

			final LinearLayout ll_contacts = (LinearLayout) warp.getViewById(R.id.ll_contacts);
			ll_contacts.removeAllViews();

			boolean isQ = river.riverLevel <= 4; // 区及其以上
			boolean isF = river.isPiecewise(); // 分段显示？

			if (isQ && isF) {
				View river_line = LinearLayout.inflate(context, R.layout.item_river_contact_line, null);
				((TextView) (river_line.findViewById(R.id.tv_title_name))).setText(R.string.river_quhezhang);
				river_line.findViewById(R.id.tv_river_name).setVisibility(View.GONE);
				ll_contacts.addView(river_line);
			}

			if (isQ) {
				// 区县

				// 区县河长 联系人
				LinearLayout row = new LinearLayout(context);
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.addView(initContItem(R.string.river_quhezhang, river.districtRiverChief.chiefName, null, false));
				row.addView(initContItem(R.string.river_quhezhang_cont, river.districtComtactPeo.chiefName, river.districtComtactPeo.contactWay, false));

				ll_contacts.addView(row);

				// 联系部门 联系人
				row = new LinearLayout(context);
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.addView(initContItem(R.string.river_contdep, river.comtactDepartment.department, null, false));
				row.addView(initContItem(R.string.river_contpe, river.comtactDepartment.river_contact_user, river.comtactDepartment.department_phone, false));

				ll_contacts.addView(row);

				if (isF) {
					// 河道警长 ---
					row = new LinearLayout(context);
					row.addView(initContItem(R.string.river_jingzhang, river.districtRiverSheriff != null ? river.districtRiverSheriff.chiefName : null, river.districtRiverSheriff != null ? river.districtRiverSheriff.contactWay : null, false));
					row.addView(initContItem(R.string.river_jingzhang, null, null, true));
					ll_contacts.addView(row);
				}

			} else {
				// 镇级
				LinearLayout row = new LinearLayout(context);
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.addView(initContItem(R.string.river_zhenhezhang, river.townRiverChiefs[0].chiefName, river.townRiverChiefs[0].contactWay, false));
				row.addView(initContItem(R.string.river_jingzhang, river.townRiverSheriffs.length > 0 ? river.townRiverSheriffs[0].chiefName : null, river.townRiverSheriffs.length > 0 ? river.townRiverSheriffs[0].contactWay : null, false));

				ll_contacts.addView(row);

				if (river.comtactDepartment != null) {
					row = new LinearLayout(context);
					row.setOrientation(LinearLayout.HORIZONTAL);
					row.addView(initContItem(R.string.river_contdep, river.comtactDepartment.department, null, false));
					row.addView(initContItem(R.string.river_contpe, river.comtactDepartment.river_contact_user, river.comtactDepartment.department_phone, false));
				}
				ll_contacts.addView(row);
			}
			if (isF) {
				// 分段
				// view.findViewById(R.id.iv_complaint).setVisibility(View.GONE);
				// view.findViewById(R.id.iv_suggestion).setVisibility(View.GONE);

				// TextView tv_segtip = (TextView) LinearLayout.inflate(context,
				// R.layout.tv_river_segment_tip, null);
				// tv_segtip.setText(StrUtils.renderText(context,
				// R.string.fmt_river_segment_tip,
				// river.townRiverChiefs.length));
				// ll_contacts.addView(tv_segtip);

			} else {
				// view.findViewById(R.id.iv_complaint).setVisibility(View.VISIBLE);
				// view.findViewById(R.id.iv_suggestion).setVisibility(View.VISIBLE);
			}

			for (int i = 0; i < river.townRiverChiefs.length && isQ; ++i) {
				if (isF) {
					View river_line = LinearLayout.inflate(context, R.layout.item_river_contact_line, null);
					((TextView) (river_line.findViewById(R.id.tv_river_name))).setText(river.townRiverChiefs[i].townRiverName);

					ll_contacts.addView(river_line);
				}
				LinearLayout row = new LinearLayout(context);
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.addView(initContItem(R.string.river_zhenhezhang, river.townRiverChiefs[i].chiefName, river.townRiverChiefs[i].contactWay, false));

				if (isF) {
					row.addView(initContItem(R.string.river_jingzhang, i < river.townRiverSheriffs.length ? river.townRiverSheriffs[i].chiefName : null, i < river.townRiverSheriffs.length ? river.townRiverSheriffs[i].contactWay : null, true));
				} else {
					row.addView(initContItem(R.string.river_jingzhang, river.districtRiverSheriff != null ? river.districtRiverSheriff.chiefName : null, river.districtRiverSheriff != null ? river.districtRiverSheriff.contactWay : null, false));
				}

				ll_contacts.addView(row);
			}
		}
	}

	private View initContItem(int titleid, String val, String tel, boolean canHide) {
		View view = LinearLayout.inflate(context, R.layout.item_river_contact_user, null);
		if ((val == null || val.length() == 0) && (tel != null && tel.length() > 0))
			val = "未指定";
		((TextView) view.findViewById(R.id.tv_user_title)).setText(titleid);
		((TextView) view.findViewById(R.id.tv_user_name)).setText(val);
		if (tel == null || tel.length() == 0)
			view.findViewById(R.id.iv_phone).setVisibility(View.GONE);
		if (canHide && (val == null || val.length() == 0))
			((TextView) view.findViewById(R.id.tv_user_title)).setVisibility(View.GONE);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
		int dp1px = DipPxUtils.dip2px(context, context.getResources().getDimension(R.dimen.linew));
		lp.setMargins(dp1px, dp1px, 0, 0);
		view.setLayoutParams(lp);
		view.setTag(tel);
		view.setOnClickListener(telclik);

		return view;
	}
}
