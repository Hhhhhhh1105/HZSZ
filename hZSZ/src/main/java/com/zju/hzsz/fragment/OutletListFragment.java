package com.zju.hzsz.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.OutletActivity;
import com.zju.hzsz.model.Industrialport;
import com.zju.hzsz.model.OutletListDataRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangli on 2017/2/19.
 */

public class OutletListFragment extends BaseFragment{

    SimpleListAdapter adapter = null;
    List<Industrialport> outlets = new ArrayList<Industrialport>();
    ListViewWarp listViewWarp =null;
    Location location = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //获取当前位置信息相关
        mLocationClient = new LocationClient(getBaseActivity().getApplicationContext());
        mBDLocationListener = new MyBDLocationListner();
        mLocationClient.registerLocationListener(mBDLocationListener);
        getLocation();


        if (this.rootView == null){
            adapter = new SimpleListAdapter(getBaseActivity(), outlets, new SimpleViewInitor() {

                @Override
                public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
                    if (convertView == null) {
                        convertView = LinearLayout.inflate(context, R.layout.item_outlet_list, null);
                    }
                    Industrialport outlet = (Industrialport) data;

                    ((TextView)convertView.findViewById(R.id.tv_source_name)).setText(outlet.sourceName);
                    ((TextView)convertView.findViewById(R.id.tv_basin)).setText(outlet.basin);
                    if (outlet.distance <= 1000.0){
                        ((TextView)convertView.findViewById(R.id.tv_distance)).setText(Double.toString(outlet.distance) + '米');
                    }else {
                        ((TextView)convertView.findViewById(R.id.tv_distance)).setText(Double.toString(outlet.distance/1000.0) + "千米");
                    }

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getBaseActivity(), OutletActivity.class);
                            intent.putExtra(Tags.TAG_OUTLET, StrUtils.Obj2Str(v.getTag()));
                            startActivity(intent);
                        }
                    });
                    convertView.setTag(outlet);
                    return convertView;
                }
            });

            listViewWarp = new ListViewWarp(getBaseActivity(), adapter, new ListViewWarp.WarpHandler() {
                @Override
                public boolean onRefresh() {
                    return startLoad(true);
                }

                @Override
                public boolean onLoadMore() {
                    return startLoad(false);
                }
            });

            rootView = listViewWarp.getRootView();

            onHeadRefresh();
        }

        return rootView;
    }

    public void onHeadRefresh() {
        startLoad(true);
    }


    private final int DefaultPageSize = 10;

    protected JSONObject getPageParam(boolean refresh) {
        if ( longitude == 0.0){
            return refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (outlets.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
        }else{
            return refresh ? ParamUtils.freeParam(null, "pageSize", DefaultPageSize, "currentPage", 1, "latitude", latitude, "longtitude", longitude)
                    : ParamUtils.freeParam(null, "pageSize", DefaultPageSize, "currentPage", (outlets.size() + DefaultPageSize - 1) / DefaultPageSize + 1,"latitude", latitude, "longtitude", longitude );
        }

    }

    private boolean startLoad(final boolean refresh) {
        showOperating();
        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);
        getRequestContext().add("Get_IndustrialPort_List", new Callback<OutletListDataRes>() {
            @Override
            public void callback(OutletListDataRes o) {
                listViewWarp.setLoadingMore(false);
                listViewWarp.setRefreshing(false);
                if(o != null && o.isSuccess() && o.data != null && o.data.industrialPortJsons != null){
                    if (refresh)
                        outlets.clear();
                    for (Industrialport outlet : o.data.industrialPortJsons) {
                        outlets.add(outlet);
                    }
                    adapter.notifyDataSetChanged();
                }
                hideOperating();
                if ((o != null && o.data != null && o.data.industrialPortJsons != null) && (o.data.pageInfo != null && outlets.size() >= o.data.pageInfo.totalCounts || o.data.industrialPortJsons.length == 0)) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, OutletListDataRes.class, getPageParam(refresh));
        return true;
    }


    private LocationClient mLocationClient;
    private BDLocationListener mBDLocationListener;
    private double latitude;
    private double longitude;

    public void getLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(5000);

        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    /**
     * 获取当前的位置信息&获取位置信息后发送请求
     */
    private class MyBDLocationListner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null){
                latitude = bdLocation.getLatitude();
                longitude = bdLocation.getLongitude();
                System.out.println("latitude:" + latitude + "," + "longitude:" + longitude);
                initData(true);
                if (mLocationClient.isStarted()){
                    mLocationClient.stop();
                    mLocationClient.unRegisterLocationListener(mBDLocationListener);
                }

            }
        }
    }

    /**
     * 获取数据所发送的请求
     */
    public void initData(final boolean refresh){
        showOperating();
        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);
        getRequestContext().add("Get_IndustrialPort_List", new Callback<OutletListDataRes>() {
            @Override
            public void callback(OutletListDataRes o) {
                listViewWarp.setLoadingMore(false);
                listViewWarp.setRefreshing(false);
                if(o != null && o.isSuccess() && o.data != null && o.data.industrialPortJsons != null){
                    if (refresh)
                        outlets.clear();
                    for (Industrialport outlet : o.data.industrialPortJsons) {
                        outlets.add(outlet);
                    }
                    adapter.notifyDataSetChanged();
                }
                hideOperating();
                if ((o != null && o.data != null && o.data.industrialPortJsons != null) && (o.data.pageInfo != null && outlets.size() >= o.data.pageInfo.totalCounts || o.data.industrialPortJsons.length == 0)) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, OutletListDataRes.class, getPageParam(refresh));
    }
}
