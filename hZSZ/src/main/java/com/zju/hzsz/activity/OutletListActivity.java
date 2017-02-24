package com.zju.hzsz.activity;

import android.app.FragmentTransaction;
import android.os.Bundle;

import com.zju.hzsz.R;
import com.zju.hzsz.fragment.BaseFragment;
import com.zju.hzsz.fragment.OutletFragment;

/**
 * Created by Wangli on 2017/2/19.
 */

public class OutletListActivity extends BaseActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alloutlet);

        OutletFragment outletFragment = new OutletFragment();
        replaceFragment(outletFragment);
    }

    BaseFragment curFragment = null;

    private void replaceFragment(BaseFragment newFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (curFragment != null)
            curFragment.whenVisibilityChanged(false);
        if (!newFragment.isAdded()) {
            if (curFragment == null) {
                transaction.replace(R.id.container, newFragment).commit();
            } else {
                transaction.hide(curFragment).add(R.id.container, newFragment).commit();
            }
        } else {
            if (curFragment != null)
                transaction.hide(curFragment);
            transaction.show(newFragment);
            transaction.commit();
        }

        curFragment = newFragment;
        curFragment.whenVisibilityChanged(true);
    }
}
