package com.zju.hzsz.utils;

import android.content.Context;
import android.location.LocationManager;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZJTLM4600l on 2018/10/26.
 */

public class PatrolRecordUtils {
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static boolean isOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return gps;
    }

    /*
    进行轨迹切分
    返回与其后面的点距离过大的点所在的位置
     */
    public static List<Integer> segmentTrack(List<LatLng> pointsToSegment){
        List<Integer> out = new ArrayList<Integer>();
        if(pointsToSegment.size()<=1){
            return out;
        }
        for(int i  = 0; i<pointsToSegment.size()-1;i++){
            if(DistanceUtil.getDistance(pointsToSegment.get(i),pointsToSegment.get(i+1))>=200){
                out.add(i);
            }
        }
        return out;
    }
}
