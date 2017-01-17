package com.zju.hzsz.clz;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zju.hzsz.utils.ImgUtils;

public class ViewWarp {
	private static final String TAG = "ViewWarp";
	private View view;
	protected Context context;

	public ViewWarp(View view, Context context) {
		super();
		this.view = view;
		this.context = context;
	}

	public View getView() {
		return view;
	}

	public View getViewById(int id) {
		View v = view.findViewById(id);
		if (v == null) {
			Log.e(TAG, "View ID=" + id + " not exist!");
		}
		return v;
	}

	public void setText(int id, int sid) {
		TextView tv = (TextView) getViewById(id);
		if (tv != null) {
			tv.setText(sid);
		}
	}

	public void setText(int id, String str) {
		TextView tv = (TextView) getViewById(id);
		if (tv != null) {
			tv.setText(str);
		}
	}

	public void setText(int id, CharSequence str) {
		TextView tv = (TextView) getViewById(id);
		if (tv != null) {
			tv.setText(str);
		}
	}

	public void setImage(int id, int sid) {
		ImageView iv = (ImageView) getViewById(id);
		if (iv != null) {
			if (sid == 0) {
				iv.setVisibility(View.INVISIBLE);
			} else {
				iv.setImageResource(sid);
				iv.setVisibility(View.VISIBLE);
			}
		}
	}

	public void setImage(int id, String url) {
		ImageView iv = (ImageView) getViewById(id);
		if (iv != null) {
			ImgUtils.loadImage(context, iv, url);
		}
	}
}
