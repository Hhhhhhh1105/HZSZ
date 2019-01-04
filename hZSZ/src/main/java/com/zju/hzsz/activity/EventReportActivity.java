package com.zju.hzsz.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.base.Callable;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.Event;
import com.zju.hzsz.model.EventRes;
import com.zju.hzsz.model.Lake;
import com.zju.hzsz.model.LakeList;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.SugOrComRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.service.LocalService;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * Created by ZJTLM4600l on 2018/3/15.
 * 巡河过程中事件上报
 */

public class EventReportActivity extends BaseActivity {
    River river = null;   //投诉河段
    Lake lake = null;   //投诉湖泊
    int segment_ix = -1;   //分段信息
    private Location location = null;

    private River r;
    private Lake l;

    private LinearLayout ll_cboxes = null;
    private boolean isReadOnly = false;

    //河长考核指标
    private int chiefPatrol = 0;
    private int chiefFeedBack = 0;
    private int chiefWork = 0;

    private Event[] events;
    private int eventId;
    private static final String TAG = "hhhh";

    private boolean fromLakeRecord;//是河的事件上报(来自于巡河)还是湖的事件上报（来自于巡湖） false：河；true：湖

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event_report);
        findViewById(R.id.sv_form).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_submit_result).setVisibility(View.GONE);

        initHead(R.drawable.ic_head_close, 0);

        fromLakeRecord = getIntent().getBooleanExtra("fromLakeRecord", false);
        int ix = getIntent().getIntExtra(Tags.TAG_INDEX, -1);
        Log.d(TAG, "ix: "+ix);

        //显示选择投诉类别
        getSugCategory();
        findViewById(R.id.ll_et_sug_category).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectSugCategory();
            }
        });

        //如果是人大代表，则显示“代表姓名”，并隐去匿名
        if (getUser().isNpc()) {
//			((EditText) findViewById(R.id.et_suggest_name)).setHint("代表姓名");
            //将投诉人直接显示为人大代表的姓名
            ((EditText) findViewById(R.id.et_suggest_name)).setText(getUser().getRealName());
            ((EditText) findViewById(R.id.et_suggest_name)).setEnabled(false);
            findViewById(R.id.ll_anonymity).setVisibility(View.GONE);
            //不自动弹出软键盘
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        //标题设置
        setTitle(R.string.ieventreport);

        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                submmit();
            }
        });

        findViewById(R.id.ib_photo).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // startActivityForResult(new
                // Intent(MediaStore.ACTION_IMAGE_CAPTURE), Tags.CODE_ADDPHOTO);
                //将提醒文字隐藏
