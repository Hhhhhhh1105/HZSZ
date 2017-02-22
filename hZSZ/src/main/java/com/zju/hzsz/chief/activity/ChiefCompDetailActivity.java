package com.zju.hzsz.chief.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.base.Callable;
import com.sin.android.sinlibs.tagtemplate.TemplateEngine;
import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.activity.PhotoViewActivity;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.model.ChiefComp;
import com.zju.hzsz.model.ChiefCompFul;
import com.zju.hzsz.model.ChiefCompFulRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.utils.ViewUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ChiefCompDetailActivity extends BaseActivity {
	private ChiefComp comp = null;
	private ChiefCompFul compFul = null;
	private boolean isComp = true;
	private boolean isHandled = true;
	private boolean hasImg = false;

	private TemplateEngine templateEngine = new TemplateEngine() {

		@Override
		public String evalString(Object model, String tmpl) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
			String s = super.evalString(model, tmpl);
			if (s != null) {
				if (isComp) {
					s = s.replace("建议", "投诉");
				} else {
					s = s.replace("投诉", "建议");
				}
			}
			return s;
		}

	};

	private ViewRender viewRender = new ViewRender(templateEngine);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_compdetail);
		isHandled = getIntent().getBooleanExtra(Tags.TAG_HANDLED, false);
		comp = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_COMP), ChiefComp.class);

		findViewById(R.id.ll_evalinfo).setVisibility(View.GONE);
		if (isHandled) {
			findViewById(R.id.ll_handle).setVisibility(View.GONE);
			// findViewById(R.id.ll_status).setVisibility(View.GONE);
		} else {
			if (!comp.isAddingHandle())
				findViewById(R.id.ll_result).setVisibility(View.GONE);
		}

		if (comp != null) {
			isComp = comp.isComp();

			initHead(R.drawable.ic_head_back, 0);
			setTitle(!isHandled ? "处理投诉" : "处理单");

			if (comp.isComp()) {
				// 投诉

				ViewUtils.replaceInView((ViewGroup) findViewById(R.id.ll_root), "建议", "投诉");
			} else {
				ViewUtils.replaceInView((ViewGroup) findViewById(R.id.ll_root), "投诉", "建议");
			}

			findViewById(R.id.btn_submit).setOnClickListener(this);
			findViewById(R.id.btn_store).setOnClickListener(this);
			findViewById(R.id.btn_cancel).setOnClickListener(this);

			findViewById(R.id.sv_main).setVisibility(View.INVISIBLE);
			viewRender.renderView(findViewById(R.id.ll_root), comp);
			initPhotoView(comp.complaintsPicPath);

			findViewById(R.id.ib_chief_photo).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					startAddPhoto();
				}
			});

			loadData();
		} else {
			showToast("没有处理单信息");
			finish();
		}
	}

	private String[] imgUrls = null;

	private View.OnClickListener clkGotoPhotoView = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof Integer) {
				Intent intent = new Intent(ChiefCompDetailActivity.this, PhotoViewActivity.class);
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
				ImgUtils.loadImage(ChiefCompDetailActivity.this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
				view.setTag(i);
				view.setOnClickListener(clkGotoPhotoView);
				ll_photos.addView(view);
			}

		} else {
			findViewById(R.id.hsv_photos).setVisibility(View.GONE);
		}
	}

	private void loadData() {
		showOperating();
		getRequestContext().add(comp.isComp() ? "Get_ChiefComplain_Content" : "Get_ChiefAdvise_Content", new Callback<ChiefCompFulRes>() {
			@Override
			public void callback(ChiefCompFulRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					compFul = o.data;
					compFul.isComp = isComp;
					compFul.advTheme = comp.advTheme;
					compFul.compTheme = comp.compTheme;
					viewRender.renderView(findViewById(R.id.ll_root), compFul);

					if (compFul.ifAnonymous == 1) {
						((TextView) findViewById(R.id.tv_user_name)).setText("***");
						((TextView) findViewById(R.id.tv_user_telno)).setText("***********");
					}

					if (compFul.evelContent != null && compFul.evelContent.length() > 0) {
						findViewById(R.id.ll_evalinfo).setVisibility(View.VISIBLE);
					}
					findViewById(R.id.sv_main).setVisibility(View.VISIBLE);

					if (comp.complaintsPicPath == null) {
						comp.complaintsPicPath = compFul.getPicPath();
						initPhotoView(comp.complaintsPicPath);
					}
					
					initResultPhotoView(compFul.dealPicPath);
				}
			}
		}, ChiefCompFulRes.class, ParamUtils.freeParam(null, comp.isComp() ? "complianId" : "adviseId", comp.getId()));
	}

	@Override
	public void onClick(View v) {
		int ope = 0;
		switch (v.getId()) {
		case R.id.btn_cancel: {
			finish();
			break;
		}
		case R.id.btn_submit:
			ope = 1;
		case R.id.btn_store: {
			String s = ((EditText) findViewById(R.id.et_handlecontent)).getText().toString().trim();
			if (s.length() > 0) {
				opetype = ope;
				dealContent = s;
				LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_chief_photos);
				if (ll_photos.getChildCount() <= 1){
					showToast("请您拍摄相关图片，在回复详情栏上传之后再提交");
					return;
				}
				if (ll_photos.getChildCount() > 1) {
					// 有图片
					hasImg = true;
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
							picbase64 = sb.toString();
							Log.i("NET", "all base64.len " + picbase64.length());
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
			} else {
				showToast("处理结果不能为空!");
			}
			break;
		}
		default:
			super.onClick(v);
		}
	}

	private String picbase64 = "";
	private int opetype = 0;
	private String dealContent = null;

	private void submitData() {
		showOperating("正在提交数据...");
		getRequestContext().add(comp.isComp() ? "Add_ChiefComplainDeal_Content" : "Add_ChiefAdviseDeal_Content", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					showToast(opetype == 0 ? "暂存成功" : "提交成功");
					if (opetype == 1) {
						setResult(RESULT_OK);
						finish();
					}
				}
			}
			// picBase64
		}, BaseRes.class, ParamUtils.freeParam(null, comp.isComp() ? "complianId" : "adviseId", comp.getId(), "dealStatus", opetype, "dealContent", dealContent, "picBase64", picbase64));
	}

	private Uri imageFilePath = null;

	private void startAddPhoto() {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DISPLAY_NAME, "拍照");
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		imageFilePath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath);
		startActivityForResult(intent, Tags.CODE_ADDPHOTO);
	}

	private void addPhoto(Uri uri) {
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
		}
	}
	
	String[] imgResultUrls = null;
	private View.OnClickListener clkResultGotoPhotoView = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof Integer) {
				Intent intent = new Intent(ChiefCompDetailActivity.this, PhotoViewActivity.class);
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
