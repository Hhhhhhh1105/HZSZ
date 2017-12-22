package com.zju.hzsz.fragment.lake;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.zju.hzsz.R;
import com.zju.hzsz.activity.BaseActivity;
import com.zju.hzsz.adapter.PagerItem;
import com.zju.hzsz.model.Lake;
import com.zju.hzsz.utils.ViewUtils;

/**
 * Created by ZJTLM4600l on 2017/12/15.
 */

public class BaseLakePagerItem extends PagerItem {
    protected Lake lake;
    protected BaseActivity context;
    protected View view;

    public BaseLakePagerItem(Lake lake, BaseActivity context) {
        super();
        this.lake = lake;
        this.context = context;
    }

    public void loadData() {

    }

    public void readyView(){

    }

    protected void initedView() {
        if (view != null) {
            if (view.findViewById(R.id.srl_main) instanceof SwipeRefreshLayout) {
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_main);
                ViewUtils.setSwipeRefreshLayoutColorScheme(swipeRefreshLayout);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                    @Override
                    public void onRefresh() {
                        loadData();
                    }
                });
            }
        }
    }

    protected void setRefreshing(boolean b) {
        if (view != null) {
            if (view.findViewById(R.id.srl_main) instanceof SwipeRefreshLayout) {
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_main);
                swipeRefreshLayout.setRefreshing(b);
            }
        }
    }

    @Override
    public View getView() {
        return null;
    }
}
