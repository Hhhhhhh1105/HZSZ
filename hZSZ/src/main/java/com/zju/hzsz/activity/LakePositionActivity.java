package com.zju.hzsz.activity;

import android.location.Location;
import android.os.Bundle;

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
import com.zju.hzsz.model.Lake;
import com.zju.hzsz.utils.StrUtils;

/**
 * Created by ZJTLM4600l on 2017/12/22.
 */

public class LakePositionActivity extends BaseActivity {
    Lake lake = null;
    private BaiduMap baiduMap = null;
    protected Location location;
    private LatLng lakePosition;
    private LatLng me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riverposition);
        initHead(R.drawable.ic_head_back, 0);
        setTitle("湖泊方位");
        lake = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_LAKE), Lake.class);

        me = new LatLng(getLatitude(), getLongitude());

        if (lake != null) {
            setTitle(lake.lakeName);
            lakePosition = new LatLng(Double.parseDouble(lake.lakeLatitude),Double.parseDouble(lake.lakeLongitude));//湖泊方位

            MapView mv_section = (MapView) findViewById(R.id.mv_position);
            baiduMap = mv_section.getMap();
            baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);
            baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {

                @Override
                public void onMapLoaded() {
//					adjustViewPort();

                }
            });

            showOperating();

            drawLake();

            hideOperating();

        }
    }

    //画湖泊
    private void drawLake() {
        //自己的方位
        BitmapDescriptor bmp_me = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_me);
        MarkerOptions optionMe = new MarkerOptions().position(me).icon(bmp_me);
        baiduMap.addOverlay(optionMe);
        //湖泊方位
        BitmapDescriptor bmp_lake = BitmapDescriptorFactory.fromResource(R.drawable.lake_posotion);
        if(lakePosition!=null){
            MarkerOptions optionLake = new MarkerOptions().position(lakePosition).icon(bmp_lake);
            baiduMap.addOverlay(optionLake);

            float lat = (float) (lakePosition.latitude);
            float lng = (float) (lakePosition.longitude);
            baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());
            //target：设置地图中心点；zoom:设置缩放级别
            MapStatus status = new MapStatus.Builder().target(new LatLng(lat, lng)).zoom(13).build();
            //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
            baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
        }else{
            baiduMap.setMyLocationData(new MyLocationData.Builder()
                    .latitude(getLatitude()).longitude(getLongitude()).build());
            //target：设置地图中心点；zoom:设置缩放级别
            MapStatus status = new MapStatus.Builder().target(new LatLng(getLatitude(), getLongitude())).zoom(13).build();
            //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
            baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
        }

    }

}
