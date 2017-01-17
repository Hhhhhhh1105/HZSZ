package com.zju.hzsz.activity;

import android.content.Intent;
import android.os.Bundle;

import com.zju.hzsz.R;

/**
 * 该Activity只是简单的显示xml布局，传入参数:titleid, xmlid, leftid, rightid
 * 
 * @author Robin
 * 
 */
public class XmlActivity extends BaseActivity {
	public static final String STR_TITLEID = "titleid";
	public static final String STR_XMLID = "xmlid";
	public static final String STR_LEFTID = "leftid";
	public static final String STR_RIGHTID = "right";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setContentView(intent.getIntExtra(STR_XMLID, 0));

		int leftid = intent.getIntExtra(STR_LEFTID, 0);
		int rightid = intent.getIntExtra(STR_RIGHTID, 0);
		initHead(leftid == 0 ? R.drawable.ic_head_back : leftid, rightid);

		int titleid = intent.getIntExtra(STR_TITLEID, 0);
		if (titleid != 0)
			setTitle(titleid);
	}
}