//				findViewById(R.id.tv_btn_explain).setVisibility(View.GONE);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "拍照");
                // values.put(MediaStore.Images.Media.WIDTH, "800");
                // values.put(MediaStore.Images.Media.HEIGHT, "600");
                // values.put(MediaStore.Images.Media.DESCRIPTION,
                // "this is description");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                imageFilePath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath);

                startActivityForResult(intent, Tags.CODE_ADDPHOTO);
            }
        });

        ((CheckBox) findViewById(R.id.ck_anonymity)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean cked) {
                ((TextView) findViewById(R.id.tv_anonymity_tip)).setText(cked ? R.string.tip_anonymity_checked : R.string.tip_anonymity_uncheck);
            }
        });

        ((CheckBox) findViewById(R.id.ck_gps)).setChecked(!getUser().gpsdisable);

        ((CheckBox) findViewById(R.id.ck_gps)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean cked) {
                ((TextView) findViewById(R.id.tv_gps_tip)).setText(cked ? "(温馨提示:您的GPS定位信息将提交到服务器，用于投诉地图显示)" : "(温馨提示:您取消了定位)");
                if (cked)
                    getLocation();
            }
        });

        if (getUser().isLogined()) {
            ((EditText) findViewById(R.id.et_phonenum)).setText(getUser().userName);
        }

        findViewById(R.id.tv_rivername).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(fromLakeRecord){
                    selectLake();
                }else{
                    selectRiver();
                }
            }
        });

        //关于巡河事件上报和巡湖事件上报的适配
        if(fromLakeRecord){
            l = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_LAKE), Lake.class);
            if (l != null) {
                setWithLake(l);
            } else {
                selectLake();
            }

        }else{
            r = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_RIVER), River.class);
            Log.d(TAG, "river: "+r);

            if (r != null) {
                if (r.isPiecewise() && ix < 0) {
                    readyToSugOrCom(r);
                } else {
                    setWithRiver(r, ix);
                }
            } else {
                selectRiver();
            }


        }
    }

    private void selectRiver() {
        Intent intent = new Intent(this, SearchRiverActivity.class);
        intent.putExtra(Tags.TAG_CODE, Tags.CODE_SELECTRIVER);
        intent.putExtra(Tags.TAG_TITLE, "选择事件河道");
        startActivityForResult(intent, Tags.CODE_SELECTRIVER);
    }
    private void selectLake() {
        Intent intent = new Intent(this, LakeListAcitivity.class);
        intent.putExtra(Tags.TAG_CODE, Tags.CODE_SELECTLAKE);
        intent.putExtra(Tags.TAG_TITLE, "选择事件湖泊");
        startActivityForResult(intent, Tags.CODE_SELECTLAKE);
    }

    //选择投诉类型
    private void getSugCategory() {

        getRequestContext().add("Get_Event_Type", new Callback<EventRes>() {
            @Override
            public void callback(EventRes o) {
                if (o != null && o.isSuccess()) {
                    events = o.data;
                }
            }
        }, EventRes.class, null);

    }

    private void selectSugCategory() {
        if(events != null){
            final String[] names = new String[events.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = events[i].eventTitle;
            }

            Dialog alertDialog = new AlertDialog.Builder(this).setTitle("请选择事件类型").setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    ((TextView) findViewById(R.id.et_suggest_category)).setText(names[i]);
                    ((TextView) findViewById(R.id.et_suggest_category)).setTextColor(getResources().getColor(R.color.black));
                    eventId = events[i].eventId;

                }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create();
            alertDialog.show();
        }else{
            Dialog alertDialog = new AlertDialog.Builder(this).setTitle("请选择投诉类型").setItems(null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create();
            alertDialog.show();
        }

    }

    private void readyToSugOrCom(final River river) {
        if (river.isPiecewise()) {

            String[] names = new String[river.lowLevelRivers.length];
            for (int i = 0; i < names.length; ++i) {
                names[i] = river.lowLevelRivers[i].riverName;
            }
            Dialog alertDialog = new AlertDialog.Builder(this).setTitle(R.string.river_select_segment).setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setWithRiver(river, which);
                }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setCancelable(false).create();
            alertDialog.show();
        } else {
            setWithRiver(river, -1);
        }
    }


	/*建议表单相关 start*/

    //人大UI
    private void refreshToNpcView() {
        findViewById(R.id.inc_npc_sug).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_et_sugcontent).setVisibility(View.GONE);
        findViewById(R.id.ll_et_sugsubject).setVisibility(View.GONE);
        findViewById(R.id.ll_gps).setVisibility(View.GONE);
    }

    //为各个按钮绑定监听器
    private void btnInit() {
        int[] btnToInit = {
                R.id.cb_good_1, R.id.cb_good_2, R.id.cb_good_3,
                R.id.cb_medium_1, R.id.cb_medium_2, R.id.cb_medium_3,
                R.id.cb_bad_1, R.id.cb_bad_2, R.id.cb_bad_3
        };

        for (int id: btnToInit) {
            View v = findViewById(id);
            if (v != null)
                ((CompoundButton) v).setOnCheckedChangeListener(cclTogTagNpc);
        }
    }

    //各个按钮的监听器
    private CompoundButton.OnCheckedChangeListener cclTogTagNpc = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton cb, boolean c) {

            if (c) {
                switch (cb.getId()) {
                    case R.id.cb_good_1 : {
                        chiefPatrol = 1;
                        ((CompoundButton) findViewById(R.id.cb_bad_1)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_1)).setChecked(false);
                        break;
                    }
                    case R.id.cb_bad_1 : {
                        chiefPatrol = 3;
                        ((CompoundButton) findViewById(R.id.cb_good_1)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_1)).setChecked(false);
                        break;
                    }

                    case R.id.cb_medium_1 : {
                        chiefPatrol = 2;
                        ((CompoundButton) findViewById(R.id.cb_bad_1)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_good_1)).setChecked(false);
                        break;
                    }

                    case R.id.cb_good_2 : {
                        chiefFeedBack = 1;
                        ((CompoundButton) findViewById(R.id.cb_bad_2)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_2)).setChecked(false);
                        break;
                    }
                    case R.id.cb_bad_2 : {
                        chiefFeedBack = 3;
                        ((CompoundButton) findViewById(R.id.cb_good_2)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_2)).setChecked(false);
                        break;
                    }

                    case R.id.cb_medium_2 : {
                        chiefFeedBack = 2;
                        ((CompoundButton) findViewById(R.id.cb_bad_2)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_good_2)).setChecked(false);
                        break;
                    }

                    case R.id.cb_good_3 : {
                        chiefWork = 1;
                        ((CompoundButton) findViewById(R.id.cb_bad_3)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_3)).setChecked(false);
                        break;
                    }
                    case R.id.cb_bad_3 : {
                        chiefWork = 3;
                        ((CompoundButton) findViewById(R.id.cb_good_3)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_3)).setChecked(false);
                        break;
                    }

                    case R.id.cb_medium_3 : {
                        chiefWork = 2;
                        ((CompoundButton) findViewById(R.id.cb_bad_3)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_good_3)).setChecked(false);
                        break;
                    }

                    default:
                        break;
                }
            }

        }
    };

	/*建议表单相关 end*/

    private void setWithRiver(River r, int ix) {
        if (r != null) {
            river = r;
            segment_ix = ix;

            if (river != null) {
                if (segment_ix == -1) {
                    ((TextView) findViewById(R.id.tv_rivername)).setText(river.riverName);
                } else {
                    ((TextView) findViewById(R.id.tv_rivername)).setText(river.lowLevelRivers[segment_ix].riverName);
                }
            } else {
                showToast("没有传入河道参数");
                finish();
            }
        }
    }
    private void setWithLake(Lake l) {
        if (l != null) {
            lake = l;

            if (lake != null) {
                ((TextView) findViewById(R.id.tv_rivername)).setText(lake.lakeName);
            } else {
                showToast("没有传入湖泊参数");
                finish();
            }
        }
    }

    private Uri imageFilePath = null;
    private String uname = null;

