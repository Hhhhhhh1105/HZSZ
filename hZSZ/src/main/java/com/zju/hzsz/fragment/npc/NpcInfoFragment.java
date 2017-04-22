package com.zju.hzsz.fragment.npc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zju.hzsz.R;
import com.zju.hzsz.fragment.BaseFragment;

/**
 * 人大代表具体信息
 * Created by Wangli on 2017/4/19.
 */

public class NpcInfoFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.rootView == null) {
            rootView = LinearLayout.inflate(getBaseActivity(), R.layout.fragment_npcinfo, null);
        }
        return rootView;
    }
}
