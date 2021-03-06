package com.zju.hzsz.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.AboutActivity;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.activity.CompListActivity;
import com.zju.hzsz.activity.LoginActivity;
import com.zju.hzsz.activity.MyCollectActivity;
import com.zju.hzsz.activity.QuestionActivity;
import com.zju.hzsz.activity.RiverActivity;
import com.zju.hzsz.activity.SettingActivity;
import com.zju.hzsz.chief.activity.PatrolEventListActivity;
import com.zju.hzsz.clz.RootViewWarp;
import com.zju.hzsz.model.BaseRes;
import com.zju.hzsz.model.CheckNotifyRes;
import com.zju.hzsz.model.LoginRes;
import com.zju.hzsz.model.PatrolEvent;
import com.zju.hzsz.model.River;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.npc.activity.NpcLegalListActivity;
import com.zju.hzsz.npc.activity.NpcMyjobActivity;
import com.zju.hzsz.npc.activity.NpcRankActivity;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;

/**
 * 个人中心页面
 * Created by Wangli on 2017/1/18.
 */

public class MeFragment extends BaseFragment implements View.OnClickListener{

    private int[] showWhenLogined = { R.id.tv_logout, R.id.rl_setting, R.id.rl_complaint, R.id.rl_suggestion };
    //村级河长登陆时
    private int[] showWhenChiefLogined = { R.id.rl_chief_mail, R.id.rl_chief_complaint, R.id.rl_chief_duban,
            R.id.rl_chief_notepad, R.id.rl_chief_record, R.id.rl_chief_suggestion, R.id.rl_chief_npcsug};
    private int[] showWhenDisChiefLogined = { R.id.rl_chief_rivermanage, R.id.rl_chief_record, R.id.rl_chief_notepad };
//    private int[] showWhenChiefLogined = { R.id.rl_chief_sign, R.id.rl_chief_mail, R.id.rl_chief_complaint, R.id.rl_chief_duban, R.id.rl_chief_record, R.id.rl_chief_suggestion };
    private int[] showWhenNpcLogined = { R.id.rl_npc_name, R.id.rl_npc_myriver, R.id.rl_npc_myjob, R.id.rl_npc_comment, R.id.rl_npc_legal };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (rootView == null){
            rootView = inflater.inflate(R.layout.activity_me, container, false);

            RootViewWarp warp = getRootViewWarp();
            warp.setHeadImage(0, 0);
            warp.setHeadTitle(R.string.mycenter);


            rootView.findViewById(R.id.tv_complaint).setOnClickListener(this);
            rootView.findViewById(R.id.tv_dealEvent).setOnClickListener(this);
            rootView.findViewById(R.id.tv_suggestion).setOnClickListener(this);
            rootView.findViewById(R.id.tv_collection).setOnClickListener(this);
            rootView.findViewById(R.id.tv_setting).setOnClickListener(this);
            rootView.findViewById(R.id.tv_about).setOnClickListener(this);
            rootView.findViewById(R.id.tv_question).setOnClickListener(this); //常见问题
            rootView.findViewById(R.id.tv_logout).setOnClickListener(this);
            rootView.findViewById(R.id.iv_logo).setOnClickListener(this);

            rootView.findViewById(R.id.tv_chief_complaint).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_duban).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_duban).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_rivermanage).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_notepad).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_inspect).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_record).setOnClickListener(this);
            rootView.findViewById(R.id.tv_lake_chief_record).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_suggestion).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_sign).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_mail).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_npcsug).setOnClickListener(this);  //代表监督

            rootView.findViewById(R.id.tv_npc_legal).setOnClickListener(this);  //规范法规
            rootView.findViewById(R.id.tv_npc_myriver).setOnClickListener(this);  //我的河道
            rootView.findViewById(R.id.tv_npc_myjob).setOnClickListener(this);  //我的履职
            rootView.findViewById(R.id.tv_npc_comment).setOnClickListener(this);  //履职评价

        }

        if ( getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isNpc()) {
            changeNpcLogo();
            ((TextView) rootView.findViewById(R.id.tv_complaint)).setText("其他投诉");
            ((TextView) rootView.findViewById(R.id.tv_suggestion)).setText("其他建议");
        } else {
            ((TextView) rootView.findViewById(R.id.tv_complaint)).setText("我的投诉");
            ((TextView) rootView.findViewById(R.id.tv_suggestion)).setText("我的建议");
        }

        return rootView;
    }

    private void checkNotify() {
        getRequestContext().add("Get_Notify", new Callback<CheckNotifyRes>() {
            @Override
            public void callback(CheckNotifyRes o) {
                if (o != null && o.isSuccess()) {
                    // o.data.sumUndealComp = 10;
                    // o.data.sumUndealAdv = 5;
                    getBaseActivity().nofityChecked(o.data);

                    //投诉信息提醒
                    if (o.data.sumUndealComp > 0) {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlecomplaint_count)).setText(o.data.sumUndealComp + "个投诉未处理");
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.GONE);
                    }

                    //代表投诉提醒
                    if (o.data.sumUnDealDeputyComp > 0) {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandle_npccomplaint_count)).setText(o.data.sumUnDealDeputyComp + "个代表投诉未处理");
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandle_npccomplaint_count)).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandle_npccomplaint_count)).setVisibility(View.GONE);
                    }

                    if (o.data.sumUndealAdv > 0) {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlesuggestion_count)).setText(o.data.sumUndealAdv + "个建议未处理");
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.GONE);
                    }

                    if (o.data.sumUnReadMail > 0) {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unreadmail_count)).setText(o.data.sumUnReadMail + "条未读消息");
                        ((TextView) rootView.findViewById(R.id.tv_chief_unreadmail_count)).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unreadmail_count)).setVisibility(View.GONE);
                    }
                }
            }
        }, CheckNotifyRes.class, new JSONObject());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();

        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.GONE);
        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.GONE);
        if (getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isChief()) {
            checkNotify();
        }
    }

    private void refreshView() {
        boolean logined = getBaseActivity().getUser().isLogined();
        boolean ischief = getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isChief();
        //判断是否是村级河长 8
        boolean isVillageChief = getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isVillageChief();
        //判断是否是市级或区级河长 区级9 市级10
        boolean isDistrictChief = logined && getBaseActivity().getUser().isDistrictChief();
        boolean isCityChief = logined && getBaseActivity().getUser().isCityChief();
        boolean isNpc = logined && getBaseActivity().getUser().isNpc();
        //是否是湖长
        boolean isLakeChief = getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isLakeChief();
        //管辖河道小bug - 1.4.30
        rootView.findViewById(R.id.rl_chief_rivermanage).setVisibility(View.GONE);

        for (int id : showWhenLogined) {
            View v = rootView.findViewById(id);
            if (v != null)
                v.setVisibility(logined ? View.VISIBLE : View.GONE);
        }


        for (int id : showWhenChiefLogined) {
            View v = rootView.findViewById(id);
            if (v != null)
                v.setVisibility(ischief ? View.VISIBLE : View.GONE);
        }

        for (int id: showWhenNpcLogined) {
            View v = rootView.findViewById(id);
            if (v != null)
                v.setVisibility(isNpc ? View.VISIBLE : View.GONE);
        }

        //如果是村级河长，则显示巡河的界面
        if (isVillageChief){
            View v = rootView.findViewById(R.id.rl_chief_record);
            v.setVisibility(View.VISIBLE);
        }

        if (isCityChief || isDistrictChief) {
            for (int id : showWhenDisChiefLogined) {
                View v =rootView.findViewById(id);
                v.setVisibility(View.VISIBLE);
            }
        }
        //如果是湖长，显示巡湖功能
        if(isLakeChief){
            rootView.findViewById(R.id.rl_lake_chief_record).setVisibility(View.VISIBLE);
        }else {
            rootView.findViewById(R.id.rl_lake_chief_record).setVisibility(View.GONE);
        }
        //各级河长或湖长可以问题上报、问题处理
        if (ischief||isCityChief||isVillageChief||isDistrictChief||isLakeChief){
            rootView.findViewById(R.id.rl_dealEvent).setVisibility(View.VISIBLE);

        }else {
            rootView.findViewById(R.id.rl_dealEvent).setVisibility(View.GONE);
        }
        //如果是人大代表账号
        if (isNpc) {
            //更改页面名为“代表监督”
            getRootViewWarp().setHeadTitle("代表监督");
            //更改底端tab栏为“代表监督”
            ((RadioButton) getBaseActivity().findViewById(R.id.rd_panhang)).setText("代表监督");
            //更改河长条目的信息
            ((TextView) getRootViewWarp().getViewById(R.id.tv_npc_name))
                    .setText(ResUtils.getNpcTitle(getBaseActivity().getUser().getAuthority()) + "："
                            + getBaseActivity().getUser().realName);
        } else {
            getRootViewWarp().setHeadTitle("个人中心");
            //更改底端tab栏为“个人中心”
            ((RadioButton) getBaseActivity().findViewById(R.id.rd_panhang)).setText("个人中心");

        }

        //如果是区级河长，需要显示下级河长的投诉与巡河情况


//		if(ischief){
//			rootView.findViewById()(R.id.rl_complaint).setVisibility(View.GONE);
//			rootView.findViewById()(R.id.rl_suggestion).setVisibility(View.GONE);
//		}


        ((TextView) rootView.findViewById(R.id.tv_name)).setText(getBaseActivity().getUser().getDisplayName());
        ((TextView) rootView.findViewById(R.id.tv_info)).setText(getBaseActivity().getUser().getDisplayRiver());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_chief_sign: {
                startActivity(new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefSignActivity.class));
                break;
            }
            case R.id.tv_chief_mail: {
                startActivity(new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefMailListActivity.class));
                break;
            }
            case R.id.tv_chief_complaint: {
                startActivity(new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefCompListActivity.class));
                break;
            }
            case R.id.tv_chief_suggestion: {
                Intent intent = new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefCompListActivity.class);
                intent.putExtra(Tags.TAG_ABOOLEAN, false);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_inspect:{
                Intent intent = new Intent( getBaseActivity(),com.zju.hzsz.chief.activity.ChiefInspectActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_record: {
                Intent intent = new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefRecordListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_lake_chief_record: {
                Intent intent = new Intent(getBaseActivity(), com.zju.hzsz.lakechief.activity.LakeChiefRecordListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_rivermanage: {
                Intent intent = new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefRivermanageActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_npcsug: {
                Intent intent = new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefNpcSupActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_duban: {
                Intent intent = new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefDubanListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_notepad: {
                Intent intent = new Intent(getBaseActivity(), com.zju.hzsz.chief.activity.ChiefNotepadActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_npc_legal: {
                startActivity(new Intent(getBaseActivity(), NpcLegalListActivity.class));
                break;
            }
            case R.id.tv_npc_myriver: {
                River river = new River();
                river.riverId = getBaseActivity().getUser().getMyRiverId();
                river.riverName = getBaseActivity().getUser().riverSum[0].riverName;
                Intent intent = new Intent(getBaseActivity(), RiverActivity.class);
                intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(river));
                startActivity(intent);
                break;
            }
            case R.id.tv_npc_myjob: {
                Intent intent = new Intent(getBaseActivity(), NpcMyjobActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_npc_comment: {
                Intent intent = new Intent(getBaseActivity(), NpcRankActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_complaint: {
                startActivity(new Intent(getBaseActivity(), CompListActivity.class));
                break;
            }
            case R.id.tv_dealEvent:{
                Log.d("ss","ssss");
                startActivity(new Intent(getBaseActivity(), PatrolEventListActivity.class));
                break;
            }
            case R.id.tv_suggestion: {
                Intent intent = new Intent(getBaseActivity(), CompListActivity.class);
                intent.putExtra(Tags.TAG_ABOOLEAN, false);
                startActivity(intent);
                break;
            }
            case R.id.tv_collection: {
                startActivity(new Intent(getBaseActivity(), MyCollectActivity.class));
                break;
            }
            case R.id.tv_about: {
                startActivity(new Intent(getBaseActivity(), AboutActivity.class));
                break;
            }
            case R.id.tv_question: {
                startActivity(new Intent(getBaseActivity(), QuestionActivity.class));
                break;
            }
            case R.id.tv_setting: {
                startActivity(new Intent(getBaseActivity(), SettingActivity.class));
                break;
            }
            case R.id.iv_logo: {
                if (!getBaseActivity().getUser().isLogined()) {
                    Intent intent = new Intent(getBaseActivity(), LoginActivity.class);
                    startActivityForResult(intent, Tags.CODE_LOGIN);
                }
                break;
            }
            case R.id.tv_logout:
                getBaseActivity().createMessageDialog("提示", "确定要注销登录吗?", "确定", "取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        logout();
                    }
                }, null, null).show();
                break;
            default:
                getBaseActivity().onClick(v);
                break;
        }
    }

    private void logout() {
        showOperating();

        //更换logo
        rootView.findViewById(R.id.iv_logo).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.tv_name).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.tv_info).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.shape_radius).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.npc_logo).setVisibility(View.INVISIBLE);
        //更换"我的投诉"、"我的建议"
        ((TextView) rootView.findViewById(R.id.tv_complaint)).setText("我的投诉");
        ((TextView) rootView.findViewById(R.id.tv_suggestion)).setText("我的建议");

        getRequestContext().add("User_Logout_Action", new Callback<BaseRes>() {
            @Override
            public void callback(BaseRes o) {
                hideOperating();
                // if (o != null && o.isSuccess()) {
                getBaseActivity().getUser().clearInfo();
                refreshView();
                hideOperating();
                // }
            }
        }, BaseRes.class, ParamUtils.freeParam(null, "cid", getBaseActivity().getLocalService() != null ? getBaseActivity().getLocalService().getCid() : ""));
    }

    private void changeNpcLogo() {
        //更换logo
        rootView.findViewById(R.id.iv_logo).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.tv_name).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.tv_info).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.shape_radius).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.npc_logo).setVisibility(View.VISIBLE);
        //更换"其他投诉"、"其他建议"
        ((TextView) rootView.findViewById(R.id.tv_complaint)).setText("其他投诉");
        ((TextView) rootView.findViewById(R.id.tv_suggestion)).setText("其他建议");

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            //Fragment隐藏时调用
        }else {
            //Fragment显示时调用
            String username=getBaseActivity().getUser().userName;
            String password=getBaseActivity().getUser().pwdmd5;
            if (getBaseActivity().getUser().isLogined()){
                loginForRefresh(username,password);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isNpc()) {
            changeNpcLogo();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void loginForRefresh(String username,String password){

        showOperating();
        getBaseActivity().getUser().uuid = null;
        getRequestContext().add("action_user_load", new Callback<LoginRes>() {
            @Override
            public void callback(LoginRes o) {
                hideOperating();
                if (o != null && o.isSuccess()) {

                    getBaseActivity().getUser().uuid = o.data.uuid;
                    getBaseActivity().getUser().authority = o.data.authority;
                    getBaseActivity().getUser().riverSum = o.data.riverSum;
                    getBaseActivity().getUser().lakeSum = o.data.lakeSum;
                    getBaseActivity().getUser().isLakeChief = o.data.isLakeChief;
                    getBaseActivity().getUser().ifOnJob = o.data.ifOnJob;
                    getBaseActivity().getUser().districtId = o.data.districtId;
                    getBaseActivity().getUser().realName = o.data.realName;




                }
            }
        }, LoginRes.class, ParamUtils.freeParam(null, "userName", username, "password", password, "cid", getBaseActivity().getLocalService() != null ? getBaseActivity().getLocalService().getCid() : ""));
    }
}
