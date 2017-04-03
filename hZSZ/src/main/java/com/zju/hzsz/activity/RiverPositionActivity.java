package com.zju.hzsz.activity;

import android.graphics.Color;
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
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverPosition;
import com.zju.hzsz.model.RiverPositionRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;

public class RiverPositionActivity extends BaseActivity {
	River river = null;
	private BaiduMap baiduMap = null;
	protected Location location;
	private List<LatLng> points;
	private LatLng start;
	private LatLng end;
	private LatLng me;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_riverposition);
		initHead(R.drawable.ic_head_back, 0);
		setTitle("河道方位");
		river = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_RIVER), River.class);

		me = new LatLng(getLatitude(), getLongitude());

		if (river != null) {
			setTitle(river.riverName);
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
			getRequestContext().add("Get_OneRiverMap", new Callback<RiverPositionRes>() {
				@Override
				public void callback(RiverPositionRes o) {
					hideOperating();
					if (o != null && o.isSuccess()) {

						setTitle(river.riverName);

						RiverPosition rp = o.data;
						points = new ArrayList<LatLng>();
						if (rp.baidu_start_lat != 0.0){
							start = new LatLng(rp.baidu_start_lat, rp.baidu_start_lng);
							points.add(start);
						}
						if (rp.baidu_pub1_lat != 0.0) {
							LatLng pub1 = new LatLng(rp.baidu_pub1_lat, rp.baidu_pub1_lng);
							points.add(pub1);
						}

						if (rp.baidu_pub2_lat != 0.0) {
							LatLng pub2 = new LatLng(rp.baidu_pub2_lat, rp.baidu_pub2_lng);
							points.add(pub2);
						}

						if (rp.baidu_pub3_lat != 0.0) {
							LatLng pub3 = new LatLng(rp.baidu_pub3_lat, rp.baidu_pub3_lng);
							points.add(pub3);
						}
						if (rp.baidu_end_lat != 0.0) {
							end = new LatLng(rp.baidu_end_lat, rp.baidu_end_lng);
							points.add(end);
						}

						drawRiver();
					}
				}
			}, RiverPositionRes.class, ParamUtils.freeParam(null, "riverId", river.riverId));



		}
	}

	/**
	 * 1.在地图上显示点
	 * 2.将各点连起来
	 */
	private void drawRiver() {

		baiduMap.clear();

		//在地图上显示起点和终点
		BitmapDescriptor bmp_from = BitmapDescriptorFactory.fromResource(R.drawable.track_start);
		BitmapDescriptor bmp_to = BitmapDescriptorFactory.fromResource(R.drawable.track_end);
		BitmapDescriptor bmp_me = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_me);
		BitmapDescriptor bmp_pub = BitmapDescriptorFactory.fromResource(R.drawable.ic_pub);
		MarkerOptions optionMe = new MarkerOptions().position(me).icon(bmp_me);
		baiduMap.addOverlay(optionMe);

		if (start != null && end != null) {
			MarkerOptions optionFrom = new MarkerOptions().position(start).icon(bmp_from);
			MarkerOptions optionTo = new MarkerOptions().position(end).icon(bmp_to);

			baiduMap.addOverlay(optionFrom);
			baiduMap.addOverlay(optionTo);

			if (points.size() > 2) {
				for (int  i = 1; i < points.size() - 1; i ++) {
					MarkerOptions optionPub = new MarkerOptions().position(points.get(i)).icon(bmp_pub);
					baiduMap.addOverlay(optionPub);
				}
			}

			float lat = (float) ((start.latitude + end.latitude) / 2);
			float lng = (float) ((start.longitude + end.longitude) / 2);
			baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());

			OverlayOptions ooPolyline = new PolylineOptions().width(10)
					.color(Color.BLUE).points(points);
			baiduMap.addOverlay(ooPolyline);

			//target：设置地图中心点；zoom:设置缩放级别
			MapStatus status = new MapStatus.Builder().target(new LatLng(lat, lng)).zoom(13).build();
			//setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
			baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
		}else {
			baiduMap.setMyLocationData(new MyLocationData.Builder()
					.latitude(getLatitude()).longitude(getLongitude()).build());
			//target：设置地图中心点；zoom:设置缩放级别
			MapStatus status = new MapStatus.Builder().target(new LatLng(getLatitude(), getLongitude())).zoom(13).build();
			//setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
			baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
		}


	}

}
