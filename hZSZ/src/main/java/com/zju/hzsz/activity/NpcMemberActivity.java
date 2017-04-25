package com.zju.hzsz.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.fragment.PublicityListFragment;
import com.zju.hzsz.fragment.npc.NpcCompFragment;
import com.zju.hzsz.fragment.npc.NpcRiverFragment;
import com.zju.hzsz.fragment.npc.NpcSugFragment;
import com.zju.hzsz.model.Npc;
import com.zju.hzsz.model.River;
import com.zju.hzsz.utils.StrUtils;

/**
 * 人大代表监督详情页
 * Created by Wangli on 2017/4/19.
 */

public class NpcMemberActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener{

    NpcSugFragment npcSugFragment = new NpcSugFragment();
    NpcCompFragment npcCompFragment = new NpcCompFragment();
    NpcRiverFragment npcRiverFragment = new NpcRiverFragment();
    PublicityListFragment listFragment = new PublicityListFragment();
    Npc npc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npcmember);

        setTitle("代表监督详情页");
        initHead(R.drawable.ic_head_back, 0);
        ((RadioGroup) findViewById(R.id.rg_headtab)).setOnCheckedChangeListener(this);

        npc = StrUtils.Str2Obj(getIntent().getExtras().getString(Tags.TAG_NPC), Npc.class);
        //设置人大代表的基本信息
        ((TextView) findViewById(R.id.tv_npc_name)).setText(npc.name);
        ((TextView) findViewById(R.id.tv_npc_title)).setText(npc.position);
        ((TextView) findViewById(R.id.tv_npc_mobilephone)).setText(npc.mobilephone);
        ((TextView) findViewById(R.id.tv_npc_district)).setText(npc.districtName);

        ((TextView) findViewById(R.id.tv_npc_river)).setText(npc.riverName);
        findViewById(R.id.tv_npc_river).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                River river = new River();
                river.riverId = npc.riverId;
                river.riverName = npc.riverName;
                Intent intent = new Intent(NpcMemberActivity.this, RiverActivity.class);
                intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(river));
                startActivity(intent);
            }
        });

        replaceFragment(npcCompFragment);

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
}
