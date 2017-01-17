package com.zju.hzsz.activity;

import android.app.FragmentTransaction;
import android.os.Bundle;

import com.zju.hzsz.R;
import com.zju.hzsz.fragment.BaseFragment;
import com.zju.hzsz.fragment.SectionFragment;

public class SectionListActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_allsection);
		initHead(R.drawable.ic_head_plus, R.drawable.ic_head_share);
		SectionFragment sectionFragment = new SectionFragment();
		
		replaceFragment(sectionFragment);
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
