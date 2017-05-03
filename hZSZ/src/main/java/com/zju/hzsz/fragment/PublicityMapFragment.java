package com.zju.hzsz.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.activity.CompDetailActivity;
import com.zju.hzsz.model.CompPublicity;
import com.zju.hzsz.model.CompPublicitysRes;
import com.zju.hzsz.model.CompSugs;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.StrUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 
 * @author Robin
 * 
 */
public class PublicityMapFragment extends BaseFragment {
	@SuppressLint("UseSparseArrays")
	private Map<Integer, CompPublicity> cps = new HashMap<Integer, CompPublicity>();
	private BaiduMap baiduMap = null;
	private LinearLayout tv_compsum = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_publicity_map, container, false);
			tv_compsum = (LinearLayout) rootView.findViewById(R.id.tv_compsum);
			MapView mv_section = (MapView) rootView.findViewById(R.id.mv_section);
			baiduMap = mv_section.getMap();
			baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);
			baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(Marker m) {
					int sid = Integer.parseInt(m.getTitle());
					Log.i("map", "cp id: " + sid);
					if (cps.containsKey(sid)) {
						CompPublicity cp = cps.get(sid);
						//投诉建议
						CompSugs comp = new CompSugs();
						comp.complaintsId = cp.getId();
						comp.complaintsPicPath = cp.getCompPicPath();
						comp.compTheme = cp.compTheme;
						comp.compStatus = cp.compStatus;
						comp.compPersonId = cp.compPersonId;

						Intent intent = new Intent(getBaseActivity(), CompDetailActivity.class);
						intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
						intent.putExtra(Tags.TAG_ABOOLEAN, true);
						startActivity(intent);
					}
					return true;
				}
			});
			onHeadRefresh();
		}
		return rootView;
	}

	BitmapDescriptor bmp_done = null;
	BitmapDescriptor bmp_undo = null;

	public void onHeadRefresh() {
		showOperating();
		getRequestContext().add("Get_LastComplaint_List", new Callback<CompPublicitysRes>() {
			@Override
			public void callback(CompPublicitysRes o) {
				if (o != null && o.isSuccess()) {
					baiduMap.clear();
					if (bmp_done == null) {
//						bmp_done = BitmapDescriptorFactory.fromResource(R.drawable.mk_position_done);
						bmp_done = BitmapDescriptorFactory.fromResource(R.drawable.mk_position_done1);
//						bmp_undo = BitmapDescriptorFactory.fromResource(R.drawable.mk_position_undo);
						bmp_undo = BitmapDescriptorFactory.fromResource(R.drawable.mk_position_undo1);
					}
					// BitmapDescriptor[] bmps = new BitmapDescriptor[6];
					// bmps[0] =
					// BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_1);
					// bmps[1] =
					// BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_2);
					// bmps[2] =
					// BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_3);
					// bmps[3] =
					// BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_4);
					// bmps[4] =
					// BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_5);
					// bmps[5] =
					// BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_6);
					float latsum = 0, lngsum = 0;
					cps.clear();
					int handled = 0;
					int unhandle = 0;
					int allcount = 0;
					for (CompPublicity s : o.data) {
						if (s.compLong == 0 && s.compLat == 0)
							continue;
						++allcount;
						latsum += s.compLat;
						lngsum += s.compLong;
						//icon：设置marker覆盖物的图标；position：设置marker覆盖物的位置坐标；title:设置marker覆盖物的标题
						MarkerOptions options = new MarkerOptions().position(new LatLng(s.compLat, s.compLong)).icon(s.isHandled() ? bmp_done : bmp_undo).title("" + s.compId);
						if (s.isHandled()) {
							++handled;
						} else {
							++unhandle;
						}
						//向地图添加一个overlay
						baiduMap.addOverlay(options);

						cps.put(s.compId, s);
					}

//					tv_compsum.setText(String.format(Locale.CHINA, "最新投诉%d条，%d条未处理", unhandle + handled, unhandle));
					if (allcount > 0) {
						float lat = latsum / allcount, lng = lngsum / allcount;
						//生成定位数据对象，并设置定位数据
						baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());
						//target：设置地图中心点；zoom:设置缩放级别
						MapStatus status = new MapStatus.Builder().target(new LatLng(lat, lng)).zoom(Values.MAP_ZOOM_LEVEL).build();
						//setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
						baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
					}
				}
				hideOperating();
			}
		}, CompPublicitysRes.class, null);
	}
}
