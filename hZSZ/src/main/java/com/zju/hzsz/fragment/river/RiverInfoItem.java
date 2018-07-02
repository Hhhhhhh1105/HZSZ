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
import com.zju.hzsz.activity.RiverActivity;
import com.zju.hzsz.activity.RiverPositionActivity;
import com.zju.hzsz.activity.SugOrComtActivity;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.LowLevelRiver;
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
			//如果tag不为空，则显示拨打电话的dialog，tag值为电话号码
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

	private View.OnClickListener lowLeverRiverClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				Intent intent = new Intent(context, RiverActivity.class);
				intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(v.getTag()));
				context.startActivity(intent);
			}
		}
	};

	/**
	 * 点击投诉或建议之后，弹出窗口
	 * @param isCom：建议为false，投诉为true
     */
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

	/**
	 * 跳转至投诉界面
	 * @param ix 第几条河
	 * @param isCom 建议or投诉
     */
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

			//如果是人大代表的河的话，就去掉“建议”和“投诉”这两个按钮,以使得这条河与普通河道区分开
			//判断条件：1.是人大代表 2.河道id一致
			if (context.getUser().isNpc() && river.riverId == context.getUser().riverSum[0].riverId) {
				view.findViewById(R.id.iv_suggestion).setVisibility(View.GONE);
				view.findViewById(R.id.iv_complaint).setVisibility(View.GONE);
			}

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

	/**
	 * 得到返回值执行refreshView更新UI
	 */
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
					ObjUtils.mergeObj(river, o.data);//magic-riverLevel自动会减一，好奇怪
					river.riverLevel = o.data.riverLevel;
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

	/**
	 * 接收到返回信息后刷新显示
	 */
	private void refreshView() {

		if (river != null) {
			ViewWarp warp = new ViewWarp(view, context);
			warp.setText(R.id.tv_river_name, river.riverName);
			warp.setText(R.id.tv_river_code, river.riverSerialNum);
			warp.setText(R.id.tv_river_start, river.startingPoint);
			warp.setText(R.id.tv_river_owner, river.districtName);
			warp.setText(R.id.tv_river_end, river.endingPoint);
			//根据riverLevel判断河道的等级：省级、区级、镇街级等
			warp.setText(R.id.tv_river_level, ResUtils.getRiverSLittleLevel(river.riverLevel));
			warp.setText(R.id.tv_river_length, StrUtils.renderText(context, R.string.fmt_legnth_km, StrUtils.floatS2Str(river.riverLength)));
			warp.setText(R.id.tv_responsibility, river.responsibility);
			warp.setText(R.id.tv_river_target, river.target);


			//河道别名
			if (river.riverAlias != null && !river.riverAlias.equals("")) {
				river.riverAlias = "（" + river.riverAlias + "）";
				warp.setText(R.id.river_alias, river.riverAlias);
				warp.getViewById(R.id.river_alias).setVisibility(View.VISIBLE);
			}

			//区县级河道去掉“投诉”和“公示”两个按钮
			if (river.riverLevel <= 3) {
				warp.getViewById(R.id.iv_complaint).setVisibility(View.INVISIBLE);
				warp.getViewById(R.id.iv_suggestion).setVisibility(View.INVISIBLE);
			}

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

			//iv和tv
			warp.getViewById(R.id.iv_love).setOnClickListener(clk);
			warp.getViewById(R.id.tv_love).setOnClickListener(clk);

			String imgurl = StrUtils.getImgUrl(river.getImgUrl());
			ImgUtils.loadImage(context, ((ImageView) view.findViewById(R.id.iv_picture)), imgurl, R.drawable.im_riverbox, R.drawable.im_riverbox);

			//联系人部分
			final LinearLayout ll_contacts = (LinearLayout) warp.getViewById(R.id.ll_contacts);
			ll_contacts.removeAllViews();

			//下级河道部分
			final LinearLayout ll_rivers = (LinearLayout) warp.getViewById(R.id.ll_rivers);
			ll_rivers.removeAllViews();

			//人大监督员部分
			final LinearLayout ll_npcs = (LinearLayout) warp.getViewById(R.id.ll_npcs);
			ll_npcs.removeAllViews();

			//市级河长或区级河长
			if (river.riverLevel <= 3){
				View river_line = LinearLayout.inflate(context, R.layout.item_river_contact_line, null);

				int title_name = 3;
				if (2 == river.riverLevel)
					title_name = R.string.city_river_chief_name;
				else if (1 == river.riverLevel)
					title_name = R.string.river_province_title;
				else
					title_name = R.string.river_district_title;

			/*	((TextView) (river_line.findViewById(R.id.tv_title_name))).setText(title_name);
				river_line.findViewById(R.id.tv_river_name).setVisibility(View.GONE);
				ll_contacts.addView(river_line);*/
				LinearLayout row = new LinearLayout(context);
				if(river.riverLevel == 2){//市级河道的电子公示牌单独设计，有正副河长
					//河长姓名+联系人
					row.setOrientation(LinearLayout.HORIZONTAL);
					row.addView(initContItem(R.string.city_river_chief_name, river.districtRiverChief.chiefName, river.districtRiverChief.contactWay, false));
					row.addView(initContItem(R.string.cityriverchief_responsibility, river.districtRiverChief.department, null, false));
					ll_contacts.addView(row);
					//副河长
					row = new LinearLayout(context);
					row.addView(initContItem(R.string.vicecity_river_chief_name, river.vicecityRiverChief.chiefName , river.districtRiverSheriff != null ? river.vicecityRiverChief.contactWay : null, false));
					row.addView(initContItem(R.string.cityriverchief_responsibility, river.vicecityRiverChief.department, null, false));
					ll_contacts.addView(row);

					//河长职务+河道警长
					row = new LinearLayout(context);
					row.addView(initContItem(R.string.river_jingzhang, river.districtRiverSheriff != null ? river.districtRiverSheriff.chiefName : null, river.districtRiverSheriff != null ? river.districtRiverSheriff.contactWay : null, false));
					ll_contacts.addView(row);

					//联系部门+联系人
					row = new LinearLayout(context);
					row.addView(initContItem(R.string.river_contdep, river.comtactDepartment.department, null, false));
//					row.addView(initContItem(R.string.river_contpe, river.comtactDepartment.river_contact_user, river.comtactDepartment.department_phone, false));
					row.addView(initContItem(R.string.river_quhezhang_cont, river.districtComtactPeo.chiefName, river.districtComtactPeo.contactWay, false));
					ll_contacts.addView(row);
				}else {
					//河长姓名+联系人
//					LinearLayout row = new LinearLayout(context);
					row.setOrientation(LinearLayout.HORIZONTAL);
					row.addView(initContItem(title_name, river.districtRiverChief.chiefName, river.districtRiverChief.contactWay, false));
//				row.addView(initContItem(R.string.river_quhezhang_cont, river.districtComtactPeo.chiefName, river.districtComtactPeo.contactWay, false));
					row.addView(initContItem(R.string.river_jingzhang, river.districtRiverSheriff != null ? river.districtRiverSheriff.chiefName : null, river.districtRiverSheriff != null ? river.districtRiverSheriff.contactWay : null, false));
					ll_contacts.addView(row);

					//河长职务+河道警长
					row = new LinearLayout(context);
					row.addView(initContItem(R.string.riverchief_responsibility, river.districtRiverChief.department, null, false));
					ll_contacts.addView(row);
//				row.addView(initContItem(R.string.river_jingzhang, river.districtRiverSheriff != null ? river.districtRiverSheriff.chiefName : null, river.districtRiverSheriff != null ? river.districtRiverSheriff.contactWay : null, false));

					//联系部门+联系人
					row = new LinearLayout(context);
					row.addView(initContItem(R.string.river_contdep, river.comtactDepartment.department, null, false));
					row.addView(initContItem(R.string.river_contpe, river.comtactDepartment.river_contact_user, river.comtactDepartment.department_phone, false));
					ll_contacts.addView(row);
				}

				//市级河道或区级河道有下级河道
				if (river.lowLevelRivers.length > 0) {
					for (int i = 0; i < river.lowLevelRivers.length; i += 2) {
						LowLevelRiver lowLevelRiver_l = river.lowLevelRivers[i];
						LowLevelRiver lowLevelRiver_r = (i + 1) < river.lowLevelRivers.length ? river.lowLevelRivers[i + 1] : null;

						View view = LinearLayout.inflate(context, R.layout.item_mainpage_section, null);
						((TextView) view.findViewById(R.id.tv_name_l)).setText(lowLevelRiver_l.riverName);
						((TextView) view.findViewById(R.id.tv_level_l)).setText(ResUtils.getRiverSLittleLevel(lowLevelRiver_l.riverLevel));
						((ImageView) view.findViewById(R.id.iv_quality_l)).setImageResource(R.drawable.arrow_right);

						view.findViewById(R.id.rl_section_left).setOnClickListener(lowLeverRiverClick);
						view.findViewById(R.id.rl_section_left).setTag(lowLevelRiver_l);

						if (lowLevelRiver_r != null) {

							((TextView) view.findViewById(R.id.tv_name_r)).setText(lowLevelRiver_r.riverName);
							((TextView) view.findViewById(R.id.tv_level_r)).setText(ResUtils.getRiverSLittleLevel(lowLevelRiver_r.riverLevel));
							((ImageView) view.findViewById(R.id.iv_quality_r)).setImageResource(R.drawable.arrow_right);

							view.findViewById(R.id.rl_section_right).setOnClickListener(lowLeverRiverClick);
							view.findViewById(R.id.rl_section_right).setTag(lowLevelRiver_r);

						} else {
							view.findViewById(R.id.rl_section_right).setVisibility(View.INVISIBLE);
						}

						ll_rivers.addView(view);

					}
				} else {
					//若无下级河道则不显示下级河道
					warp.getViewById(R.id.tv_low_level_river).setVisibility(View.GONE);
					//显示不分级的镇街河长及联系方式
					row = new LinearLayout(context);
					row.addView(initContItem(R.string.river_zhenhezhang, river.townRiverChiefs[0].chiefName, river.townRiverChiefs[0].contactWay, false));
					row.addView(initContItem(R.string.river_jingzhang, null, null, true));
					ll_contacts.addView(row);
				}
			}
			//镇街级河道
			if(river.riverLevel == 4) {

				//镇街河长+河道警长
				LinearLayout row = new LinearLayout(context);
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.addView(initContItem(R.string.river_zhenhezhang, river.townRiverChiefs[0].chiefName, river.townRiverChiefs[0].contactWay, false));
				row.addView(initContItem(R.string.river_jingzhang, river.townRiverSheriffs.length > 0 ? river.townRiverSheriffs[0].chiefName : null, river.townRiverSheriffs.length > 0 ? river.townRiverSheriffs[0].contactWay : null, false));
				ll_contacts.addView(row);

				//河长职务
				row = new LinearLayout(context);
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.addView(initContItem(R.string.riverchief_responsibility, river.townRiverChiefs[0].department, null, false));
				ll_contacts.addView(row);

				//联系部门 联系人
				if (river.comtactDepartment != null) {
					row = new LinearLayout(context);
					row.setOrientation(LinearLayout.HORIZONTAL);
					row.addView(initContItem(R.string.river_contdep, river.comtactDepartment.department, null, false));
					row.addView(initContItem(R.string.river_contpe, river.comtactDepartment.river_contact_user, river.comtactDepartment.department_phone, false));
				}
				ll_contacts.addView(row);

				warp.getViewById(R.id.tv_low_level_river).setVisibility(View.GONE);

				/*//镇街级河道有下属村级河长 分栏显示方式
				if (river.villageRiverChiefs.length > 0) {

					//下属村级河道
					warp.getViewById(R.id.tv_low_level_river).setVisibility(View.VISIBLE);
					((TextView) warp.getViewById(R.id.tv_low_level_river)).setText(R.string.river_villagechief);

					for (int i = 0; i < river.villageRiverChiefs.length; i += 2) {
						VillageRiverChief villageRiverChief_l = river.villageRiverChiefs[i];
						VillageRiverChief villageRiverChief_r = (i + 1) < river.villageRiverChiefs.length ? river.villageRiverChiefs[i + 1] : null;

						View view = LinearLayout.inflate(context, R.layout.item_mainpage_section, null);
						((TextView) view.findViewById(R.id.tv_name_l)).setText(villageRiverChief_l.chiefName);
						((TextView) view.findViewById(R.id.tv_level_l)).setText(villageRiverChief_l.position);
						((ImageView) view.findViewById(R.id.iv_quality_l)).setImageResource(R.drawable.ic_phone);

						view.findViewById(R.id.rl_section_left).setOnClickListener(telclik);
						view.findViewById(R.id.rl_section_left).setTag(villageRiverChief_l.contactWay);

						if (villageRiverChief_r!= null) {

							((TextView) view.findViewById(R.id.tv_name_r)).setText(villageRiverChief_r.chiefName);
							((TextView) view.findViewById(R.id.tv_level_r)).setText(villageRiverChief_r.position);
							((ImageView) view.findViewById(R.id.iv_quality_r)).setImageResource(R.drawable.ic_phone);
							((ImageView) view.findViewById(R.id.iv_quality_r)).setScaleType(ImageView.ScaleType.FIT_XY);

							view.findViewById(R.id.rl_section_right).setOnClickListener(telclik);
							view.findViewById(R.id.rl_section_right).setTag(villageRiverChief_r.contactWay);

						} else {
							view.findViewById(R.id.rl_section_right).setVisibility(View.INVISIBLE);
						}

						ll_rivers.addView(view);

					}
				} else {
					//若无下级河道则不显示下属村级河长列表
					warp.getViewById(R.id.tv_low_level_river).setVisibility(View.GONE);
				}
*/
				//镇街级河道有下属村级河长 按分段河长方式显示
				if (river.villageRiverChiefs.length > 0) {

					warp.getViewById(R.id.tv_low_level_river).setVisibility(View.VISIBLE);
					((TextView) warp.getViewById(R.id.tv_low_level_river)).setText(R.string.river_villagechief);

					for (int i = 0; i < river.villageRiverChiefs.length; i++) {

						LinearLayout row_village = new LinearLayout(context);
						row_village.setOrientation(LinearLayout.HORIZONTAL);

						row_village.addView(initContItem(R.string.river_villagechief_title, river.villageRiverChiefs[i].chiefName, river.villageRiverChiefs[i].contactWay, false));
						row_village.addView(initContItem(R.string.river_villagechief_position, river.villageRiverChiefs[i].position, null, false));

						ll_rivers.addView(row_village);
					}

				} else {
					warp.getViewById(R.id.tv_low_level_river).setVisibility(View.GONE);
				}

			}

			//增加统一监督电话
			View supervision_phone = LinearLayout.inflate(context, R.layout.item_river_contact_line, null);
			((TextView) (supervision_phone.findViewById(R.id.tv_title_name))).setText("统一监督电话");
			((TextView) (supervision_phone.findViewById(R.id.tv_river_name))).setText("18883869560");

			LinearLayout row_superPhone = new LinearLayout(context);
			row_superPhone.setOrientation(LinearLayout.HORIZONTAL);
			row_superPhone.addView(initContItem(R.string.river_supervisePhone, river.supervisePhone, null, false));

			ll_contacts.addView(row_superPhone);


		/*	LinearLayout row_npc = new LinearLayout(context);
			row_npc.setOrientation(LinearLayout.HORIZONTAL);
			if (context.getUser().isNpc())
				row_npc.addView(initContItem(R.string.river_npc, "监督员姓名", "123456789", false));
			else
				row_npc.addView(initContItem(R.string.river_npc, "监督员姓名", null, false));
			ll_contacts.addView(row_npc);*/

			//人大监督员 如果非人大代表，不能看到电话号码
			if (river.deputiesJsons.length > 0) {

				warp.getViewById(R.id.tv_npc).setVisibility(View.VISIBLE);

				for (int i = 0; i < river.deputiesJsons.length; i++) {

					LinearLayout row_npc = new LinearLayout(context);
					row_npc.setOrientation(LinearLayout.HORIZONTAL);
					if (context.getUser().isNpc()) {
						row_npc.addView(initContItem(R.string.river_npc_name, river.deputiesJsons[i].name, river.deputiesJsons[i].mobilephone, false));
					}
					else {
						row_npc.addView(initContItem(R.string.river_npc_name, river.deputiesJsons[i].name, null, false));
					}
					row_npc.addView(initContItem(R.string.river_npc_title, ResUtils.getNpcTitle(river.deputiesJsons[i].authority), null, false));
					ll_npcs.addView(row_npc);
				}

			} else {
				warp.getViewById(R.id.tv_npc).setVisibility(View.GONE);
			}



/*
			boolean isQ = river.riverLevel <= 3; // riverLevel 1省2市3区4镇
			boolean isF = river.isPiecewise(); // 分段显示？

			//区县河长还分段的话，显示“区县河长”，不显示河道名称
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

			for (int i = 0; i < river.townRiverChiefs.length && isQ; ++i) {
				//若分段，则设置分段，显示“分段河长 镇街河名”
				if (isF) {
					View river_line = LinearLayout.inflate(context, R.layout.item_river_contact_line, null);
					((TextView) (river_line.findViewById(R.id.tv_river_name))).setText(river.townRiverChiefs[i].townRiverName);

					ll_contacts.addView(river_line);
				}
				//row：一行要显示的信息view
				LinearLayout row = new LinearLayout(context);
				row.setOrientation(LinearLayout.HORIZONTAL);
				//显示“镇街河长 镇街河长名” false代表不能隐藏
				row.addView(initContItem(R.string.river_zhenhezhang, river.townRiverChiefs[i].chiefName, river.townRiverChiefs[i].contactWay, false));

				//如果是分段的话就显示当前段的警长信息，“河道警长 警长名”
				if (isF) {
					row.addView(initContItem(R.string.river_jingzhang, i < river.townRiverSheriffs.length ? river.townRiverSheriffs[i].chiefName : null, i < river.townRiverSheriffs.length ? river.townRiverSheriffs[i].contactWay : null, true));
				} else {
					row.addView(initContItem(R.string.river_jingzhang, river.districtRiverSheriff != null ? river.districtRiverSheriff.chiefName : null, river.districtRiverSheriff != null ? river.districtRiverSheriff.contactWay : null, false));
				}

				ll_contacts.addView(row);
			}*/


		}
	}

	/**
	 *
	 * @param titleid 河长职位等级
	 * @param val 河长姓名
	 * @param tel 河长电话
	 * @param canHide
     * @return
     */
	private View initContItem(int titleid, String val, String tel, boolean canHide) {
		View view = LinearLayout.inflate(context, R.layout.item_river_contact_user, null);
		if ((val == null || val.length() == 0) && (tel != null && tel.length() > 0))
			val = "未指定";
		((TextView) view.findViewById(R.id.tv_user_title)).setText(titleid);
		((TextView) view.findViewById(R.id.tv_user_name)).setText(val);
		//如果无电话号码，则不显示拨打电话的icon
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
