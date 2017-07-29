package com.zju.hzsz.chief.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.sin.android.sinlibs.base.Callable;
import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.sin.android.sinlibs.utils.InjectUtils;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.activity.PhotoViewActivity;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.model.DateTime;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverRecord;
import com.zju.hzsz.model.RiverRecordRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.service.LocalService;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ChiefEditRecordActivity extends BaseActivity {
	//巡检情况处的布局
	private LinearLayout ll_cboxes = null;
	//处理情况
	private EditText et_deal = null;
	//其他问题
	private EditText et_otherquestion = null;
	private Button btn_track = null;
	private Button btn_trackView = null;
	//RiverRecord:巡河相关类
	private RiverRecord riverRecord = null;
	private ViewRender viewRender = new ViewRender();
	private Location location = null;

	private String picPath; //图片链接

	private boolean isReadOnly = false;

	private String latList;
	private String lngList;

	private String latlist_temp;//当前巡河数据
	private String lnglist_temp;

	private int deputyId = 0;

	private boolean hasImg = false;

	private String startTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_editrecord);

		initHead(R.drawable.ic_head_back, 0);

		InjectUtils.injectViews(this, R.id.class);

		if (location != null) {
			latlist_temp = "" + location.getLatitude();
			latlist_temp = "" + location.getLongitude();
		}
		handler.postDelayed(new MyRunable(), 10000); //每10s记录一次当前轨迹
		initLocation();

		findViewById(R.id.btn_selriver).setOnClickListener(this);//选择河道按钮
		findViewById(R.id.btn_submit).setOnClickListener(this);//保存按钮
		findViewById(R.id.btn_cancel).setOnClickListener(this);//取消按钮


		btn_track = (Button) findViewById(R.id.btn_track);//查看轨迹按钮-巡河界面
		btn_track.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChiefEditRecordActivity.this, ChiefInspectActivity.class);
				intent.putExtra("latlist_temp", latlist_temp);//将当前的巡河数据传至巡河地图界面
				intent.putExtra("lnglist_temp", lnglist_temp);
				startActivityForResult(intent, Tags.CODE_LATLNG);
			}
		});
		btn_trackView = (Button) findViewById(R.id.btn_trackView);//查看轨迹按钮-查看巡河单界面
		btn_trackView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChiefEditRecordActivity.this, ChiefTrackViewActivity.class);
				intent.putExtra("latList", latList);
				intent.putExtra("lngList", lngList);
				startActivity(intent);
			}
		});


		riverRecord = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_RECORD), RiverRecord.class);
		isReadOnly = getIntent().getBooleanExtra(Tags.TAG_ABOOLEAN, false);
		//如果是从首页进入的，则其不为0；若是从个人中心进入的，则其为0. --> 人大
		deputyId = getIntent().getIntExtra("deputyId", 0);

		if (riverRecord == null) {//从新建中进入
			setTitle("新建巡查记录");
			riverRecord = new RiverRecord();
			riverRecord.recordDate = DateTime.getNow(); //巡河时间
			riverRecord.locRiverName = "选择河道";
			viewRender.renderView(findViewById(R.id.sv_main), riverRecord);

			//巡河开始时间
			startTime = DateTime.getNow().getYMDHMS(this);

			//退出巡河时的提醒
			findViewById(R.id.iv_head_left).setOnClickListener(exitTrackRiver);

			if (!getUser().isNpc())
				refreshToView();
			else
				refreshToNpcView();

			//检查是否正确退出了巡河,弹出窗口询问其是否继续巡河
			if (getUser().getBaiduLatPoints() != null && !getUser().getBaiduLatPoints().equals("")) {
				AlertDialog.Builder ab = new AlertDialog.Builder(ChiefEditRecordActivity.this);
				ab.setTitle("有未完成的巡河轨迹");
				ab.setMessage("系统检测到您上次未正常提交巡河单，继续采用之前的巡河轨迹？");
				ab.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						latlist_temp = getUser().getBaiduLatPoints();
						lnglist_temp = getUser().getBaiduLngPoints();
						arg0.dismiss();
					}
				});
				ab.setNegativeButton("否", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});
				ab.setCancelable(false);
				ab.create().show();
			}

			//检查是否开启了GPS,若未开启，则弹出窗口令其开启GPS
			if (!isOPen(getApplicationContext())) {
				//弹窗
				AlertDialog.Builder ab = new AlertDialog.Builder(ChiefEditRecordActivity.this);
				ab.setTitle("开启GPS定位");
				ab.setMessage("为了正常记录你的巡河位置信息，需要你开启GPS定位功能");
				ab.setPositiveButton("开启", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
					}
				});
				ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});
				ab.setCancelable(false);
				ab.create().show();
			}
		} else {//从具体巡河条目中进入
			setTitle("编辑巡查记录");

			findViewById(R.id.iv_head_left).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
				}
			});

			ll_cboxes.removeAllViews();

			showOperating();
			getRequestContext().add("Edit_RiverRecord", new Callback<RiverRecordRes>() {
				@Override
				public void callback(RiverRecordRes o) {
					hideOperating();
					if (o != null && o.isSuccess()) {

						o.data.recordId = riverRecord.recordId;
						o.data.recordSerNum = riverRecord.recordSerNum;
						o.data.recordDate = riverRecord.recordDate;
						o.data.isRead = riverRecord.isRead;
						riverRecord = o.data;

						riverRecord.locRiver = null;
						for (River r : getUser().riverSum) {
							if (riverRecord.riverId == r.riverId) {
								riverRecord.locRiver = r;
								riverRecord.locRiverName = r.riverName;
							}
						}
						initPhotoView(o.data.picPath);
						//切换按钮

						//获得经纬度信息
						latList = o.data.latlist;
						lngList = o.data.lnglist;
						//如果无轨迹则不显示“查看轨迹”按钮
						if (latList == "" && lngList == "") {
							btn_track.setVisibility(View.GONE);
							btn_trackView.setVisibility(View.GONE);
						} else {
							btn_track.setVisibility(View.GONE);
							btn_trackView.setVisibility(View.VISIBLE);
						}

						viewRender.renderView(findViewById(R.id.sv_main), riverRecord);
//						if (!getUser().isNpc())
						//点击具体条目之后，若巡河人的级别为人大，则用人大的视图；是河长，则用河长的视图
						if (riverRecord.recordPersonAuthority > 20) {
							refreshToNpcView();

							//1.人大  2.查看的是自己的河的巡河记录
							if (getUser().isNpc() && deputyId == 0) {
								//显示代表巡河河长是否已阅
								findViewById(R.id.ll_isread).setVisibility(View.VISIBLE);
								((ImageView) findViewById(R.id.iv_status)).setImageResource(riverRecord.isRead == 1 ? R.drawable.im_cp_handled : R.drawable.im_cp_unhandle);
								((TextView) findViewById(R.id.tv_isread)).setTextColor(getResources().getColor(riverRecord.isRead == 1 ? R.color.green : R.color.red));
								((TextView) findViewById(R.id.tv_isread)).setText(riverRecord.isRead == 1 ? R.string.sup_isread : R.string.sup_notread);
							}

						}
						else
							refreshToView();

					}
				}
			}, RiverRecordRes.class, ParamUtils.freeParam(null, "recordId", riverRecord.recordId,
					"recordPersonName", riverRecord.recordPersonName,
					"recordPersonId",riverRecord.recordPersonId,
					"authority", getUser().getAuthority()));
		}

		/*findViewById(R.id.ib_chief_photo).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				findViewById(R.id.tv_btn_explain).setVisibility(View.GONE);
				startAddPhoto();
			}
		});*/




		//相机拍摄照片
		findViewById(R.id.action_camera).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//若照片超过5张，则无法继续添加
				if (((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount() > 2) {
					showToast("照片过多，只限3张，可长按图片进行删除");
					((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
				} else {
					//关闭FloatingActionMenu
					((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
					startAddPhoto();
				}
			}
		});

		//相册挑选照片
		findViewById(R.id.action_album).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount() > 2) {
					showToast("照片过多，只限3张，可长按图片进行删除");
					((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
				} else {
					//关闭floatingActionMenu
					((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
					startAlbum();
				}
			}
		});

		if (isReadOnly) {
			setTitle("记录详情");
			et_deal.setEnabled(false);
			et_otherquestion.setEnabled(false);
			findViewById(R.id.hsv_photos).setVisibility(View.GONE);
			findViewById(R.id.btn_selriver).setVisibility(View.GONE);
			findViewById(R.id.btn_submit).setVisibility(View.GONE);
			findViewById(R.id.multiple_actions).setVisibility(View.GONE);
			((Button) findViewById(R.id.btn_cancel)).setText("关闭");
		}

	}

	private static final String[] CBOX_TITLES = new String[]{//
			//
			"河面有无成片漂浮废弃物、病死动物等", //
			"河岸有无垃圾堆放", //
			"河岸有无新建违法建筑物",//
			"河底有无明显污泥或垃圾淤积", //
			"河道水体有无臭味，颜色有无黑色",//
			"沿线有无晴天排污口", //
			"河道长效管理机制和保洁机制是否到位"//
	};

	private static final String[] CBOX_NPC_TITLES = new String[]{//
			//
			"1.河道水质情况评价（颜色、气味、浊度等）", //flotage
			"2.河道保洁情况评价（漂浮物、废弃物等）", //rubbish
			"3.河道养护情况评价（绿化、游步道等）", // odour
			"4.沿线违法、违章建筑情况", // building
			"5.沿线排污口情况（以晴天是否排污为准）"// outfall
	};


	private static final String[] CBOX_FIELDS = new String[]{//
			//
			"flotage",//
			"rubbish",//
			"building",//
			"sludge",//
			"odour",//
			"outfall",//
			"riverinplace",//
	};

	private static final String[] CBOX_NPC_FIELDS = new String[]{//
			//
			"flotage",//
			"rubbish",//
			"odour",//
			"building",//
			"outfall",//
	};

	private View[] CBOXES = new View[CBOX_FIELDS.length];
	private View[] CBOXES_NPC = new View[CBOX_NPC_FIELDS.length];

	private OnCheckedChangeListener cclTogTag = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton cb, boolean c) {
			View v = (View) cb.getTag();
			CompoundButton tcb = (CompoundButton) v.findViewById(cb.getId() == R.id.cb_no ? R.id.cb_yes : R.id.cb_no);
			if (tcb.isChecked() == c) {
				tcb.setChecked(!c);
			}
			v.findViewById(R.id.et_text).setVisibility(((CompoundButton) v.findViewById(R.id.cb_yes)).isChecked() ? View.VISIBLE : View.GONE);
		}
	};

	private View.OnClickListener exitTrackRiver = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			AlertDialog.Builder ab = new AlertDialog.Builder(ChiefEditRecordActivity.this);
			ab.setTitle("是否退出巡河");
			ab.setMessage("退出巡河界面后，所记录轨迹将消失");
			ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {

					//提交成功之后，将缓存坐标值设为空
					latlist_temp = "";
					lnglist_temp = "";

					finish();
				}
			});
			ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
			ab.setCancelable(false);
			ab.create().show();
		}
	};

	private OnCheckedChangeListener cclTogTagNpc = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton cb, boolean c) {
			View v = (View) cb.getTag();

			if (cb.getId() == R.id.cb_good && c) {
				((CompoundButton) v.findViewById(R.id.cb_bad)).setChecked(false);
				((CompoundButton) v.findViewById(R.id.cb_medium)).setChecked(false);
			}

			if (cb.getId() == R.id.cb_medium && c) {
				((CompoundButton) v.findViewById(R.id.cb_good)).setChecked(false);
				((CompoundButton) v.findViewById(R.id.cb_bad)).setChecked(false);
			}

			if (cb.getId() == R.id.cb_bad && c) {
				((CompoundButton) v.findViewById(R.id.cb_medium)).setChecked(false);
				((CompoundButton) v.findViewById(R.id.cb_good)).setChecked(false);
			}

			v.findViewById(R.id.et_text).setVisibility(((CompoundButton) v.findViewById(R.id.cb_bad)).isChecked() ? View.VISIBLE : View.GONE);
		}
	};

	private void refreshToView() {
		// gen yes or no
		ll_cboxes.removeAllViews();
		Class<?> clz = RiverRecord.class;
		for (int i = 0; i < CBOX_TITLES.length; ++i) {
			if (i > 0) {
				ll_cboxes.addView(LinearLayout.inflate(this, R.layout.inc_vline, null));
			}
			View v = LinearLayout.inflate(this, R.layout.inc_line_yesno, null);
			((TextView) v.findViewById(R.id.tv_title)).setText(CBOX_TITLES[i]);

			boolean yes = false;
			String text = null;
			try {
				Field f = clz.getField(CBOX_FIELDS[i]);
				yes = f.getInt(riverRecord) == 1;

				text = (String) clz.getField(CBOX_FIELDS[i] + "s").get(riverRecord);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (text == null)
				text = "";

			if ("riverinplace".equals(CBOX_FIELDS[i])) {
				((CheckBox) v.findViewById(R.id.cb_yes)).setText("不到位");
				((CheckBox) v.findViewById(R.id.cb_no)).setText("到位");
			}

			v.findViewById(R.id.cb_no).setTag(v);
			v.findViewById(R.id.cb_yes).setTag(v);
			((EditText) v.findViewById(R.id.et_text)).setText(text);

			if (!isReadOnly) {
				((CheckBox) v.findViewById(R.id.cb_yes)).setOnCheckedChangeListener(cclTogTag);
				((CheckBox) v.findViewById(R.id.cb_no)).setOnCheckedChangeListener(cclTogTag);
			} else {
				((CheckBox) v.findViewById(R.id.cb_yes)).setEnabled(false);
				((CheckBox) v.findViewById(R.id.cb_no)).setEnabled(false);
				v.findViewById(R.id.et_text).setEnabled(false);
			}
			((CheckBox) v.findViewById(R.id.cb_yes)).setChecked(yes);
			((CheckBox) v.findViewById(R.id.cb_no)).setChecked(!yes);
			v.findViewById(R.id.et_text).setVisibility(yes ? View.VISIBLE : View.GONE);

			CBOXES[i] = v;
			ll_cboxes.addView(v);
		}

		refreshSelectRiverBtn();
	}

	private void refreshToNpcView() {
		ll_cboxes.removeAllViews();
		Class<?> clz = RiverRecord.class;
		for (int i = 0; i < CBOX_NPC_TITLES.length; ++i) {
			if (i > 0) {
				ll_cboxes.addView(LinearLayout.inflate(this, R.layout.inc_vline, null));
			}
			View v = LinearLayout.inflate(this, R.layout.inc_line_npctrack, null);
			((TextView) v.findViewById(R.id.tv_title)).setText(CBOX_NPC_TITLES[i]);

			int yes = 1;
			String text = null;
			try {
				Field f = clz.getField(CBOX_NPC_FIELDS[i]);
				yes = f.getInt(riverRecord); //决定哪一个打勾

				text = (String) clz.getField(CBOX_NPC_FIELDS[i] + "s").get(riverRecord);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (text == null)
				text = "";

			if ("building".equals(CBOX_NPC_FIELDS[i]) || "outfall".equals(CBOX_NPC_FIELDS[i]) ) {
				((CheckBox) v.findViewById(R.id.cb_good)).setText("无");
				((CheckBox) v.findViewById(R.id.cb_medium)).setText("难以确认");
				((CheckBox) v.findViewById(R.id.cb_bad)).setText("有");
			}

			v.findViewById(R.id.cb_good).setTag(v);
			v.findViewById(R.id.cb_medium).setTag(v);
			v.findViewById(R.id.cb_bad).setTag(v);
			((EditText) v.findViewById(R.id.et_text)).setText(text);


			if (!isReadOnly) {
				((CheckBox) v.findViewById(R.id.cb_good)).setOnCheckedChangeListener(cclTogTagNpc);
				((CheckBox) v.findViewById(R.id.cb_medium)).setOnCheckedChangeListener(cclTogTagNpc);
				((CheckBox) v.findViewById(R.id.cb_bad)).setOnCheckedChangeListener(cclTogTagNpc);
			} else {
				((CheckBox) v.findViewById(R.id.cb_good)).setEnabled(false);
				((CheckBox) v.findViewById(R.id.cb_bad)).setEnabled(false);
				((CheckBox) v.findViewById(R.id.cb_medium)).setEnabled(false);
				v.findViewById(R.id.et_text).setEnabled(false);
			}
			if (yes == 0)
				yes = 1;
			((CheckBox) v.findViewById(R.id.cb_good)).setChecked(yes == 1); //加个小于号是为了默认能够在“好”的地方打勾
			((CheckBox) v.findViewById(R.id.cb_medium)).setChecked(yes == 2);
			((CheckBox) v.findViewById(R.id.cb_bad)).setChecked(yes == 3);
			v.findViewById(R.id.et_text).setVisibility(yes == 3? View.VISIBLE : View.GONE);

			CBOXES_NPC[i] = v;
			ll_cboxes.addView(v);
		}
		//去掉处理情况
		findViewById(R.id.ll_deal).setVisibility(View.GONE);

		refreshSelectRiverBtn();
	}

	//更新选择河道按钮
	private void refreshSelectRiverBtn() {
		if (riverRecord.locRiver != null)
			((Button) findViewById(R.id.btn_selriver)).setText(riverRecord.locRiver.riverName);
		else if (getUser().riverSum.length == 1) {
			//如果河长或人大只有一条河，则直接显示其所负责的河段
			((Button) findViewById(R.id.btn_selriver)).setText(getUser().riverSum[0].riverName);
			riverRecord.locRiver = getUser().riverSum[0];
			riverRecord.riverId = riverRecord.locRiver.riverId;
			riverRecord.locRiverName = riverRecord.locRiver.riverName;
		}
		else
			((Button) findViewById(R.id.btn_selriver)).setText("请选择河道");
	}

	private void selectRiver() {
		River[] rivers = getUser().riverSum;
		String[] names = new String[rivers.length];
		for (int i = 0; i < names.length; ++i) {
			names[i] = rivers[i].riverName;
		}
		Dialog alertDialog = new AlertDialog.Builder(this).setTitle("请选择河道").setItems(names, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				riverRecord.locRiver = getUser().riverSum[which];
				riverRecord.riverId = riverRecord.locRiver.riverId;
				riverRecord.locRiverName = riverRecord.locRiver.riverName;

				refreshSelectRiverBtn();
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).create();
		alertDialog.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_cancel:
				finish();
				break;
			case R.id.btn_selriver: {
				selectRiver();
				break;
			}
	/*	case R.id.btn_track:{
			Intent intent = new Intent(ChiefEditRecordActivity.this,com.zju.hzsz.chief.activity.ChiefInspectActivity.class);
			startActivity(intent);
			break;
		}*/
			case R.id.btn_submit: {

				if (!hasImg) {
					//如果不是人大，则不用拍照片
					if (!getUser().isNpc()) {
						showToast("您还没拍摄照片，请点击右侧“+”按钮添加照片");
						return;
					}
				}

				if (riverRecord.riverId == 0) {
					showToast("您还没有选择河道，请先选择河道");
					return;
				} else {
					submitParam = new JSONObject();
					boolean needdeal = false;
					try {
						if (riverRecord.recordId != 0) {
							submitParam.put("recordId", riverRecord.recordId);
						}
						if (!getUser().isNpc()) {
							for (int i = 0; i < CBOX_FIELDS.length; ++i) {
								boolean b = ((CompoundButton) CBOXES[i].findViewById(R.id.cb_yes)).isChecked();
								submitParam.put(CBOX_FIELDS[i], b ? 1 : 0);
								needdeal = b || needdeal;

								String s = b ? ((EditText) CBOXES[i].findViewById(R.id.et_text)).getText().toString().trim() : "";
								if (b && s.length() == 0) {
									showToast("您还有具体描述没填写，请先填写");
									((EditText) CBOXES[i].findViewById(R.id.et_text)).requestFocus();
									((EditText) CBOXES[i].findViewById(R.id.et_text)).setFocusable(true);
									return;
								}
								submitParam.put(CBOX_FIELDS[i] + "s", s);
							}

							//处理情况
							if (needdeal && et_deal.getText().toString().length() == 0) {
								showToast("您还没有填写处理情况，请先填写");
								et_deal.requestFocus();
								et_deal.setFocusable(true);
								return;
							}
							submitParam.put("deal", et_deal.getText().toString());

						} else {
							for (int i = 0; i < CBOX_NPC_FIELDS.length; ++i) {
								int b = 0;
								if (((CompoundButton) CBOXES_NPC[i].findViewById(R.id.cb_good)).isChecked())
									b = 1;
								else if (((CompoundButton) CBOXES_NPC[i].findViewById(R.id.cb_medium)).isChecked())
									b = 2;
								else
									b = 3;

								submitParam.put(CBOX_NPC_FIELDS[i], b );
								needdeal = b!=1  || needdeal;

								String s = b==3 ? ((EditText) CBOXES_NPC[i].findViewById(R.id.et_text)).getText().toString().trim() : "";
								if (b == 3 && s.length() == 0) {
									showToast("您还有具体描述没填写，请先填写");
									((EditText) CBOXES_NPC[i].findViewById(R.id.et_text)).requestFocus();
									((EditText) CBOXES_NPC[i].findViewById(R.id.et_text)).setFocusable(true);
									return;
								}
								submitParam.put(CBOX_NPC_FIELDS[i] + "s", s);
							}

							submitParam.put("sludge", 0);
							submitParam.put("sludges", "");
							submitParam.put("riverinplace", 0);
							submitParam.put("riverinplaces", "");
							submitParam.put("deal", "");

						}

						submitParam.put("RiverId", riverRecord.riverId);
						submitParam.put("otherquestion", et_otherquestion.getText().toString());


						//增加图片链接
						submitParam.put("picPath", picPath);

						//添加巡河轨迹经纬度信息
						submitParam.put("latlist", latlist_temp);
						submitParam.put("lnglist", lnglist_temp);

						//添加河长权限和uuid
						submitParam.put("authority", getUser().getAuthority());
						submitParam.put("UUID", getUser().getUuid());


						//巡河时间
						submitParam.put("startTime", startTime);

						if (location != null) {
							submitParam.put("latitude", location.getLatitude());
							submitParam.put("longtitude", location.getLongitude());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}


					LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_chief_photos);
					if (ll_photos.getChildCount() > 0) {
						// 有图片
						final Uri[] bmps = new Uri[ll_photos.getChildCount()];
						for (int i = 0; i < bmps.length; i++) {
							bmps[i] = (Uri) ll_photos.getChildAt(i).getTag();
						}
						asynCallAndShowProcessDlg("正在处理图片...", new Callable() {
							@Override
							public void call(Object... args) {

								StringBuffer sb = new StringBuffer();
								for (Uri bmp : bmps) {
									if (sb.length() > 0)
										sb.append(";");
									byte[] bts = bmp2Bytes(bmp);
//									Log.i("NET", bmp.toString() + " bts.len " + bts.length);
									Log.i("NET", " bts.len " + bts.length);
									sb.append(Base64.encodeToString(bts, Base64.DEFAULT));
								}
								String picbase64 = sb.toString();
								Log.i("NET", "all base64.len " + picbase64.length());
								try {
									submitParam.put("picBase64", picbase64);
								} catch (JSONException e) {
									e.printStackTrace();
								}
								safeCall(new Callable() {
									@Override
									public void call(Object... args) {
										submitData();
									}
								});
							}
						});
					} else {
						submitData();
					}
				}
				break;
			}
			default:
				super.onClick(v);
				break;
		}
	}

	JSONObject submitParam = null;

	private void submitData() {
		showOperating(R.string.doing_submitting);

		//判断是否是村级河长
		boolean isVillageChief = getUser().isLogined() && getUser().isVillageChief();

		getRequestContext().add("AddOrEdit_RiverRecord", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					showToast("提交成功!");
					setResult(RESULT_OK);

					//提交成功之后，将缓存坐标值设为空
					latlist_temp = "";
					lnglist_temp = "";

					finish();
				}
			}
		}, BaseRes.class, submitParam);
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();

		if (!isReadOnly) {
			getLocalService().getLocation(new LocalService.LocationCallback() {
				@Override
				public void callback(Location location) {
					ChiefEditRecordActivity.this.location = location;
				}
			});
		}
	}

	private Uri imageFilePath = null;

	private void startAddPhoto() {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DISPLAY_NAME, "拍照");
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		imageFilePath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); //内容提供者，设置地址+存放照片

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //指定开启系统相机的Action
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath); //设置系统相机拍摄完成后照片的存放地址
		startActivityForResult(intent, Tags.CODE_ADDPHOTO);
	}

	//从相册中调取照片
	private void startAlbum() {

		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, Tags.CODE_ALBUM);
	}

	//拍摄完成后在巡河表处添加照片
	private void addPhoto(Uri uri) {
		//view的layout属性：48*48+fitXY
		View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
		BitmapFactory.Options op = new BitmapFactory.Options();
		try {
			Bitmap pic = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null, op);

			int w = Values.UPLOAD_IMG_W;
			int h = Values.UPLOAD_IMG_H;
			if (pic.getHeight() != h)
				pic = ThumbnailUtils.extractThumbnail(pic, w, h);

			view.setTag(pic);
			Log.i("NET", "" + pic.getWidth() + "*" + pic.getHeight());
			((ImageView) view.findViewById(R.id.iv_photo)).setImageBitmap(pic);
			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(final View view) {
					//弹出“是否确定删除”对话框
					//弹窗
					AlertDialog.Builder ab = new AlertDialog.Builder(ChiefEditRecordActivity.this);
					ab.setTitle("删除图片");
					ab.setMessage("是否确定删除该巡河照片？");
					ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//移除照片
							((LinearLayout) findViewById(R.id.ll_chief_photos)).removeView(view);

							arg0.dismiss();
						}
					});
					ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});
					ab.setCancelable(false);
					ab.create().show();

					//在长按操作之后不再加入短按操作
					return true;
				}
			});

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		view.setTag(uri);
		((LinearLayout) findViewById(R.id.ll_chief_photos)).addView(view, ((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount());
	}

	private byte[] bmp2Bytes(Uri uri) {
		try {
			BitmapFactory.Options op = new BitmapFactory.Options();
			Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, op);

			int w = Values.UPLOAD_IMG_W;
			// int h = (int) (photo.getHeight() * (((float) w) /
			// photo.getWidth()));
			int h = Values.UPLOAD_IMG_H;
			if (photo.getHeight() != h)
				photo = ThumbnailUtils.extractThumbnail(photo, w, h);
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024);
			photo.compress(Bitmap.CompressFormat.JPEG, 75, baos);
			baos.flush();
			baos.close();
			byte[] data = baos.toByteArray();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == Tags.CODE_ADDPHOTO) && resultCode == RESULT_OK) {
			// Log.e("uri", imageFilePath.toString());
			// addPhoto(imageFilePath);
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(imageFilePath, "image/*");
			// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
			intent.putExtra("crop", "true");
			// aspectX aspectY 是宽高的比例
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 800);
			intent.putExtra("outputY", 800);
			// intent.putExtra("return-data", true);

			intent.putExtra("output", imageFilePath);
			startActivityForResult(intent, Tags.CODE_CUTPHOTO);
		} else if (requestCode == Tags.CODE_CUTPHOTO && resultCode == RESULT_OK) {
			addPhoto(imageFilePath);
			hasImg = true;
		} else if (requestCode == Tags.CODE_ALBUM && resultCode == RESULT_OK){
			try {

				Uri uri = data.getData();
				final String absolutePath=
						getAbsolutePath(ChiefEditRecordActivity.this, uri);
				addPhoto(uri);
				hasImg = true;

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode == Tags.CODE_LATLNG && resultCode == RESULT_OK) {
			String result = data.getExtras().getString("result");
			//由inspect返回并需要上传至服务器的地理位置信息
			latList = data.getExtras().getString("latList");
			lngList = data.getExtras().getString("lngList");
			//由inspect返回的当前位置信息
			latlist_temp = "" + latList;
			lnglist_temp = "" + lngList;
			Log.i("来自record的latList", latList);
			Log.i("来自record的lngList", lngList);
			Log.i("recordinspect", latlist_temp);
			//改变按钮内容与颜色
			/*btn_track.setText("完成巡河");
			btn_track.setClickable(false);*/
		}
	}

	public String getAbsolutePath(final Context context,
								  final Uri uri) {
		if (null == uri) return null;
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null)
			data = uri.getPath();
		else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri,
					new String[]{MediaStore.Images.ImageColumns.DATA},
					null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(
							MediaStore.Images.ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}     return data;
	}

	String[] imgUrls = null;
	private View.OnClickListener clkGotoPhotoView = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof Integer) {
				Intent intent = new Intent(ChiefEditRecordActivity.this, PhotoViewActivity.class);
				intent.putExtra("URLS", imgUrls);
				intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
				startActivity(intent);
			}
		}
	};

	private void initPhotoView(String urls) {
		final List<String> list = new ArrayList<String>();
		if (urls != null) {
			hasImg = true; //如果之前的巡河记录存在照片，则无需再进行拍照
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
			findViewById(R.id.hsv_result_photos).setVisibility(View.VISIBLE);
			final LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_result_photos);
			ll_photos.removeAllViews();

			for (int i = 0; i < imgUrls.length; ++i) {
				final String url = imgUrls[i];
				System.out.println("url:" + url);
				View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
				ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
				view.setTag(i);
				view.setOnClickListener(clkGotoPhotoView);
				//实现长按删除图片
				view.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(final View view) {

						//弹出“是否确定删除”对话框
						//弹窗
						AlertDialog.Builder ab = new AlertDialog.Builder(ChiefEditRecordActivity.this);
						ab.setTitle("删除图片");
						ab.setMessage("是否确定删除该巡河照片？");
						ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								//移除照片
								ll_photos.removeView(view);

								//picPath删去相应图片的链接
								list.remove((int) view.getTag());
								picPath = "";
								for (int j = 0; j < list.size(); j++) {
									picPath = picPath + list.get(j) + ";";
								}

								//为了放大查看图片时而更新数据
								imgUrls = new String[list.size()];
								imgUrls = list.toArray(imgUrls);
								//更新每张图片的tag
								for (int j = 0; j < ll_photos.getChildCount(); j++) {
									ll_photos.getChildAt(j).setTag(j);
								}

								arg0.dismiss();
							}
						});
						ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						});
						ab.setCancelable(false);
						ab.create().show();

						//在长按操作之后不再加入短按操作
						return true;

					}
				});
				ll_photos.addView(view);
			}

		} else {
			findViewById(R.id.hsv_result_photos).setVisibility(View.GONE);
		}
	}

	/**
	 * 接下来是定位相关的代码
	 * String:locate_temp:当前的定位数据
	 */

	/**
	 * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
	 *
	 * @param context
	 * @return true 表示开启
	 */
	public static final boolean isOPen(final Context context) {
		LocationManager locationManager
				= (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		// 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
		boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return gps;
	}


	Handler handler = new Handler();
	private boolean isFirstLoc = true;
	LocationClient mLocClient;
	public MyLocationListener myListener = new MyLocationListener();
	List<LatLng> points = new ArrayList<LatLng>();//全部点
	LatLng point;


	/**
	 * 地图初始化
	 */
	public void initLocation() {

		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);

		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);

		mLocClient.setLocOption(option);
		mLocClient.start();
	}
	

	/**
	 * 地图sdk监听器
	 */
	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation bdLocation) {

			if (bdLocation == null)
				return;

			LatLng point = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
			points.add(point);
		}
	}

	/**
	 * 每10s记录一次当前坐标值
	 */
	private class MyRunable implements Runnable {

		@Override
		public void run() {

			if (!mLocClient.isStarted()) {
				mLocClient.start();
			}

			if (points == null) {
				handler.postDelayed(this, 10000);
				return;
			}

			if (isFirstLoc && points.size() >= 1 && latlist_temp == null) {
				//若从inspect返回（latlist_temp != null)就不执行这里的代码
				isFirstLoc = false;
				lnglist_temp = "" + points.get(points.size() - 1).longitude;
				latlist_temp = "" + points.get(points.size() - 1).latitude;
				point = points.get(points.size() - 1);
				Log.i("temp:", "first" + latlist_temp);
			} else if (points.size() > 1) {
				//要解决从inspect返回时point的经纬度
				if (isFirstLoc) {
					//如果是从inspect返回，则isFirstLoc始终为真
					System.out.println("recordInspect" + "：来自inspect跳到edit的记录位置");
					point = points.get(points.size() - 1);
					isFirstLoc = false;
				}
				if (point.latitude != points.get(points.size() - 1).latitude ||
						point.longitude != points.get(points.size() - 1).longitude) { //如果一直处于一个位置则不重复记录
					lnglist_temp = lnglist_temp + "," + points.get(points.size() - 1).longitude;
					latlist_temp = latlist_temp + "," + points.get(points.size() - 1).latitude;
					point = points.get(points.size() - 1);
					Log.i("temp:", latlist_temp);
				}
			}

			handler.postDelayed(this, 10000);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		getUser().setBaiduLatPoints(latlist_temp);
		getUser().setBaiduLngPoints(lnglist_temp);

		points.clear();
		mLocClient.unRegisterLocationListener(myListener);
	}

	//防止退出当前activity之后还在继续定位
	@Override
	protected void onDestroy() {
		super.onDestroy();
	/*	points.clear();
		mLocClient.unRegisterLocationListener(myListener);*/
	}
}
