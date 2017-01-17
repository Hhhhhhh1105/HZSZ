package com.zju.hzsz.clz;

import android.content.Context;
import android.view.View;

import com.zju.hzsz.R;

/*
* 主要是针对inc_head的操作
* setHeadImage()
* setHeadTitle()
* */

public class RootViewWarp extends ViewWarp {
	private static final String TAG = "RootViewWarp";

	public RootViewWarp(View rootView, Context context) {
		super(rootView, context);
	}

	public void setHeadImage(int leftid, int rightid) {
		setImage(R.id.iv_head_left, leftid);
		setImage(R.id.iv_head_right, rightid);
	}

	public void setHeadTitle(int titleid) {
		setText(R.id.tv_head_title, titleid);
	}

	public void setHeadTitle(String title) {
		setText(R.id.tv_head_title, title);
	}

	public void setHeadTabText(int leftid, int rightid) {
		setText(R.id.rb_head_left, leftid);
		setText(R.id.rb_head_right, rightid);
	}
}
