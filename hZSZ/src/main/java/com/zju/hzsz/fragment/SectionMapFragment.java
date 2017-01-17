package com.zju.hzsz.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.zju.hzsz.activity.SectionActivity;
import com.zju.hzsz.model.Section;
import com.zju.hzsz.model.SectionArrRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.StrUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 断面水质
 * 
 * @author Robin
 * 
 */
@SuppressLint("UseSparseArrays")
public class SectionMapFragment extends BaseFragment {
	private Map<Integer, Section> sections = new HashMap<Integer, Section>();
	private BaiduMap baiduMap = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_section_map, container, false);
			rootView.findViewById(R.id.tv_qualityexplain).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					getBaseActivity().startXmlActivity(R.layout.activity_qualityexplain, R.string.qualityexplain, 0, 0);
				}
			});
			//获取百度地图控件
			MapView mv_section = (MapView) rootView.findViewById(R.id.mv_section);
			//获取百度地图对象
			baiduMap = mv_section.getMap();
			//设置地图视角为俯视
			baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);
			onHeadRefresh();
		}
		return rootView;
	}

	public void onHeadRefresh() {
		showOperating();
		getRequestContext().add("section_map_get", new Callback<SectionArrRes>() {
			@Override
			public void callback(SectionArrRes o) {
				if (o != null && o.isSuccess()) {
					baiduMap.clear();
					BitmapDescriptor[] bmps = new BitmapDescriptor[6];
					bmps[0] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_1);
					bmps[1] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_2);
					bmps[2] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_3);
					bmps[3] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_4);
					bmps[4] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_5);
					bmps[5] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_6);
					float latsum = 0, lngsum = 0;
					sections.clear();
					for (Section s : o.data) {
						latsum += s.latitude;
						lngsum += s.longititude;
						MarkerOptions options = new MarkerOptions().position(new LatLng(s.latitude, s.longititude)).icon(bmps[s.waterType - 1]).title("" + s.sectionId);
						baiduMap.addOverlay(options);

						sections.put(s.sectionId, s);
					}
					float lat = latsum / o.data.length, lng = lngsum / o.data.length;
					baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());

					MapStatus status = new MapStatus.Builder().target(new LatLng(lat, lng)).zoom(9).build();
					baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
					baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {

						@Override
						public boolean onMarkerClick(Marker m) {
							int sid = Integer.parseInt(m.getTitle());
							Log.i("map", "sec id: " + sid);
							if (sections.containsKey(sid)) {
								Section s = sections.get(sid);
								Intent intent = new Intent(getBaseActivity(), SectionActivity.class);
								intent.putExtra(Tags.TAG_SECTION, StrUtils.Obj2Str(s));
								startActivity(intent);
							}
							return true;
						}
					});
				}
				hideOperating();
			}
		}, SectionArrRes.class, null);
	}
}
