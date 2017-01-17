package com.zju.hzsz.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sin.android.update.ToolBox;
import com.sin.android.update.Update;
import com.zju.hzsz.R;
import com.zju.hzsz.Values;
import com.zju.hzsz.utils.StrUtils;

public class AboutActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.about);

		((TextView) findViewById(R.id.tv_version)).setText(StrUtils.renderText(this, R.string.fmt_version, Values.Ver));

		refreshVersion();

		findViewById(R.id.ll_checkupdate).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showOperating(R.string.doing_loading);
				(new ToolBox(AboutActivity.this)).checkUpdate(!Values.RELEASE ? "hzsz" : "hzhdsz", Values.Ver, true, new ToolBox.CheckCallback() {
					@Override
					public void onNewVersion(Update update) {
						hideOperating();
						if (update == null) {
							showToast("检查更新失败!");
						} else {
							Values.LastVer = update.getLast();
							if (Values.LastVer == null || !ToolBox.isNewVer(Values.LastVer, Values.Ver)) {
								showToast("当前版本已经是最新的了");
							} else {
								refreshVersion();
							}
						}
					}
				});
			}
		});
	}

	private void refreshVersion() {
		if (Values.LastVer != null && ToolBox.isNewVer(Values.LastVer, Values.Ver)) {
			((TextView) findViewById(R.id.tv_lastversion)).setText(StrUtils.renderText(this, R.string.fmt_newversion, Values.LastVer));
		}
	}
}
