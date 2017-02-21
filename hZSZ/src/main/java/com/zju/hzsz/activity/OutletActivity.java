package com.zju.hzsz.activity;

import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.model.Industrialport;
import com.zju.hzsz.utils.StrUtils;

/**
 * Created by Wangli on 2017/2/17.
 */

public class OutletActivity extends BaseActivity{

    private Industrialport industrialport = null;
    private BaiduMap baiduMap = null;
    protected Location location;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet);

        initHead(R.drawable.ic_head_back, 0);
        setTitle("工业阳光排放口");
        industrialport = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_OUTLET),Industrialport.class);
        if (industrialport != null){
            MapView mv_outlet = (MapView) findViewById(R.id.mv_outlet);
            baiduMap = mv_outlet.getMap();
            baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);
            drawOutlet();
            refreshData();
        }
    }

    private void refreshData() {

        TextView tv_outlet_name = (TextView) findViewById(R.id.outlet_name);
        TextView tv_outlet_code = (TextView) findViewById(R.id.tv_outlet_code);
        TextView tv_outlet_owner = (TextView) findViewById(R.id.tv_outlet_owner);
        TextView tv_outlet_basin = (TextView) findViewById(R.id.tv_outlet_basin);
        TextView tv_outlet_method = (TextView) findViewById(R.id.tv_outlet_method);

        tv_outlet_name.setText(industrialport.sourceName);
        tv_outlet_code.setText(industrialport.sourceId);
        tv_outlet_owner.setText(industrialport.destination);
        tv_outlet_basin.setText(industrialport.basin);
        tv_outlet_method.setText(industrialport.workmanship);


    }

    private void drawOutlet() {
        baiduMap.clear();

        latitude = industrialport.latitude;
        longitude = industrialport.longitude;

        BitmapDescriptor bmp_location = BitmapDescriptorFactory.fromResource(R.drawable.umeng_socialize_location_ic);

        if (latitude != 0.0 && longitude != 0.0){
            LatLng location = new LatLng(latitude, longitude);
            MarkerOptions option = new MarkerOptions().position(location).icon(bmp_location);
            baiduMap.addOverlay(option);

            baiduMap.setMyLocationData(new MyLocationData.Builder().
                    latitude(latitude).longitude(longitude).build());

            MapStatus status = new MapStatus.Builder().target(location).zoom(Values.MAP_ZOOM_LEVEL).build();
            baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
        }
    }
}
