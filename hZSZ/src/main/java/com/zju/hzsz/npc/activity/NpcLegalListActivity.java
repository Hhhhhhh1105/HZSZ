package com.zju.hzsz.npc.activity;

import android.os.Bundle;

import com.zju.hzsz.R;
import com.zju.hzsz.activity.BaseActivity;

/**
 * 人大代表-规范法规页面
 * Created by Wangli on 2017/4/22.
 */

public class NpcLegalListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npclegallist);

        setTitle("规范法规");
        initHead(R.drawable.ic_head_back, 0);


    }
}
