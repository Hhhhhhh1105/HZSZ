package com.zju.hzsz.service;

import java.util.Random;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.igexin.sdk.PushManager;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.model.User;
import com.zju.hzsz.utils.PreferencesWarp;

public class LocalService extends Service {
	static String TAG = "SERVICE";

	private static User user = null;
	private static SharedPreferences preferences = null;
	private PreferencesWarp preferencesWarp = null;
	private LocationClient locationClient = null;

	protected SharedPreferences getLocalStorage() {
		if (preferences == null) {
			preferences = this.getSharedPreferences("preferences", MODE_APPEND);
		}
		return preferences;
	}

	protected PreferencesWarp getPreferencesWarp() {
		if (preferencesWarp == null)
			preferencesWarp = new PreferencesWarp(this, getLocalStorage());
		return preferencesWarp;
	}

	public User getUser() {
		if (user == null) {
			user = getPreferencesWarp().getObj(Tags.TAG_USER, User.class);
			if (user == null)
				user = new User();
		}
		return user;
	}

	public class LocalBinder extends Binder {
		public LocalService getService() {
			return LocalService.this;
		}
	}

	LocalBinder binder = null;

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
		binder = new LocalBinder();
		PushManager.getInstance().initialize(this.getApplicationContext());
		notifyableChanged();
	}

	public void notifyableChanged() {
		user = null;
		if (getUser().isNotifyable()) {
			PushManager.getInstance().turnOnPush(this);
		} else {
			PushManager.getInstance().turnOffPush(this);
		}
		// Toast.makeText(this, "isNotifyable=" + getUser().isNotifyable(),
		// Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	public String getCid() {
		return PushManager.getInstance().getClientid(getApplicationContext());
	}

	public interface LocationCallback {
		void callback(Location location);
	}

	public LocationClient getLocation(final LocationCallback callback) {
		return this.getLocation(callback, true);
	}

	public LocationClient getLocation(final LocationCallback callback, final boolean once) {
		// if (Values.DEBUG)
		Toast.makeText(this, "请求定位...", Toast.LENGTH_SHORT).show();
		try {
			if (locationClient != null)
				locationClient.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		locationClient = new LocationClient(this);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		option.setOpenGps(true);
		locationClient.setLocOption(option);

		locationClient.registerLocationListener(new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation location) {
				if (Values.DEBUG)
					Toast.makeText(LocalService.this, "GPS位置回调", Toast.LENGTH_SHORT).show();

				if (location != null && Math.abs(location.getLatitude()) > 0.0000001 && Math.abs(location.getLongitude()) > 0.0000001) {
					final BDLocation locx = location;
					if (Values.DEBUG)
						Toast.makeText(LocalService.this, "GPS位置:" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
					if (callback != null) {
						Location l = new Location("") {
							@Override
							public double getLatitude() {
								return locx.getLatitude();
							}

							public double getLongitude() {
								return locx.getLongitude();
							};
						};
						if (Values.DEBUG) {
							// l = new Location("") {
							// @Override
							// public double getLongitude() {
							// return 120.16416012803 + (new
							// Random().nextInt(100) / 1000.0);
							// }
							//
							// @Override
							// public double getLatitude() {
							// return 30.269324885228 + (new
							// Random().nextInt(100) / 1000.0);
							// }
							// };
						}
						callback.callback(l);
					}
					if (once) {
						locationClient.stop();
					}
				} else {
					if (callback != null)
						callback.callback(null);
					Toast.makeText(LocalService.this, "定位失败!", Toast.LENGTH_LONG).show();
				}
			}
		});
		// locationClient.requestLocation();
		locationClient.start();
		locationClient.requestLocation();
		return locationClient;
	}

	LocationManager locationManager = null;
	LocationCallback locationCallback = null;
	LocationListener locationListener = null;

	public void getLocation2(LocationCallback callback) {
		if (locationManager == null) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(this, "系统设置不允许使用定位!", Toast.LENGTH_LONG).show();
			return;
		}

		locationCallback = callback;

		String provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
		Location location = locationManager.getLastKnownLocation(provider);
		if (callback != null && location != null) {
			if (Values.DEBUG)
				Toast.makeText(LocalService.this, "GPS位置:" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_LONG).show();
			callback.callback(location);
		} else {
			unlistenLocation();
			locationListener = new LocationListener() {
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
				}

				@Override
				public void onProviderEnabled(String provider) {
				}

				@Override
				public void onProviderDisabled(String provider) {
				}

				@Override
				public void onLocationChanged(Location location) {
					if (location != null) {
						if (locationCallback != null)
							locationCallback.callback(location);
						// Log.e("Map", "Location changed : Lat: " +
						// location.getLatitude() + " Lng: " +
						// location.getLongitude());
						if (Values.DEBUG)
							Toast.makeText(LocalService.this, "GPS位置:" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_LONG).show();
						unlistenLocation();
					}
				}
			};
			locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
		}
	}

	public void unlistenLocation() {
		if (locationClient != null)
			locationClient.stop();

		if (locationManager != null && locationListener != null)
			locationManager.removeUpdates(locationListener);
		locationClient = null;
		locationManager = null;
		locationListener = null;
	}
}
