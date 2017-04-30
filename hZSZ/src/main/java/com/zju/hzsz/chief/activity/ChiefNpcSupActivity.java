package com.zju.hzsz.chief.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;

import com.zju.hzsz.R;
import com.zju.hzsz.fragment.npc.chief.ChiefNpcCompListFragment;
import com.zju.hzsz.fragment.npc.chief.ChiefNpcSugListFragment;

/**
 * 河长个人中心页面-代表监督
 * Created by Wangli on 2017/4/29.
 */

public class ChiefNpcSupActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    ChiefNpcCompListFragment npcCompListFragment = new ChiefNpcCompListFragment();
    ChiefNpcSugListFragment npcSugListFragment = new ChiefNpcSugListFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_npcsug);

        setTitle("代表监督");
        initHead(R.drawable.ic_head_back, 0);

        ((RadioGroup) findViewById(R.id.rg_headtab)).setOnCheckedChangeListener(this);

        //一开始显示人大的投诉界面
        replaceFragment(npcCompListFragment);

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int rdid) {
        switch (rdid) {
            case R.id.rb_head_left:
                replaceFragment(npcCompListFragment);
                break;
            case R.id.rb_head_right:
                replaceFragment(npcCompListFragment);
                break;
            default:
                break;
        }
    }

    //Fragment替换
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
            if (curFragment != null) {
                transaction.hide(curFragment);
            }
            transaction.show(newFragment);
            transaction.commit();
        }

        curFragment = newFragment;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
