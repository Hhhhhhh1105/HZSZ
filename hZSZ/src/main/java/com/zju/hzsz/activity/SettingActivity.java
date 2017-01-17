package com.zju.hzsz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.zju.hzsz.R;

public class SettingActivity extends BaseActivity implements OnCheckedChangeListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.mysetting);

		findViewById(R.id.ll_changepwd).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(SettingActivity.this, ChangePasswordActivity.class));
			}
		});
		((Switch) (findViewById(R.id.sw_gps))).setChecked(!getUser().gpsdisable);
		((Switch) (findViewById(R.id.sw_notify))).setChecked(getUser().isNotifyable());

		((Switch) (findViewById(R.id.sw_gps))).setOnCheckedChangeListener(this);
		((Switch) (findViewById(R.id.sw_notify))).setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton cb, boolean ck) {
		if (cb.getId() == R.id.sw_gps) {
			// GPS
			getUser().gpsdisable = !ck;
		} else if (cb.getId() == R.id.sw_notify) {
			// GPS
			getUser().setNotifyable(ck);
			if (getLocalService() != null) {
				saveLocalData();
				getLocalService().notifyableChanged();
			}
		}
	}
}
