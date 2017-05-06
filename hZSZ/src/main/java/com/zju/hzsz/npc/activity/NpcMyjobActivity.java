package com.zju.hzsz.npc.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.activity.SugOrComtActivity;
import com.zju.hzsz.chief.activity.ChiefEditRecordActivity;
import com.zju.hzsz.fragment.PublicityListFragment;
import com.zju.hzsz.fragment.npc.NpcCompFragment;
import com.zju.hzsz.fragment.npc.NpcRiverFragment;
import com.zju.hzsz.fragment.npc.NpcSugFragment;
import com.zju.hzsz.model.River;
import com.zju.hzsz.utils.StrUtils;

/**
 * 人大代表-我的履职
 * Created by Wangli on 2017/4/22.
 */

public class NpcMyjobActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    NpcSugFragment npcSugFragment = new NpcSugFragment();    //监督河长
    NpcCompFragment npcCompFragment = new NpcCompFragment();   //投诉举报
    NpcRiverFragment npcRiverFragment = new NpcRiverFragment();   //巡河记录
    PublicityListFragment listFragment = new PublicityListFragment();



    private View.OnClickListener comclik = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            River river = new River();
            river.riverId = getUser().getMyRiverId();
            river.riverName = getUser().riverSum[0].riverName;
            Intent intent = new Intent(NpcMyjobActivity.this, SugOrComtActivity.class);
            intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(river));
            intent.putExtra(Tags.TAG_INDEX, -1);
            intent.putExtra(Tags.TAG_ABOOLEAN, true);
            startActivity(intent);
        }
    };

    private View.OnClickListener sugClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            River river = new River();
            river.riverId = getUser().getMyRiverId();
            river.riverName = getUser().riverSum[0].riverName;
            Intent intent = new Intent(NpcMyjobActivity.this, SugOrComtActivity.class);
            intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(river));
            intent.putExtra(Tags.TAG_INDEX, -1);
            intent.putExtra(Tags.TAG_ABOOLEAN, false);
            startActivity(intent);
        }
    };

    private View.OnClickListener trackClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(NpcMyjobActivity.this, ChiefEditRecordActivity.class);
            startActivityForResult(intent, Tags.CODE_NEW);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npc_myjob);

        setTitle("我的履职");
        initHead(R.drawable.ic_head_back, 0);
        ((RadioGroup) findViewById(R.id.rg_headtab)).setOnCheckedChangeListener(this);


        findViewById(R.id.btn_comp).setOnClickListener(comclik);
        findViewById(R.id.btn_sug).setOnClickListener(sugClick);
        findViewById(R.id.btn_track).setOnClickListener(trackClick);


        replaceFragment(npcRiverFragment);

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int rdid) {
        switch (rdid) {
            case R.id.rb_head_left:
                replaceFragment(npcCompFragment);
                break;
            case R.id.rb_head_medium:
//                replaceFragment(npcCompFragment);
                replaceFragment(npcSugFragment);
                break;
            case R.id.rb_head_right:
                replaceFragment(npcRiverFragment);
                break;
            default:
                break;
        }
    }

    Fragment curFragment = null;
    private void replaceFragment(Fragment newFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (!newFragment.isAdded()) {
            if (curFragment == null) {
                transaction.replace(R.id.fl_fragment_container, newFragment).commit();
            } else {
                transaction.hide(curFragment).add(R.id.fl_fragment_container, newFragment).commit();
            }
        } else {
            if (curFragment != null)
                transaction.hide(curFragment);
            transaction.show(newFragment);
            transaction.commit();
        }
        curFragment = newFragment;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ((RadioButton) findViewById(R.id.rb_head_right)).setChecked(true);
        }
    }
}
