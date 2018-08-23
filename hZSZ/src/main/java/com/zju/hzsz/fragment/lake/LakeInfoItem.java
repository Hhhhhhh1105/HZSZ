package com.zju.hzsz.fragment.lake;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.activity.LakeActivity;
import com.zju.hzsz.activity.LakePositionActivity;
import com.zju.hzsz.activity.SugOrComtActivity;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.Lake;
import com.zju.hzsz.model.LakeChief;
import com.zju.hzsz.model.LakeDataRes;
import com.zju.hzsz.model.LowLevelLake;

import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.DipPxUtils;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.ObjUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ZJTLM4600l on 2017/12/20.
 */

public class LakeInfoItem extends BaseLakePagerItem {
    private BaseActivity.BooleanCallback careCkb = null;
    private static final String TAG = "hhhhhh";

    public LakeInfoItem(Lake lake, BaseActivity context, BaseActivity.BooleanCallback careCkb) {
        super(lake, context);
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

    //注释 投诉相关
//    private BaseActivity.LoginCallback cbkTosug = new BaseActivity.LoginCallback() {
//        @Override
//        public void loginCallback(boolean logined) {
//            if (logined) {
//                readyToSugOrCom(false);
//            }
//        }
//    };
//
//    private View.OnClickListener sugclik = new View.OnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//            context.setLoginCallback(cbkTosug);
//            if (context.checkUserAndLogin("请到个人中心进行注册或登录，使用个人账号登录后再进行建议。")) {
//                readyToSugOrCom(false);
//            }
//        }
//    };
//
//    private BaseActivity.LoginCallback cbkTocom = new BaseActivity.LoginCallback() {
//        @Override
//        public void loginCallback(boolean logined) {
//            if (logined) {
//                readyToSugOrCom(true);
//            }
//        }
//    };
//
//    private View.OnClickListener comclik = new View.OnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//            context.setLoginCallback(cbkTocom);
//            if (context.checkUserAndLogin("请到个人中心进行注册或登录，使用个人账号登录后再进行投诉。")) {
//                readyToSugOrCom(true);
//            }
//        }
//    };
//下级湖泊
    private View.OnClickListener lowLeverLakeClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getTag() != null) {
                Intent intent = new Intent(context, LakeActivity.class);
                intent.putExtra(Tags.TAG_LAKE, StrUtils.Obj2Str(v.getTag()));
                context.startActivity(intent);
            }
        }
    };

    /**
     * 点击投诉或建议之后，弹出窗口
     * @param isCom：建议为false，投诉为true
     *             暂时不可投诉
     */
//    private void readyToSugOrCom(final boolean isCom) {
//        if (lake.isPiecewise()) {
//            String[] names = new String[river.townRiverChiefs.length];
//            for (int i = 0; i < names.length; ++i) {
//                names[i] = river.townRiverChiefs[i].townRiverName;
//            }
//            Dialog alertDialog = new AlertDialog.Builder(context).setTitle(R.string.river_select_segment).setItems(names, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    startToWithRiverSegmtntIx(which, isCom);
//                }
//            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                }
//            }).create();
//            alertDialog.show();
//        } else {
//            startToWithRiverSegmtntIx(-1, isCom);
//        }
//    }

    /**
     * 跳转至投诉界面
     //* @param ix 第几条河
     //* @param isCom 建议or投诉
     *              暂时不可投诉
     */
