package com.zju.hzsz.npc.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.RadioGroup;

import com.zju.hzsz.R;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.fragment.PublicityListFragment;
import com.zju.hzsz.fragment.npc.NpcCompFragment;
import com.zju.hzsz.fragment.npc.NpcInfoFragment;
import com.zju.hzsz.fragment.npc.NpcRiverFragment;

/**
 * Created by Wangli on 2017/4/22.
 */

public class NpcMyjobActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    NpcInfoFragment npcInfoFragment = new NpcInfoFragment();
    NpcCompFragment npcCompFragment = new NpcCompFragment();
    NpcRiverFragment npcRiverFragment = new NpcRiverFragment();
    PublicityListFragment listFragment = new PublicityListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npc_myjob);

        setTitle("代表监督详情页");
        initHead(R.drawable.ic_head_back, 0);
        ((RadioGroup) findViewById(R.id.rg_headtab)).setOnCheckedChangeListener(this);

        replaceFragment(npcInfoFragment);

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int rdid) {
        switch (rdid) {
            case R.id.rb_head_left:
                replaceFragment(npcInfoFragment);
                break;
            case R.id.rb_head_medium:
//                replaceFragment(npcCompFragment);
                replaceFragment(listFragment);
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
