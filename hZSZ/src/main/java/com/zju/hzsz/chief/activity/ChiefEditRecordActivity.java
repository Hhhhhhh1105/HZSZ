package com.zju.hzsz.chief.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
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

	private boolean isReadOnly = false;

	private String latList;
	private String lngList;

	private boolean hasImg = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_editrecord);

		initHead(R.drawable.ic_head_back, 0);

		InjectUtils.injectViews(this, R.id.class);

		findViewById(R.id.btn_selriver).setOnClickListener(this);//选择河道按钮
		findViewById(R.id.btn_submit).setOnClickListener(this);//保存按钮
		findViewById(R.id.btn_cancel).setOnClickListener(this);//取消按钮

		btn_track = (Button) findViewById(R.id.btn_track);//巡河开始按钮
		btn_track.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChiefEditRecordActivity.this,com.zju.hzsz.chief.activity.ChiefInspectActivity.class);
				startActivityForResult(intent, Tags.CODE_LATLNG);
			}
		});
		btn_trackView = (Button) findViewById(R.id.btn_trackView);
		btn_trackView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChiefEditRecordActivity.this, com.zju.hzsz.chief.activity.ChiefTrackViewActivity.class);
				intent.putExtra("latList", latList);
				intent.putExtra("lngList", lngList);
				startActivity(intent);
			}
		});


		riverRecord = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_RECORD), RiverRecord.class);

		isReadOnly = getIntent().getBooleanExtra(Tags.TAG_ABOOLEAN, false);

		if (riverRecord == null) {//从新建中进入
			setTitle("新建巡查记录");
			riverRecord = new RiverRecord();
			riverRecord.recordDate = DateTime.getNow(); //巡河时间
			riverRecord.locRiverName = "选择河道";
			viewRender.renderView(findViewById(R.id.sv_main), riverRecord);

			refreshToView();
		} else {//从具体巡河条目中进入
			setTitle("编辑巡查记录");
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
						btn_track.setVisibility(View.GONE);
						btn_trackView.setVisibility(View.VISIBLE);
						//获得经纬度信息
						latList = o.data.latlist;
						lngList = o.data.lnglist;

						viewRender.renderView(findViewById(R.id.sv_main), riverRecord);
						refreshToView();

					}
				}
			}, RiverRecordRes.class, ParamUtils.freeParam(null, "recordId", riverRecord.recordId));
		}

		findViewById(R.id.ib_chief_photo).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startAddPhoto();
			}
		});

		if (isReadOnly) {
			setTitle("记录详情");
			et_deal.setEnabled(false);
			et_otherquestion.setEnabled(false);
			findViewById(R.id.hsv_photos).setVisibility(View.GONE);
			findViewById(R.id.btn_selriver).setVisibility(View.GONE);
			findViewById(R.id.btn_submit).setVisibility(View.GONE);
			((Button) findViewById(R.id.btn_cancel)).setText("关闭");
		}
	}

	private static final String[] CBOX_TITLES = new String[] {//
	//
			"河面有无成片漂浮废弃物、病死动物等", //
			"河岸有无垃圾堆放", //
			"河岸有无新建违法建筑物",//
			"河底有无明显污泥或垃圾淤积", //
			"河道水体有无臭味，颜色有无黑色",//
			"沿线有无晴天排污口", //
			"河道长效管理机制和保洁机制是否到位"//
	};

	private static final String[] CBOX_FIELDS = new String[] {//
	//
			"flotage",//
			"rubbish",//
			"building",//
			"sludge",//
			"odour",//
			"outfall",//
			"riverinplace",//
	};

	private View[] CBOXES = new View[CBOX_FIELDS.length];

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

	//更新选择河道按钮
	private void refreshSelectRiverBtn() {
		if (riverRecord.locRiver != null)
			((Button) findViewById(R.id.btn_selriver)).setText(riverRecord.locRiver.riverName);
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

			if (!hasImg){
				showToast("您还没拍摄照片，请上传后提交");
				return;
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
					submitParam.put("RiverId", riverRecord.riverId);
					submitParam.put("otherquestion", et_otherquestion.getText().toString());
					if (needdeal && et_deal.getText().toString().length() == 0) {
						showToast("您还没有填写处理情况，请先填写");
						et_deal.requestFocus();
						et_deal.setFocusable(true);
						return;
					}
					submitParam.put("deal", et_deal.getText().toString());

					//添加巡河轨迹经纬度信息
					submitParam.put("latlist",latList);
					submitParam.put("lnglist",lngList);

					if (location != null) {
						submitParam.put("latitude", location.getLatitude());
						submitParam.put("longtitude", location.getLongitude());
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_chief_photos);
				if (ll_photos.getChildCount() > 1) {
					// 有图片
					final Uri[] bmps = new Uri[ll_photos.getChildCount() - 1];
					for (int i = 0; i < bmps.length; ++i) {
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
								Log.i("NET", bmp.toString() + " bts.len " + bts.length);
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
		getRequestContext().add("AddOrEdit_RiverRecord", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					showToast("提交成功!");
					setResult(RESULT_OK);
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

	//拍摄完成后在寻巡河表处添加照片
	private void addPhoto(Uri uri) {
		//view的layout属性：48*48+fitXY
		View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
		BitmapFactory.Options op = new BitmapFactory.Options();
		try {
			Bitmap pic = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null, op);
			view.setTag(pic);
			Log.i("NET", "" + pic.getWidth() + "*" + pic.getHeight());
			((ImageView) view.findViewById(R.id.iv_photo)).setImageBitmap(pic);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		view.setTag(uri);
		((LinearLayout) findViewById(R.id.ll_chief_photos)).addView(view, ((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount() - 1);
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
		}else if (requestCode == Tags.CODE_LATLNG && resultCode == RESULT_OK){
			String result = data.getExtras().getString("result");
			latList = data.getExtras().getString("latList");
			lngList = data.getExtras().getString("lngList");
			Log.i("来自record的latList",latList);
			Log.i("来自record的lngList",lngList);
			Log.i("latlng:", result);
			//改变按钮内容与颜色
			btn_track.setText("完成巡河");
			btn_track.setClickable(false);
		}
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
			findViewById(R.id.hsv_result_photos).setVisibility(View.VISIBLE);
			LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_result_photos);
			ll_photos.removeAllViews();

			for (int i = 0; i < imgUrls.length; ++i) {
				String url = imgUrls[i];
				View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
				ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
				view.setTag(i);
				view.setOnClickListener(clkGotoPhotoView);
				ll_photos.addView(view);
			}

		} else {
			findViewById(R.id.hsv_result_photos).setVisibility(View.GONE);
		}
	}
}
