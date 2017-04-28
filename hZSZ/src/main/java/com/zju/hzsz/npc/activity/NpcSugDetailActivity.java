package com.zju.hzsz.npc.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.model.DeputySupervise;
import com.zju.hzsz.utils.StrUtils;

/**
 * 人大代表-监督单详情页
 * Created by Wangli on 2017/4/26.
 */

public class NpcSugDetailActivity extends BaseActivity {

    private DeputySupervise ds = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npc_sugdetail);

        setTitle("监督单详情页");
        initHead(R.drawable.ic_head_back, 0);

        ds = StrUtils.Str2Obj(getIntent().getStringExtra("npcsug"), DeputySupervise.class);

        initView();
        initWork();

    }

    private void initView() {
        ((TextView) findViewById(R.id.tv_sernum)).setText(ds.advSerNum);
        ((TextView) findViewById(R.id.tv_supervise_name)).setText(ds.advPersonName);
        ((TextView) findViewById(R.id.tv_sup_teln)).setText(ds.advTeleNum);
        ((TextView) findViewById(R.id.tv_npc_suptime)).setText(ds.getDateTime());
        ((TextView) findViewById(R.id.tv_sup_personname)).setText(ds.dealPersonName);
        ((TextView) findViewById(R.id.tv_sup_river)).setText(ds.advRiverName);
    }

    private void initWork() {

        btnInit();

        switch (ds.chiefPatrol) {
            case 1:
                ((CompoundButton) findViewById(R.id.cb_good_1)).setChecked(true);
                break;
            case 2:
                ((CompoundButton) findViewById(R.id.cb_medium_1)).setChecked(true);
                break;
            case 3:
                ((CompoundButton) findViewById(R.id.cb_bad_1)).setChecked(true);
                break;
            default:
                ((CompoundButton) findViewById(R.id.cb_good_1)).setChecked(true);
                break;
        }


        switch (ds.chiefFeedBack) {
            case 1:
                ((CompoundButton) findViewById(R.id.cb_good_2)).setChecked(true);
                break;
            case 2:
                ((CompoundButton) findViewById(R.id.cb_medium_2)).setChecked(true);
                break;
            case 3:
                ((CompoundButton) findViewById(R.id.cb_bad_2)).setChecked(true);
                break;
            default:
                ((CompoundButton) findViewById(R.id.cb_good_2)).setChecked(true);
                break;
        }

        switch (ds.chiefWork) {
            case 1:
                ((CompoundButton) findViewById(R.id.cb_good_3)).setChecked(true);
                break;
            case 2:
                ((CompoundButton) findViewById(R.id.cb_medium_3)).setChecked(true);
                break;
            case 3:
                ((CompoundButton) findViewById(R.id.cb_bad_3)).setChecked(true);
                break;
            default:
                ((CompoundButton) findViewById(R.id.cb_good_3)).setChecked(true);
                break;
        }

        btnInitEnabled();

    }

    //为各个按钮设置不可点击
    private void btnInitEnabled() {
        int[] btnToInit = {
                R.id.cb_good_1, R.id.cb_good_2, R.id.cb_good_3,
                R.id.cb_medium_1, R.id.cb_medium_2, R.id.cb_medium_3,
                R.id.cb_bad_1, R.id.cb_bad_2, R.id.cb_bad_3
        };

        for (int id: btnToInit) {
            View v = findViewById(id);
            if (v != null)
                ((CompoundButton) v).setEnabled(false);
        }
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
                        ((CompoundButton) findViewById(R.id.cb_bad_1)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_1)).setChecked(false);
                        break;
                    }
                    case R.id.cb_bad_1 : {
                        ((CompoundButton) findViewById(R.id.cb_good_1)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_1)).setChecked(false);
                        break;
                    }

                    case R.id.cb_medium_1 : {
                        ((CompoundButton) findViewById(R.id.cb_bad_1)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_good_1)).setChecked(false);
                        break;
                    }

                    case R.id.cb_good_2 : {
                        ((CompoundButton) findViewById(R.id.cb_bad_2)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_2)).setChecked(false);
                        break;
                    }
                    case R.id.cb_bad_2 : {
                        ((CompoundButton) findViewById(R.id.cb_good_2)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_2)).setChecked(false);
                        break;
                    }

                    case R.id.cb_medium_2 : {
                        ((CompoundButton) findViewById(R.id.cb_bad_2)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_good_2)).setChecked(false);
                        break;
                    }

                    case R.id.cb_good_3 : {
                        ((CompoundButton) findViewById(R.id.cb_bad_3)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_3)).setChecked(false);
                        break;
                    }
                    case R.id.cb_bad_3 : {
                        ((CompoundButton) findViewById(R.id.cb_good_3)).setChecked(false);
                        ((CompoundButton) findViewById(R.id.cb_medium_3)).setChecked(false);
                        break;
                    }

                    case R.id.cb_medium_3 : {
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


}