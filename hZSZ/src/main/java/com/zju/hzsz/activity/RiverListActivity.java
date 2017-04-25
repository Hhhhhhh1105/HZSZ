package com.zju.hzsz.activity;

import android.location.Location;
import android.os.Bundle;

public class RiverListActivity extends SearchRiverActivity{
	/*RiverListFragment listFragment = new RiverListFragment();
	RiverMapFragment mapFragment = new RiverMapFragment();
	boolean isMainPage = false;
	public static Location location = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_allriver);

		setTitle("所有河道");
		initHead(R.drawable.ic_head_back, R.drawable.ic_head_refresh);
		((RadioGroup) findViewById(R.id.rg_headtab)).setOnCheckedChangeListener(this);

		replaceFragment(listFragment);
		findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (islist) {
					listFragment.onHeadRefresh();
				} else {
					mapFragment.onHeadRefresh();
				}
			}
		});

		findViewById(R.id.tv_qualityexplain).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startXmlActivity(R.layout.activity_qualityexplain, R.string.qualityexplain, 0, 0);
			}
		});
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
		// if (islist) {
		// listFragment.onHeadRefresh();
		// } else {
		// mapFragment.onHeadRefresh();
		// }
	}

	Fragment curFragment = null;

	private void replaceFragment(Fragment newFragment) {
		islist = newFragment == listFragment;
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
	protected void onServiceConnected() {
		super.onServiceConnected();
		if (!getUser().gpsdisable) {
			getLocalService().getLocation(new LocationCallback() {
				@Override
				public void callback(Location location) {
					RiverListActivity.location = location;
				}
			});
		}
	}*/

	public static Location location = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("所有河道");
	}
}
