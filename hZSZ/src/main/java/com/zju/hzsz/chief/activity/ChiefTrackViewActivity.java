package com.zju.hzsz.chief.activity;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.zju.hzsz.R;
import com.zju.hzsz.Values;
import com.zju.hzsz.activity.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangli on 2016/12/24.
 */

public class ChiefTrackViewActivity extends BaseActivity {

    private BaiduMap baiduMap = null;
    protected Location location;
    private  static String latList;
    private  static String lngList;

    private static String[] latArray;
    private static String[] lngArray;

    private List<LatLng> pointsToDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_trackview);
        setTitle("轨迹");
        initHead(R.drawable.ic_head_back,0);

        //地图初始化
        MapView mv_trackview = (MapView) findViewById(R.id.mv_trackview);
        baiduMap = mv_trackview.getMap();
        baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);

        //得到轨迹经纬度数组
        latList = getIntent().getExtras().getString("latList");
        lngList = getIntent().getExtras().getString("lngList");
        //将字符串变成数组形式,如果只含有一个坐标点，强行变成两个
        if (getIntent().getExtras().getString("latList") != null){

            //如果仅有一个坐标，则其不包含逗号
            if (!latList.contains(",")){
                latList = latList + "," + latList;
                lngList = lngList + "," + lngList;
            }
        }
        latArray = latList.split(",");
        lngArray = lngList.split(",");

        pointsToDraw = new ArrayList<LatLng>();
        //开始循环填充数据
        for (int i = 0; i < latArray.length; i ++){
            pointsToDraw.add(new LatLng(Double.parseDouble(latArray[i]),Double.parseDouble(lngArray[i])));
        }
        Log.i("来自trackView的latArray", latArray[0].toString());
        Log.i("来自trackView的lngArray", lngArray[0].toString());
        Log.i("pointsToDraw", pointsToDraw.toString());
        //开始画轨迹
        drawTrack();
    }

    private void drawTrack() {
        baiduMap.clear();

        //在地图上显示起点和终点
        BitmapDescriptor bmp_from = BitmapDescriptorFactory.fromResource(R.drawable.track_start);
        BitmapDescriptor bmp_to = BitmapDescriptorFactory.fromResource(R.drawable.track_end);

        //起点和终点值
        LatLng from = pointsToDraw.get(0);
        LatLng to = pointsToDraw.get(pointsToDraw.size() - 1);
        //起点和终点标记
        MarkerOptions optionFrom = new MarkerOptions().position(from).icon(bmp_from);
        MarkerOptions optionTo = new MarkerOptions().position(to).icon(bmp_to);
        //添加标记
        baiduMap.addOverlay(optionFrom);
        baiduMap.addOverlay(optionTo);

        //设置地图中心位置
        float lat = (float) ((from.latitude + to.latitude)/2);
        float lng = (float) ((from.longitude + to.longitude)/2);
        baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());

        List<LatLng> points = new ArrayList<LatLng>();

        //可设置循环，添加坐标点
        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                .color(Color.BLUE).points(pointsToDraw);
        baiduMap.addOverlay(ooPolyline);

        //设置地图中心点与设置缩放级别
        MapStatus status = new MapStatus.Builder().target(new LatLng(lat,lng)).zoom(Values.MAP_ZOOM_LEVEL).build();
        //改变地图的状态
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));

    }
}
