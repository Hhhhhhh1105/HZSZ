package com.zju.hzsz.fragment;

import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.activity.OutletActivity;
import com.zju.hzsz.activity.RiverListActivity;
import com.zju.hzsz.model.Industrialport;
import com.zju.hzsz.model.OutletLocationsRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Wangli on 2017/2/19.
 */

public class OutletMapFragment extends BaseFragment {

    private Map<String, Industrialport> outlets = new Hashtable<String, Industrialport>();
    private BaiduMap baiduMap = null;
    MapView mvBaidu = null;
    Location location = RiverListActivity.location;
    double longScope = 0.05;
    double latiScope = 0.05;
    BitmapDescriptor bmp;
    MarkerOptions options = null;
    boolean isFirst = true;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null){
            rootView = mvBaidu = new MapView(getBaseActivity());
            baiduMap = mvBaidu.getMap();
            baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);
            onHeadRefresh(true);

            baiduMap.setMaxAndMinZoomLevel(Values.MAP_ZOOM_MAX_LEVEL, 10);
            baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

                @Override
                public void onMapStatusChangeStart(MapStatus arg0) {

                }

                @Override
                public void onMapStatusChangeFinish(MapStatus status) {
                    if (location.getLatitude() != status.target.latitude || location.getLongitude() != status.target.longitude) {
                        location.setLatitude(status.target.latitude);
                        location.setLongitude(status.target.longitude);
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
                    String sid = m.getTitle();
                    //哈希表 containsKey
                    if (outlets.containsKey(sid)) {
                        Industrialport s = outlets.get(sid);
                        Intent intent = new Intent(getBaseActivity(), OutletActivity.class);
                        intent.putExtra(Tags.TAG_OUTLET, StrUtils.Obj2Str(s));
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

    public void onHeadRefresh(boolean b) {
        if (b)
            showOperating();
        //Projection: 获取屏幕像素坐标和经纬度对之间的转换
        Projection projection = baiduMap.getProjection();
        if (projection != null){
            LatLng ll1 = projection.fromScreenLocation(new Point(0, 0));
            LatLng ll2 = projection.fromScreenLocation(new Point(mvBaidu.getWidth(), mvBaidu.getHeight()));
            latiScope = Math.abs(ll1.latitude - ll2.latitude);
            longScope = Math.abs(ll1.longitude - ll2.longitude);
        }
        JSONObject p = null;
        p = location != null ? ParamUtils.freeParam(null, "longtitude", location.getLongitude(), "latitude", location.getLatitude(),
                "longScope", longScope, "latiScope", latiScope) : ParamUtils.freeParam(null, "longScope", longScope, "latiScope", latiScope);

        getRequestContext().add("Get_IndustrialLocationMap", new Callback<OutletLocationsRes>() {
            @Override
            public void callback(OutletLocationsRes o) {

                if ( o!= null && o.isSuccess()){
                    if (location == null){

                        location = new Location("");
                        location.setLatitude(o.data.centerLati);
                        location.setLongitude(o.data.centerLongti);
                        baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(location.getLatitude()).
                                longitude(location.getLongitude()).build());
                        MapStatus status = new MapStatus.Builder().target(new LatLng(location.getLatitude(),
                                location.getLongitude())).build();
                        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));

                    }

                    if (bmp == null){
                        bmp = BitmapDescriptorFactory.fromResource(R.drawable.umeng_socialize_location_ic);
                    }

                    synchronized (outlets){
                        baiduMap.clear();
                        for (Industrialport outlet : o.data.industrialPortJsons){
                            if (outlet.latitude == 0 && outlet.longitude == 0)
                                continue;
                            options = new MarkerOptions().position(new LatLng(outlet.latitude,
                                    outlet.longitude)).icon(bmp).title("" + outlet.sourceId);
                            baiduMap.addOverlay(options);

                            outlets.put(outlet.sourceId, outlet);
                        }
                    }
                }

                hideOperating();

                if (location != null){
                    baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(location.getLatitude()).longitude(location.getLongitude()).build());
                }

                if (isFirst) {
                    isFirst = false;
                    onHeadRefresh(false);
                }

            }
        }, OutletLocationsRes.class, p);


    }
}
