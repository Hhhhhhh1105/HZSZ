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
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.zju.hzsz.R;
import com.zju.hzsz.Values;

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
//    List<LatLng> points_to_server = new ArrayList<LatLng>();//提交至服务器后台的点
    String lngList;//上传至服务器的经度列表，为上传方便转化为字符串
    String latList;//上传至服务器的纬度列表，为上传方便转化为字符串
    int countForPoint = 0;

    //暂时的经纬度数据
    private String latlist_temp = null;
    private String lnglist_temp = null;
    private String[] lat_temp_array;
    private String[] lng_temp_array;
    ArrayList<LatLng> pointsToDrawFirst;
    private boolean hasHistroyData = false;

    OverlayOptions options;

    Handler handler = new Handler();

    boolean isStopLocClient = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(this.getApplication());

        setContentView(R.layout.activity_chief_inspect);
        setTitle("轨迹");
        initHead(R.drawable.ic_head_back,0);

        handler.postDelayed(new MyRunable(),3000);

        SDKInitializer.initialize(this);

        mMapView = (MapView) findViewById(R.id.mv_position);
        mBaiduMap = mMapView.getMap();

        btn_stop = (Button) findViewById(R.id.btn_stop);

        initLocation();

        latlist_temp = getIntent().getExtras().getString("latlist_temp");
        lnglist_temp = getIntent().getExtras().getString("lnglist_temp");


        track_start = BitmapDescriptorFactory.fromResource(R.drawable.track_start);
        track_end = BitmapDescriptorFactory.fromResource(R.drawable.track_end);

        if (getIntent().getExtras().getString("latlist_temp") != null){
            hasHistroyData = true;
            Log.i("recordinspect", latlist_temp);

            if (latList != null){
                latList = latList + latlist_temp;
                lngList = lngList + lnglist_temp;
            }else {
                latList = "" + latlist_temp;
                lngList = "" + lnglist_temp;
            }

            if (latlist_temp.contains(",")){
                //如果不止一个数据，变成一个数组
                lat_temp_array = latlist_temp.split(",");
                lng_temp_array = lnglist_temp.split(",");
            }else{
                //如果只有一个数据
                lat_temp_array = new String[1];
                lng_temp_array = new String[1];
                lat_temp_array[0] = latlist_temp;
                lng_temp_array[0] = lnglist_temp;
            }

            pointsToDrawFirst = new ArrayList<LatLng>();
            pointsToDrawFirst.add(new
                    LatLng(Double.parseDouble(lat_temp_array[0]), Double.parseDouble(lng_temp_array[0])));

            for (int i = 0; i < lat_temp_array.length; i++){
                pointsToDrawFirst.add(new LatLng(Double.parseDouble(lat_temp_array[i]), Double.parseDouble(lng_temp_array[i])));
            }

            //将editRecord界面的最后一个点与inspect界面的第一个点连起来
            points.add(pointsToDrawFirst.get(pointsToDrawFirst.size() - 1));
        }

        mLocationClient.start();

        findViewById(R.id.iv_head_left).setOnClickListener(backToEditListener);

        btn_stop.setOnClickListener(backToEditListener);

    /*    btn_stop.setOnClickListener(new View.OnClickListener() {
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
        });*/
    }

    View.OnClickListener backToEditListener = new View.OnClickListener() {
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

                //若成功提交，则地理坐标缓存值设置设为空
                getUser().setBaiduLatPoints("");
                getUser().setBaiduLngPoints("");

                ChiefInspectActivity.this.finish();
            }
        }
    };

    private void drawBeforeTrack() {

        showOperating();

        mBaiduMap.clear();
        BitmapDescriptor bmp_from = BitmapDescriptorFactory.fromResource(R.drawable.track_start);
        LatLng from = pointsToDrawFirst.get(0);
        Log.i("recordinspect", "起点坐标" + from.toString());
        MarkerOptions optionFrom = new MarkerOptions().position(from).icon(bmp_from);
        mBaiduMap.addOverlay(optionFrom);

        OverlayOptions ooPolyline = new PolylineOptions().width(10).color(Color.BLUE).points(pointsToDrawFirst);
        mBaiduMap.addOverlay(ooPolyline);
        Log.i("recordinspect", "画过起点");

        MapStatus status = new MapStatus.Builder().target(from).zoom(Values.MAP_ZOOM_LEVEL).build();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));

        hideOperating();

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
    }

    private class MyRunable implements Runnable {

        @Override
        public void run() {
            if(!mLocationClient.isStarted()){
                mLocationClient.start();
            }
            if(!isStopLocClient){
                //每分钟加个点至points_to_server 20*3s = 1min 3*3 = 9s
                if (countForPoint < 3) {
                    countForPoint++;

                }else{
//                    points_to_server.add(points.get(points.size() - 1));

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

//                    Log.d("points_to_server:", points_to_server.toString());
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

            if(points.size() == 5 && !hasHistroyData ){
                drawStart(points);
            }else if (points.size() == 5 && hasHistroyData){
                drawBeforeTrack();
                options = new PolylineOptions().color(Color.BLUE).width(10).points(points);
                mBaiduMap.addOverlay(options);
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
        if (lngList == null || latList == null){
            lngList = "" + myLng / 5;
            latList = "" + myLat / 5;
        }

//        Log.d("lngList:", lngList);
//        Log.d("latList:", latList);

        LatLng avePoint = new LatLng(myLat / points.size(),myLng / points.size());
        points.add(avePoint);

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
    protected void onStop() {
        super.onStop();
        getUser().setBaiduLatPoints(latList);
        getUser().setBaiduLngPoints(lngList);
    }

    @Override
    protected void onDestroy() {
        //退出时销毁地位
        mLocationClient.stop();
        isStopLocClient = true;

        mBaiduMap.setMyLocationEnabled(false);
//        mMapView.onDestroy();
        mMapView = null;
        mBaiduMap = null;
        super.onDestroy();

    }
}