//    private String subject = null;
    private String contentt = null;
    private String telno = null;
    private boolean anonymity = false;
    private int rid = 0;
    private long lid = 0;
    private String picbase64 = null;

    private void submmit() {
        uname = ((EditText) findViewById(R.id.et_suggest_name)).getText().toString().trim();
        //注意了，如果是人大，没有这两个编辑框。
//        subject = subject == null ? ((EditText) findViewById(R.id.et_suggest_subject)).getText().toString().trim(): subject;
//        if (getUser().isNpc() && r.riverId == getUser().getMyRiverId()) {
//            contentt = ((EditText) findViewById(R.id.et_npc_otherquestion)).getText().toString().trim();
//        } else
        contentt = ((EditText) findViewById(R.id.et_suggest_contentt)).getText().toString().trim();

        telno = ((EditText) findViewById(R.id.et_phonenum)).getText().toString().trim();

        if (uname.length() == 0) {
            showToast("姓名不能为空!");
            ((EditText) findViewById(R.id.et_suggest_name)).requestFocus();
            return;
        }

        if (eventId == 0) {
            showToast("请选择事件类别");
            return;
        }
        if(fromLakeRecord){
            if (lake == null) {
                showToast("请选择要上报的湖泊");
                return;
            }
            lid = lake.lakeId;
        }else{
            if (river == null) {
                showToast("请选择要上报的河道");
                return;
            }
            rid = segment_ix < 0 ? river.riverId : (river.lowLevelRivers[segment_ix].riverId);
        }

        anonymity = ((CheckBox) findViewById(R.id.ck_anonymity)).isChecked();
        LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_photos);
        if (ll_photos.getChildCount() > 1) {
            // 有图片
            final Bitmap[] bmps = new Bitmap[ll_photos.getChildCount() - 1];
            for (int i = 0; i < bmps.length; ++i) {
                bmps[i] = (Bitmap) ll_photos.getChildAt(i).getTag();
            }
            asynCallAndShowProcessDlg("正在处理图片...", new Callable() {

                @Override
                public void call(Object... args) {

                    StringBuffer sb = new StringBuffer();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024);
                    for (Bitmap bmp : bmps) {
                        if (sb.length() > 0)
                            sb.append(";");
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] bts = baos.toByteArray();
                        Log.i("NET", "bts.len " + bts.length);
                        sb.append(Base64.encodeToString(bts, Base64.DEFAULT));
                        baos.reset();
                    }
                    picbase64 = sb.toString();
                    // Log.i("PIC", "base64 " + picbase64);
                    Log.i("PIC", "base64.len " + picbase64.length());

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

    private void submitData() {
        showOperating();
        JSONObject p = null;
        String requestName = "";
        p = ParamUtils.freeParam(null, "content", contentt, "picBase64",picbase64 == null ? "" : picbase64, "eventId", eventId);

        //人大代表的相关参数
        try{

            p.put("chiefPatrol", chiefPatrol);
            p.put("chiefFeedBack", chiefFeedBack);
            p.put("chiefWork", chiefWork);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (location != null && ((CheckBox) findViewById(R.id.ck_gps)).isChecked()) {
            try {
                p.put("latitude", location.getLatitude());
                p.put("longtitude", location.getLongitude());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(fromLakeRecord){
            try{
                p.put("lakeId", lid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            requestName = "event_action_add_lake";
        }else {
            try{
                p.put("riverId", rid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            requestName = "event_action_add";

        }
        getRequestContext().add(requestName, new Callback<SugOrComRes>() {
            @Override
            public void callback(SugOrComRes o) {
                if (o != null && o.isSuccess()) {
                    doResult(o);
                } else {
                    hideOperating();
                }
            }
        }, SugOrComRes.class, p);
    }

    private void doResult(SugOrComRes o) {
        findViewById(R.id.sv_form).setVisibility(View.GONE);
        findViewById(R.id.ll_submit_result).setVisibility(View.VISIBLE);

        // String fid = ("T" + System.currentTimeMillis()).substring(0, 6);

        ((TextView) findViewById(R.id.tv_submit_result)).setText(StrUtils.renderText(EventReportActivity.this, R.string.fmt_event_report, o.data.comOrAdvSerNum));

        hideOperating();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Tags.CODE_ADDPHOTO && resultCode == RESULT_OK) {

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
            View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
            BitmapFactory.Options op = new BitmapFactory.Options();
            try {
                Bitmap pic = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(imageFilePath), null, op);
                view.setTag(pic);
                Log.i("NET", "" + pic.getWidth() + "*" + pic.getHeight());
                ((ImageView) view.findViewById(R.id.iv_photo)).setImageBitmap(pic);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ((LinearLayout) findViewById(R.id.ll_photos)).addView(view, ((LinearLayout) findViewById(R.id.ll_photos)).getChildCount() - 1);
        } else if (requestCode == Tags.CODE_SELECTRIVER && resultCode == RESULT_OK) {
            River r = StrUtils.Str2Obj(data.getStringExtra(Tags.TAG_RIVER), River.class);
            if (r != null) {
                readyToSugOrCom(r);
            }
        }else if (requestCode == Tags.CODE_SELECTLAKE && resultCode == RESULT_OK) {
            Lake l = StrUtils.Str2Obj(data.getStringExtra(Tags.TAG_LAKE), Lake.class);
            if (l != null) {
                setWithLake(l);
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (!getUser().gpsdisable)
            getLocation();
    }

    private void getLocation() {
        if (getLocalService() != null) {
            getLocalService().getLocation(new LocalService.LocationCallback() {
                @Override
                public void callback(Location location) {
                    EventReportActivity.this.location = location;
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getLocalService() != null) {
            // getLocalService().cl
        }
    }
}
