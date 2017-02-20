package com.zju.hzsz.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.activity.RiverActivity;
import com.zju.hzsz.activity.RiverListActivity;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverLocationsRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;

/**
 * 断面水质
 * 
 * @author Robin
 * 
 */
@SuppressLint("UseSparseArrays")
public class RiverMapFragment extends BaseFragment {
	private Map<Integer, River> rivers = new Hashtable<Integer, River>();
	private BaiduMap baiduMap = null;
	MapView mvBaidu = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = mvBaidu = new MapView(getBaseActivity());
			baiduMap = mvBaidu.getMap();
			baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);
			onHeadRefresh();

			// rootView = inflater.inflate(R.layout.fragment_section_map,
			// container, false);
			// rootView.findViewById(R.id.tv_qualityexplain).setOnClickListener(new
			// View.OnClickListener() {
			// @Override
			// public void onClick(View arg0) {
			// getBaseActivity().startXmlActivity(R.layout.activity_qualityexplain,
			// R.string.qualityexplain, 0, 0);
			// }
			// });
			// MapView mv_section = (MapView)
			// rootView.findViewById(R.id.mv_section);
			// baiduMap = mv_section.getMap();
			// baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);
			// onHeadRefresh();

			// baiduMap.getMapStatus().target
			baiduMap.setMaxAndMinZoomLevel(Values.MAP_ZOOM_MAX_LEVEL, Values.MAP_ZOOM_MIN_LEVEL);
			baiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

				@Override
				public void onMapStatusChangeStart(MapStatus arg0) {

				}

				@Override
				public void onMapStatusChangeFinish(MapStatus status) {
					if (location.getLatitude() != status.target.latitude || location.getLongitude() != status.target.longitude) {
						location.setLatitude(status.target.latitude);
						location.setLongitude(status.target.longitude);
						// latiScope = Math.abs(status.bound.northeast.latitude
						// - status.bound.southwest.latitude);
						// longScope = Math.abs(status.bound.northeast.longitude
						// - status.bound.southwest.longitude);
						// getBaseActivity().showToast(baiduMap.getMapStatus().zoom + "");
						onHeadRefresh(false);
					}
				}

				@Override
				public void onMapStatusChange(MapStatus arg0) {

				}
			});

			baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(Marker m) {
					int sid = Integer.parseInt(m.getTitle());
					if (rivers.containsKey(sid)) {
						River s = rivers.get(sid);
						Intent intent = new Intent(getBaseActivity(), RiverActivity.class);
						intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(s));
						startActivity(intent);
					}
					return true;
				}
			});
			baiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(Values.MAP_ZOOM_LEVEL));
			if (location != null) {
				baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(location.getLatitude()).longitude(location.getLongitude()).build());
				MapStatus status = new MapStatus.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).build();
				baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(status));
			}
		}
		return rootView;
	}

	Location location = RiverListActivity.location;
	double longScope = 0.05;
	double latiScope = 0.05;

	public void onHeadRefresh() {
		this.onHeadRefresh(true);
	}

	BitmapDescriptor[] bmps = null;
	boolean isFirst = true;

	public void onHeadRefresh(boolean showope) {
		if (showope)
			showOperating();
		Projection projection = baiduMap.getProjection();
		if (projection != null) {
			LatLng ll1 = projection.fromScreenLocation(new Point(0, 0));
			LatLng ll2 = projection.fromScreenLocation(new Point(mvBaidu.getWidth(), mvBaidu.getHeight()));
			latiScope = Math.abs(ll1.latitude - ll2.latitude);
			longScope = Math.abs(ll1.longitude - ll2.longitude);
		}
		JSONObject p = null;
		{
			// Location location = null;
			p = location != null ? ParamUtils.freeParam(null, "longtitude", location.getLongitude(), "latitude", location.getLatitude(), "longScope", longScope, "latiScope", latiScope) : null;
			// if (location != null) {
			// MarkerOptions options = new MarkerOptions().position(new
			// LatLng(location.getLatitude(),
			// location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.mk_position)).title("MyLocation");
			// baiduMap.addOverlay(options);
			// }
		}
		getRequestContext().add("Get_RiverLoactionMap", new Callback<RiverLocationsRes>() {
			@Override
			public void callback(RiverLocationsRes o) {
				if (o != null && o.isSuccess()) {
					if (location == null) {
						location = new Location("");
						location.setLatitude(o.data.centerLati);
						location.setLongitude(o.data.centerLongti);

						baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(location.getLatitude()).longitude(location.getLongitude()).build());
						MapStatus status = new MapStatus.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).build();
						baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
					}
					if (bmps == null) {
						bmps = new BitmapDescriptor[6];
						bmps[0] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_1);
						bmps[1] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_2);
						bmps[2] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_3);
						bmps[3] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_4);
						bmps[4] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_5);
						bmps[5] = BitmapDescriptorFactory.fromResource(R.drawable.mk_quality_6);
					}
					// rivers.clear();
					synchronized (rivers) {
						baiduMap.clear();
						for (River r : o.data.riverLocations) {
							// if (!rivers.containsKey(r.riverId)) {
							if (r.latitude == 0 && r.longtitude == 0 || r.riverId == 0)
								continue;
							int ix = r.waterType - 1;
							BitmapDescriptor bmp = ix < bmps.length && ix >= 0 ? bmps[ix] : bmps[0];
							MarkerOptions options = new MarkerOptions().position(new LatLng(r.latitude, r.longtitude)).icon(bmp).title("" + r.riverId);
							baiduMap.addOverlay(options);

							// }

							rivers.put(r.riverId, r);
						}
						if (Values.DEBUG && location != null) {
							double[] lats = new double[] { location.getLatitude(), location.getLatitude() - latiScope / 2, location.getLatitude() - latiScope / 2, location.getLatitude() + latiScope / 2, location.getLatitude() + latiScope / 2 };
							double[] lngs = new double[] { location.getLongitude(), location.getLongitude() - longScope / 2, location.getLongitude() + longScope / 2, location.getLongitude() - longScope / 2, location.getLongitude() + longScope / 2 };
							for (int i = 0; i < lats.length; ++i) {
								baiduMap.addOverlay(new MarkerOptions().position(new LatLng(lats[i], lngs[i])).icon(bmps[0]).title("0"));
							}
						}
					}
				}
				hideOperating();

				if (location != null) {
					baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(location.getLatitude()).longitude(location.getLongitude()).build());
				}

				if (isFirst) {
					isFirst = false;
					onHeadRefresh(false);
				}
			}
		}, RiverLocationsRes.class, p);
	}
}
