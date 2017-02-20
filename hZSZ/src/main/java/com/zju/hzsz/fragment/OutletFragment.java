package com.zju.hzsz.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.zju.hzsz.R;

/**
 * Created by Wangli on 2017/2/19.
 */

public class OutletFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener{


    OutletListFragment listFragment = new OutletListFragment();
    SectionMapFragment mapFragment = new SectionMapFragment();
    boolean isMainPage = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_outlet, container, false);

            ((RadioGroup) rootView.findViewById(R.id.rg_headtab)).setOnCheckedChangeListener(this);
            getRootViewWarp().setHeadImage(R.drawable.ic_head_back, R.drawable.ic_head_refresh);
            replaceFragment(listFragment);
            //该功能后期可改为利用区划进行筛选
            rootView.findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (islist) {
                        listFragment.onHeadRefresh();
                    } else {
                        mapFragment.onHeadRefresh();
                    }
                }
            });

            getRootViewWarp().setHeadTitle("阳光排放口");

            rootView.findViewById(R.id.iv_head_left).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!isMainPage) {
                        getBaseActivity().finish();
                    }
                }
            });
        }
        return rootView;
    }

    boolean islist = false;

    @Override
    public void onCheckedChanged(RadioGroup rg, int rdid) {
        switch (rdid) {
            case R.id.rb_head_left:
                replaceFragment(listFragment);
                break;
            case R.id.rb_head_right:
                replaceFragment(mapFragment);
                break;
            default:
                break;
        }
    }

    Fragment curFragment = null;

    private void replaceFragment(Fragment newFragment) {
        islist = newFragment == listFragment;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (!newFragment.isAdded()) {
            if (curFragment == null) {
                transaction.replace(R.id.fl_outlet_container, newFragment).commit();
            } else {
                transaction.hide(curFragment).add(R.id.fl_outlet_container, newFragment).commit();
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
