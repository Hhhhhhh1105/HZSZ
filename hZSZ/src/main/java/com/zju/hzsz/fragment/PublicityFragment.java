package com.zju.hzsz.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.zju.hzsz.R;
import com.zju.hzsz.activity.MainActivity;

/**
 * 投诉公示
 * 
 * @author Robin
 * 
 */
public class PublicityFragment extends BaseFragment implements OnCheckedChangeListener {

	PublicityListFragment listFragment = new PublicityListFragment();
	PublicityMapFragment mapFragment = new PublicityMapFragment();
	TabRankingFragment rankingFragment = new TabRankingFragment();
	boolean isMainPage = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_publicity, container, false);

			isMainPage = (getBaseActivity() instanceof MainActivity);

			((RadioGroup) rootView.findViewById(R.id.rg_headtab)).setOnCheckedChangeListener(this);
			//设置右上方的图标
			getRootViewWarp().setHeadImage(0, R.drawable.ic_head_refresh);
			replaceFragment(listFragment);
			rootView.findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (islist && !isRank) {
						listFragment.onHeadRefresh();
					} else if(!islist && !isRank){
						mapFragment.onHeadRefresh();
					}else if (isRank){
						rankingFragment.showAreaPop();
					}

				}
			});

			if (!isMainPage) {
				getRootViewWarp().setHeadImage(R.drawable.ic_head_back, R.drawable.ic_head_refresh);
				// ((TextView)
				// rootView.findViewById(R.id.tv_head_title)).setText("所有断面");

			}
			getRootViewWarp().setHeadTitle("投诉公示");

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
	boolean isRank = false;

	//取id为add是因为后续增加了个tab，但自己太懒了，不想改原来right的逻辑
	@Override
	public void onCheckedChanged(RadioGroup rg, int rdid) {
		switch (rdid) {
		case R.id.rb_head_left:
			replaceFragment(listFragment);
			getRootViewWarp().setHeadImage(0, R.drawable.ic_head_refresh);
			break;
		case R.id.rb_head_right:
			replaceFragment(mapFragment);
			getRootViewWarp().setHeadImage(0, R.drawable.ic_head_refresh);
			break;
		case R.id.rb_head_add:
			replaceFragment(rankingFragment);
			getRootViewWarp().setHeadImage(0, R.drawable.ic_head_order);
			break;
		default:
			break;
		}
	}

	Fragment curFragment = null;

	private void replaceFragment(Fragment newFragment) {
		islist = newFragment == listFragment;//islist为true时，listFragment加载数据
		isRank = newFragment == rankingFragment;
		//开启fragment事务
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (!newFragment.isAdded()) {
			if (curFragment == null) {
				//使用当前Fragment的布局替代id为fl_section_container的控件
				transaction.replace(R.id.fl_section_container, newFragment).commit();
			} else {
				transaction.hide(curFragment).add(R.id.fl_section_container, newFragment).commit();
			}
		} else {
			if (curFragment != null)
				transaction.hide(curFragment);
			transaction.show(newFragment);
			//提交事务
			transaction.commit();
		}
		curFragment = newFragment;
	}
}
