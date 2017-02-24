package com.zju.hzsz.chief.activity;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.zju.hzsz.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Wangli on 2016/11/28.
 */
public class ChiefInspectActivity extends BaseActivity{

    MapView mMapView;
    BaiduMap mBaiduMap;

    Button btn_stop;

    private LocationClient mLocationClient;
    BitmapDescriptor track_start = null;
    BitmapDescriptor track_end = null;
    boolean isFirstLoc = true;
    List<LatLng> points = new ArrayList<LatLng>();//全部点
    List<LatLng> points_tem = new ArrayList<LatLng>();//临时点
    List<LatLng> points_to_server = new ArrayList<LatLng>();//提交至服务器后台的点
    String lngList;//上传至服务器的经度列表，为上传方便转化为字符串
    String latList;//上传至服务器的纬度列表，为上传方便转化为字符串
    int countForPoint = 0;

    OverlayOptions options;

    Handler handler = new Handler();

    boolean isStopLocClient = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_inspect);
        setTitle("巡河");
        initHead(R.drawable.ic_head_back,0);

        handler.postDelayed(new MyRunable(),3000);

        SDKInitializer.initialize(this);

        mMapView = (MapView) findViewById(R.id.mv_position);
        mBaiduMap = mMapView.getMap();

        btn_stop = (Button) findViewById(R.id.btn_stop);

        initLocation();

        track_start = BitmapDescriptorFactory.fromResource(R.drawable.track_start);
        track_end = BitmapDescriptorFactory.fromResource(R.drawable.track_end);


        /*btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStopLocClient = false;
                if (!mLocationClient.isStarted()) {
                    mLocationClient.start();
                }
            }
        });*/

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStopLocClient = true;
                if(mLocationClient.isStarted()){
                    drawEnd(points);
                    mLocationClient.stop();

                    Intent intent = new Intent();
                    intent.putExtra("result",points.toString());
                    intent.putExtra("latList",  latList);
                    intent.putExtra("lngList", lngList);

                    ChiefInspectActivity.this.setResult(RESULT_OK, intent);
                    ChiefInspectActivity.this.finish();
                }
            }
        });
    }


    private void initLocation() {

        ///设置是否允许定位图层
        mBaiduMap.setMyLocationEnabled(true);

        //定位初始化，创建LocationClient对象，创建时传入Context对象
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开GPS
        option.setAddrType("all");//返回的定位结果包含地址信息
        option.setCoorType("bd09ll");//设置坐标类型
        option.setScanSpan(1000);//设置发起定位请求的间隔时间为1000ms

        option.setPriority(LocationClientOption.GpsFirst);
        mLocationClient.setLocOption(option);

        //开启地图定位，显示定位地点非北京天安门
        mLocationClient.start();


    }

    private class MyRunable implements Runnable {

        @Override
        public void run() {
            if(!mLocationClient.isStarted()){
                mLocationClient.start();
            }
            if(!isStopLocClient){
                //每分钟加个点至points_to_server 20*3s = 1min 10*30 = 30s
                if (countForPoint < 10) {
                    countForPoint++;

                    //lngList和latList的处理 for test
                   /* if(lngList == null || latList == null){
                        lngList = "" + points.get(points.size() - 1).longitude;
                        latList = "" + points.get(points.size() - 1).latitude;
                    }else {
                        lngList = lngList + "," + points.get(points.size() - 1).longitude;
                        latList = latList + "," + points.get(points.size() - 1).latitude;
                        Log.d("lngList:", lngList);
                        Log.d("latList:", latList);
                    }*/

                }else{
                    points_to_server.add(points.get(points.size() - 1));

                    //lngList和latList的处理
                    if(lngList == null || latList == null){
                        lngList = "" + points.get(points.size() - 1).longitude;
                        latList = "" + points.get(points.size() - 1).latitude;
                    }else {
                        lngList = lngList + "," + points.get(points.size() - 1).longitude;
                        latList = latList + "," + points.get(points.size() - 1).latitude;
                        Log.d("lngList:", lngList);
                        Log.d("latList:", latList);
                    }

                    Log.d("points_to_server:", points_to_server.toString());
                    countForPoint = 0;
                }
                handler.postDelayed(this,3000);
            }
        }
    }

    /**
     * 定位SDK监听函数
     */
    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location == null || mMapView == null)
                return;

            MyLocationData locData = new MyLocationData.Builder().accuracy(0)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();

            mBaiduMap.setMyLocationData(locData);
            LatLng point = new LatLng(location.getLatitude(),location.getLongitude());
            points.add(point);

            if(isFirstLoc){
                points.add(point);
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());

                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
                //animateMapStatus()方法把定位到的点移动到地图中心
                mBaiduMap.animateMapStatus(msu);
            }

            if(points.size() == 5){
                drawStart(points);
            }
            else if(points.size() > 7){
                points_tem = points.subList(points.size() - 4,points.size());
                options = new PolylineOptions().color(Color.BLUE).width(10).points(points_tem);
                mBaiduMap.addOverlay(options);
            }
        }


    }

    private void drawStart(List<LatLng> points) {

        double myLat = 0.0;
        double myLng = 0.0;

        for(LatLng ll : points){
            myLat += ll.latitude;
            myLng += ll.longitude;
        }

        //将起点处的经纬度添加至经纬度数组之中
        lngList = "" + myLng / 5;
        latList = "" + myLat / 5;
        Log.d("lngList:", lngList);
        Log.d("latList:", latList);

        LatLng avePoint = new LatLng(myLat / points.size(),myLng / points.size());
        points.add(avePoint);

//        options = new DotOptions().center(avePoint).color(0xAA00ff00).radius(15);
        options = new MarkerOptions().position(avePoint).icon(track_start);
        mBaiduMap.addOverlay(options);
    }

    private void drawEnd(List<LatLng> points) {
        double myLat = 0.0;
        double myLng = 0.0;
        if(points.size() > 5){
            for (int i = points.size() - 5; i < points.size(); i++){
                LatLng ll = points.get(i);
                myLat += ll.latitude;
                myLng += ll.longitude;
            }
            LatLng avePoint = new LatLng(myLat / 5, myLng / 5);

            //将终点处的坐标添加至经纬度坐标数组之中
            lngList = lngList + "," + myLng / 5;
            latList = latList + "," + myLat / 5;

            //MarkerOptions options = new MarkerOptions().position(new LatLng(s.compLat, s.compLong)).icon(s.isHandled() ? bmp_done : bmp_undo).title("" + s.compId);
            //options = new DotOptions().center(avePoint).color(0xAAff00ff).radius(15);
            options = new MarkerOptions().position(avePoint).icon(track_end);
            mBaiduMap.addOverlay(options);

//            System.out.println("巡河轨迹点坐标：" + points.toString());
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        //退出时销毁地位
        mLocationClient.stop();
        isStopLocClient = true;

        mBaiduMap.setMyLocationEnabled(false);
        mMapView = null;
        mBaiduMap = null;
        super.onDestroy();

    }
}
