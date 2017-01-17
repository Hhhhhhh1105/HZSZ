package com.zju.hzsz.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;

import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.clz.RootViewWarp;
import com.zju.hzsz.net.RequestContext;

public class BaseFragment extends Fragment {
	protected View rootView = null;
	private RootViewWarp rootViewWarp = null;

	public RootViewWarp getRootViewWarp() {
		if (rootViewWarp == null)
			rootViewWarp = new RootViewWarp(rootView, getActivity());
		return rootViewWarp;
	}

	protected BaseActivity getBaseActivity() {
//		return (BaseActivity) getActivity();
//		this.getActivity()
		return BaseActivity.getCurActivity();
	}

	public ProgressDialog showOperating() {
		return getBaseActivity().showOperating();
	}

	public ProgressDialog showOperating(int strid) {
		return getBaseActivity().showOperating(strid);
	}

	public void hideOperating() {
		getBaseActivity().hideOperating();
	}

	public RequestContext getRequestContext() {
		return getBaseActivity().getRequestContext();
	}
	
	@Override
	public void startActivity(Intent intent) {
		getBaseActivity().startActivity(intent);
	}
	
	
	public void whenVisibilityChanged(boolean isVisibilityed){
		
	}
	

}
