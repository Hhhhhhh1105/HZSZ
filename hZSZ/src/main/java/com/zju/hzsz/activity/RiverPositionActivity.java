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
import com.zju.hzsz.Values;
import com.zju.hzsz.model.River;
import com.zju.hzsz.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;

public class RiverPositionActivity extends BaseActivity {
	River river = null;
	private BaiduMap baiduMap = null;
	protected Location location;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_riverposition);
		initHead(R.drawable.ic_head_back, 0);
		setTitle("河道方位");
		river = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_RIVER), River.class);
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
//			showOperating();
			drawRiver();
			/*getRequestContext().add("Get_OneRiverLoaction", new Callback<RiverDataRes>() {
				@Override
				public void callback(RiverDataRes o) {
					hideOperating();
					if (o != null && o.isSuccess()) {
						ObjUtils.mergeObj(river, o.data);
						setTitle(river.riverName);
						if (river.latitude != 0 && river.longtitude != 0) {
							// BitmapDescriptor bmp =
							// BitmapDescriptorFactory.fromResource(R.drawable.mk_position);
							BitmapDescriptor bmp = BitmapDescriptorFactory.fromResource(R.drawable.mk_position_done);
							MarkerOptions options = new MarkerOptions().position(new LatLng(river.latitude, river.longtitude)).icon(bmp).title(river.riverName);
							baiduMap.addOverlay(options);
							// baiduMap.setMyLocationData(new
							// MyLocationData.Builder().latitude(river.latitude).longitude(river.longtitude).build());
							adjustViewPort();

						} else {
							showToast("暂时未没有该河道的方位信息！");
						}
					}
				}
			}, RiverDataRes.class, ParamUtils.freeParam(null, "riverId", river.riverId));*/
		}
	}

	/**
	 * 1.在地图上显示点
	 * 2.将各点连起来
	 */
	private void drawRiver() {

		baiduMap.clear();

		//在地图上显示起点和终点
		BitmapDescriptor bmp_from = BitmapDescriptorFactory.fromResource(R.drawable.mk_position_done1);
		BitmapDescriptor bmp_to = BitmapDescriptorFactory.fromResource(R.drawable.mk_position_undo1);
		//起点和终点值 latitude:纬度 longitude:经度
		LatLng from = new LatLng(30.271263, 120.133572);
		LatLng to = new LatLng(30.270394, 120.133325);

		MarkerOptions optionFrom = new MarkerOptions().position(from).icon(bmp_from);
		MarkerOptions optionTo = new MarkerOptions().position(to).icon(bmp_to);

		baiduMap.addOverlay(optionFrom);
		baiduMap.addOverlay(optionTo);

		float lat = (float) ((from.latitude + to.latitude) / 2);
		float lng = (float) ((from.longitude + from.longitude) / 2);

		baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());


		List<LatLng> points = new ArrayList<LatLng>();
		points.add(from);
		points.add(new LatLng(30.270666, 120.133430));
		points.add(to);
		OverlayOptions ooPolyline = new PolylineOptions().width(15)
				.color(Color.BLUE).points(points);
		baiduMap.addOverlay(ooPolyline);

		//target：设置地图中心点；zoom:设置缩放级别
		MapStatus status = new MapStatus.Builder().target(new LatLng(lat, lng)).zoom(Values.MAP_ZOOM_LEVEL).build();
		//setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
		baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));




	}

}