//    private void startToWithRiverSegmtntIx(int ix, boolean isCom) {
//        Intent intent = new Intent(context, SugOrComtActivity.class);
//        intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(river));
//        intent.putExtra(Tags.TAG_INDEX, ix);
//        intent.putExtra(Tags.TAG_ABOOLEAN, isCom);
//        context.startActivity(intent);
//    }

    @Override
    public View getView() {
        if (view == null) {
            view = LinearLayout.inflate(context, R.layout.confs_lake_info, null);

            view.findViewById(R.id.iv_suggestion).setVisibility(View.GONE);
            view.findViewById(R.id.iv_complaint).setVisibility(View.GONE);
//注释投诉相关
//            view.findViewById(R.id.iv_complaint).setOnClickListener(comclik);
//            view.findViewById(R.id.iv_suggestion).setOnClickListener(sugclik);

            view.findViewById(R.id.tv_goposition).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //方位！！！！！！！
                    if (lake != null) {
                        Intent intent = new Intent(context, LakePositionActivity.class);
                        intent.putExtra(Tags.TAG_LAKE, StrUtils.Obj2Str(lake));
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
            p.put("lakeId", lake.lakeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRefreshing(true);
        context.getRequestContext().add("Get_OneLake_Data", new Callback<LakeDataRes>() {

            @Override
            public void callback(LakeDataRes o) {
                if (o != null && o.isSuccess()) {
                    ObjUtils.mergeObj(lake, o.data);//magic-riverLevel自动会减一，好奇怪
//                    lake.lakeLevel = o.data.lakeLevel;
                    lake.deep = o.data.deep;
                    try {
                        refreshView();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                setRefreshing(false);
            }
        }, LakeDataRes.class, p);
    }

    /**
     * 接收到返回信息后刷新显示
     */
    private void refreshView() {

        if (lake != null) {
            //湖泊基本信息
            ViewWarp warp = new ViewWarp(view, context);
            warp.setText(R.id.tv_river_name, lake.lakeName);//河与湖复用的文本框
            warp.setText(R.id.tv_lake_code, lake.lakeSerialNum);
            warp.setText(R.id.tv_lake_owner, lake.districtName);
            warp.setText(R.id.tv_lake_main_function, lake.mianFunction);
            warp.setText(R.id.tv_lake_level, ResUtils.getRiverSLittleLevel(lake.lakeLevel));
            warp.setText(R.id.tv_lake_size, lake.lakeSize+" km^2");
            warp.setText(R.id.tv_lake_depth, lake.deep+" m");
            warp.setText(R.id.tv_lake_capacity, lake.capacity+" 万m^3");
//            warp.setText(R.id.tv_lake_size, StrUtils.renderText(context, R.string.fmt_size_km2, StrUtils.floatS2Str(lake.lakeSize)));
//            warp.setText(R.id.tv_lake_depth, StrUtils.renderText(context, R.string.fmt_depth_m, StrUtils.floatS2Str("2.50")));
//            warp.setText(R.id.tv_lake_capacity, StrUtils.renderText(context, R.string.fmt_capacity_m3, StrUtils.floatS2Str(lake.capacity)));

            if(lake.lakeOrReservoir == 2){
                warp.getViewById(R.id.textViewDepth).setVisibility(View.GONE);
                warp.getViewById(R.id.tv_lake_depth).setVisibility(View.GONE);
            }

            //去掉“投诉”和“公示”两个按钮
            warp.getViewById(R.id.iv_complaint).setVisibility(View.INVISIBLE);
            warp.getViewById(R.id.iv_suggestion).setVisibility(View.INVISIBLE);

//            warp.setImage(R.id.iv_love, lake.iCare(context.getUser()) ? R.drawable.ic_loved : R.drawable.ic_love);
            warp.getViewById(R.id.iv_love).setVisibility(View.GONE);
            warp.getViewById(R.id.tv_love).setVisibility(View.GONE);

            View.OnClickListener clk = new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

//                    river.toggleCare(context, new BaseActivity.BooleanCallback() {
//                        @Override
//                        public void callback(boolean b) {
//                            (new ViewWarp(view, context)).setImage(R.id.iv_love, river.isCared(context.getUser()) ? R.drawable.ic_loved : R.drawable.ic_love);
//                            if (careCkb != null) {
//                                careCkb.callback(river.isCared(context.getUser()));
//                            }
//                        }
//                    });

                }
            };

            //iv和tv
            warp.getViewById(R.id.iv_love).setOnClickListener(clk);
            warp.getViewById(R.id.tv_love).setOnClickListener(clk);

            String imgurl = StrUtils.getImgUrl(lake.lakePicPath);
            ImgUtils.loadImage(context, ((ImageView) view.findViewById(R.id.iv_picture)), imgurl, R.drawable.im_riverbox, R.drawable.im_riverbox);

            //联系人部分
            final LinearLayout ll_contacts = (LinearLayout) warp.getViewById(R.id.ll_contacts);
            ll_contacts.removeAllViews();

//            //下级河道部分
            final LinearLayout ll_lakes = (LinearLayout) warp.getViewById(R.id.ll_lakes);
            ll_lakes.removeAllViews();
//
//            //人大监督员部分
//            final LinearLayout ll_npcs = (LinearLayout) warp.getViewById(R.id.ll_npcs);
//            ll_npcs.removeAllViews();

            LinearLayout row;
            //湖泊相关信息
            if(lake.cityLakeChiefJsons!=null){//市级湖长
                for (LakeChief lakeChief:lake.cityLakeChiefJsons){
                    row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
//                    row.addView(initContItem(R.string.city_lake_chief_name, lakeChief.getChiefName() , lakeChief.getContactWay() != null ? lakeChief.getContactWay() : null, false));
                    row.addView(initContItem(R.string.city_lake_chief_name, lakeChief.getChiefName() , null, false));
                    row.addView(initContItem(R.string.cityriverchief_responsibility, lakeChief.getDepartment(), null, false));
                    ll_contacts.addView(row);
                }
            }
            if(lake.cityViceLakeChiefJsons!=null){//市级副湖长
                for (LakeChief lakeChief:lake.cityViceLakeChiefJsons){
                    row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
//                    row.addView(initContItem(R.string.vicecity_lake_chief_name, lakeChief.getChiefName() , lakeChief.getContactWay() != null ? lakeChief.getContactWay() : null, false));
                    row.addView(initContItem(R.string.vicecity_lake_chief_name, lakeChief.getChiefName() , null, false));
                    row.addView(initContItem(R.string.cityriverchief_responsibility, lakeChief.getDepartment(), null, false));
                    ll_contacts.addView(row);
                }
            }

            if(lake.cityConnectLakeChiefJsons!=null){//市级湖长联系人
                for (LakeChief lakeChief:lake.cityConnectLakeChiefJsons){
                    row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.addView(initContItem(R.string.city_lake_connectchief_name, lakeChief.getChiefName() , lakeChief.getContactWay() != null ? lakeChief.getContactWay() : null, false));
                    row.addView(initContItem(R.string.cityriverchief_responsibility, lakeChief.getDepartment(), null, false));
                    ll_contacts.addView(row);
                }
            }
            if(lake.districtLakeChiefJsons!=null){//区级湖长
                for (LakeChief lakeChief:lake.districtLakeChiefJsons){
                    row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
//                    row.addView(initContItem(R.string.district_lake_chief_name, lakeChief.getChiefName() , lakeChief.getContactWay() != null ? lakeChief.getContactWay() : null, false));
                    row.addView(initContItem(R.string.district_lake_chief_name, lakeChief.getChiefName() , null, false));
                    row.addView(initContItem(R.string.cityriverchief_responsibility, lakeChief.getDepartment(), null, false));
                    ll_contacts.addView(row);
                }
            }
            if(lake.districtConnectLakeChiefJsons!=null){//区级湖长联系人
                for (LakeChief lakeChief:lake.districtConnectLakeChiefJsons){
                    row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.addView(initContItem(R.string.district_lake_connectchief_name, lakeChief.getChiefName() , lakeChief.getContactWay() != null ? lakeChief.getContactWay() : null, false));
                    row.addView(initContItem(R.string.cityriverchief_responsibility, lakeChief.getDepartment(), null, false));
                    ll_contacts.addView(row);
                }
            }
            if(lake.townLakeChiefJsons!=null){//镇街湖长
                for (LakeChief lakeChief:lake.townLakeChiefJsons){
                    row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.addView(initContItem(R.string.town_lake_chief_name, lakeChief.getChiefName() , lakeChief.getContactWay() != null ? lakeChief.getContactWay() : null, false));
                    row.addView(initContItem(R.string.cityriverchief_responsibility, lakeChief.getDepartment(), null, false));
                    ll_contacts.addView(row);
                }
            }
            if(lake.villageLakeChiefJsons!=null){//村级湖长
                for (LakeChief lakeChief:lake.villageLakeChiefJsons){
                    row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.addView(initContItem(R.string.village_lake_chief_name, lakeChief.getChiefName() , lakeChief.getContactWay() != null ? lakeChief.getContactWay() : null, false));
                    row.addView(initContItem(R.string.cityriverchief_responsibility, lakeChief.getDepartment(), null, false));
                    ll_contacts.addView(row);
                }
            }
//            if(lake.lakePoliceMan!=null){//湖泊警长
//                row = new LinearLayout(context);
//                row.setOrientation(LinearLayout.HORIZONTAL);
//                row.addView(initContItem(R.string.lake_police_name, lake.lakePoliceMan.getChiefName() , lake.lakePoliceMan.getContactWay() != null ? lake.lakePoliceMan.getContactWay() : null, false));
//                row.addView(initContItem(R.string.department, lake.lakePoliceMan.getDepartment(), null, false));
//                ll_contacts.addView(row);
//            }
            //湖泊不显示联系部门和联系人
//            if(lake.lakeOrReservoir ==1 || lake.lakeLevel != 4){
//                //联系部门+联系人
//                row = new LinearLayout(context);
//                row.addView(initContItem(R.string.river_contdep, lake.department, null, false));
//                row.addView(initContItem(R.string.river_contpe, lake.lakeContactUser, lake.departmentPhone, false));
//                ll_contacts.addView(row);
//            }

//            //增加统一监督电话
//            View supervision_phone = LinearLayout.inflate(context, R.layout.item_river_contact_line, null);
//            ((TextView) (supervision_phone.findViewById(R.id.tv_title_name))).setText("统一监督电话");
//            ((TextView) (supervision_phone.findViewById(R.id.tv_river_name))).setText("18883869560");
//
//            LinearLayout row_superPhone = new LinearLayout(context);
//            row_superPhone.setOrientation(LinearLayout.HORIZONTAL);
//            row_superPhone.addView(initContItem(R.string.river_supervisePhone, lake.unifiedTelephone, null, false));
            row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(initContItem(R.string.river_supervisePhone, lake.unifiedTelephone, null, false));
            ll_contacts.addView(row);
            //下级湖泊
            if (lake.lowLevelLakes.length > 0) {
                for (int i = 0; i < lake.lowLevelLakes.length; i += 2) {
                    LowLevelLake lowLevelLake_l = lake.lowLevelLakes[i];
                    LowLevelLake lowLevelLake_r = (i + 1) < lake.lowLevelLakes.length ? lake.lowLevelLakes[i + 1] : null;


                    View view = LinearLayout.inflate(context, R.layout.item_mainpage_section, null);
                    ((TextView) view.findViewById(R.id.tv_name_l)).setText(lowLevelLake_l.lakeName);
                    Log.d(TAG, "refreshView: "+lowLevelLake_l.lakeName);
                    ((TextView) view.findViewById(R.id.tv_level_l)).setText(ResUtils.getRiverSLittleLevel(lowLevelLake_l.lakeLevel));
                    ((ImageView) view.findViewById(R.id.iv_quality_l)).setImageResource(R.drawable.arrow_right);

                    view.findViewById(R.id.rl_section_left).setOnClickListener(lowLeverLakeClick);
                    view.findViewById(R.id.rl_section_left).setTag(lowLevelLake_l);

                    if (lowLevelLake_r != null) {

                        ((TextView) view.findViewById(R.id.tv_name_r)).setText(lowLevelLake_r.lakeName);
                        Log.d(TAG, "refreshView: "+lowLevelLake_r.lakeName);
                        ((TextView) view.findViewById(R.id.tv_level_r)).setText(ResUtils.getRiverSLittleLevel(lowLevelLake_r.lakeLevel));
                        ((ImageView) view.findViewById(R.id.iv_quality_r)).setImageResource(R.drawable.arrow_right);

                        view.findViewById(R.id.rl_section_right).setOnClickListener(lowLeverLakeClick);
                        view.findViewById(R.id.rl_section_right).setTag(lowLevelLake_r);

                    } else {
                        view.findViewById(R.id.rl_section_right).setVisibility(View.INVISIBLE);
                    }

                    ll_lakes.addView(view);

                }
            } else {
                //若无下级湖泊则不显示下级湖泊
                warp.getViewById(R.id.tv_low_level_lake).setVisibility(View.GONE);
                //显示不分级的镇街河长及联系方式
//                row = new LinearLayout(context);
//                row.addView(initContItem(R.string.river_zhenhezhang, river.townRiverChiefs[0].chiefName, river.townRiverChiefs[0].contactWay, false));
//                row.addView(initContItem(R.string.river_jingzhang, null, null, true));
//                ll_contacts.addView(row);
            }

//            //镇街级河道
//            if(river.riverLevel == 4) {
//
//                //镇街河长+河道警长
//                LinearLayout row = new LinearLayout(context);
//                row.setOrientation(LinearLayout.HORIZONTAL);
//                row.addView(initContItem(R.string.river_zhenhezhang, river.townRiverChiefs[0].chiefName, river.townRiverChiefs[0].contactWay, false));
//                row.addView(initContItem(R.string.river_jingzhang, river.townRiverSheriffs.length > 0 ? river.townRiverSheriffs[0].chiefName : null, river.townRiverSheriffs.length > 0 ? river.townRiverSheriffs[0].contactWay : null, false));
//                ll_contacts.addView(row);
//
//                //河长职务
//                row = new LinearLayout(context);
//                row.setOrientation(LinearLayout.HORIZONTAL);
//                row.addView(initContItem(R.string.riverchief_responsibility, river.townRiverChiefs[0].department, null, false));
//                ll_contacts.addView(row);
//
//                //联系部门 联系人
//                if (river.comtactDepartment != null) {
//                    row = new LinearLayout(context);
//                    row.setOrientation(LinearLayout.HORIZONTAL);
//                    row.addView(initContItem(R.string.river_contdep, river.comtactDepartment.department, null, false));
//                    row.addView(initContItem(R.string.river_contpe, river.comtactDepartment.river_contact_user, river.comtactDepartment.department_phone, false));
//                }
//                ll_contacts.addView(row);
//
////                warp.getViewById(R.id.tv_low_level_river).setVisibility(View.GONE);
////湖泊无下属湖长
////                //镇街级河道有下属村级河长 按分段河长方式显示
////                if (river.villageRiverChiefs.length > 0) {
////
////                    warp.getViewById(R.id.tv_low_level_river).setVisibility(View.VISIBLE);
////                    ((TextView) warp.getViewById(R.id.tv_low_level_river)).setText(R.string.river_villagechief);
////
////                    for (int i = 0; i < river.villageRiverChiefs.length; i++) {
////
////                        LinearLayout row_village = new LinearLayout(context);
////                        row_village.setOrientation(LinearLayout.HORIZONTAL);
////
////                        row_village.addView(initContItem(R.string.river_villagechief_title, river.villageRiverChiefs[i].chiefName, river.villageRiverChiefs[i].contactWay, false));
////                        row_village.addView(initContItem(R.string.river_villagechief_position, river.villageRiverChiefs[i].position, null, false));
////
////                        ll_rivers.addView(row_village);
////                    }
////
////                } else {
////                    warp.getViewById(R.id.tv_low_level_river).setVisibility(View.GONE);
////                }
//
//            }

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
