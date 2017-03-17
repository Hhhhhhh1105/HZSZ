package com.zju.hzsz.chief.activity;

import android.os.Bundle;

import com.zju.hzsz.R;

/**
 * Created by Wangli on 2017/3/8.
 */

public class ChiefEditNoteActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_editnote);
        setTitle("编辑备忘");
        initHead(R.drawable.ic_head_back, 0);


    }
}
