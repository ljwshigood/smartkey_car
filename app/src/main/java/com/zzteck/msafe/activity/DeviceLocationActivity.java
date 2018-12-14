package com.zzteck.msafe.activity;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.zzteck.msafe.R;
import com.zzteck.msafe.adapter.LocationDeviceAdapter;
import com.zzteck.msafe.bean.DeviceSetInfo;
import com.zzteck.msafe.db.DatabaseManager;
import com.zzteck.msafe.util.LocationUtils;
import com.zzteck.msafe.view.FollowInfoDialog;

import java.util.ArrayList;

public class DeviceLocationActivity extends FragmentActivity implements
		OnItemClickListener, OnClickListener, LocationListener {

	//private OnLocationChangedListener mListener;
	private ListView mListView;
	private TextView btn_find;
	private boolean showFlag = true;

	private DatabaseManager mDatabaseManager;

	private Context mContext;

	private ArrayList<DeviceSetInfo> mDeviceList = new ArrayList<DeviceSetInfo>();

	//private GoogleMap mMap;

	//private LocationClient mLocationClient;
	
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	private RelativeLayout mRlMap ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = DeviceLocationActivity.this;
		mDatabaseManager = DatabaseManager.getInstance(mContext);
		setContentView(R.layout.activity_location);
		mRlMap = (RelativeLayout)findViewById(R.id.rl_map);
		mListView = (ListView) findViewById(R.id.deviceLocationList);
		btn_find = (TextView) findViewById(R.id.btn_find);
		btn_find.setOnClickListener(this);
		if (!LocationUtils.isOPen(mContext)) {
			FollowInfoDialog dialogLocation = new FollowInfoDialog(
					mContext, R.style.MyDialog, null,
					mContext.getString(R.string.open_gps), 1);
			dialogLocation.show();
		}
		
		Toast.makeText(mContext, mContext.getString(R.string.find_device_donot_touch), Toast.LENGTH_SHORT).show();
		
	}

	private void initDeviceList() {
		mDeviceList = mDatabaseManager.selectDeviceInfoByLocation();
		LocationDeviceAdapter mFindDeviceAdapter = new LocationDeviceAdapter(
				this, mDeviceList);
		mListView.setAdapter(mFindDeviceAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	private Dialog mDialog ;

	@Override
	protected void onResume() {
		super.onResume();
		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
		if (result == ConnectionResult.SUCCESS) {
			mRlMap.setVisibility(View.VISIBLE);
			setUpMapIfNeeded();
			setUpLocationClientIfNeeded();
			//mLocationClient.connect();
			initDeviceList();
		} else {
			if(mDialog == null){
				mDialog = GooglePlayServicesUtil.getErrorDialog(result,DeviceLocationActivity.this, 255);				
			}
			mDialog.show();
		}
	}

	private void setUpMapIfNeeded() {
		/*if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			if (mMap != null) {
				mMap.setMyLocationEnabled(true);
				//mMap.setOnMyLocationButtonClickListener(this);
			}
		}*/
	}

	private void setUpLocationClientIfNeeded() {
		/*if (mLocationClient == null) {
			mLocationClient = new LocationClient(getApplicationContext(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}*/
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		super.onPause();
	//	deactivate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		
		Log.e("DeviceLocationActivity","###############onLocationChanged");

		Log.e("DeviceLocationActivity","###############onLocationChanged");

		Log.e("DeviceLocationActivity","###############onLocationChanged");
		if (location != null) {
			geoLat = location.getLatitude();
			geoLng = location.getLongitude();
			/*mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(geoLat),
					Double.valueOf(geoLng)), 18f));
			
			if (mLocationClient != null) {
	            mLocationClient.disconnect();
			}*/

		}
	}

	private Double geoLat = 0.0;

	private Double geoLng = 0.0;

	/*@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
	}*/

	/**
	 * 停止定位
	 * 
	 */
/*	@Override
	public void deactivate() {
		mListener = null;
	}*/

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
		
		if(mDeviceList == null){
			return ;
		}
		
		DeviceSetInfo deviceSetInfo = mDeviceList.get(position);
		if(deviceSetInfo == null){
			return ;
		}
		
		if(deviceSetInfo.getLat() == null || deviceSetInfo.getLat().equals("")){
			return ;
		}
		
		if(deviceSetInfo.getLng() == null || deviceSetInfo.getLng().equals("")){
			return ;
		}
		
	/*	mMap.setLocationSource(this);
		mMap.getUiSettings().setMyLocationButtonEnabled(true);
		mMap.setMyLocationEnabled(true);
		mMap.clear();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(deviceSetInfo.getLat()),
				Double.valueOf(deviceSetInfo.getLng())), 18f));*/

		showFlag = true;
		mListView.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_find:
			if (showFlag) {
				mListView.setVisibility(View.VISIBLE);
				showFlag = false;
			} else {
				showFlag = true;
				mListView.setVisibility(View.GONE);
			}
			break;
		}
	}

/*	@Override
	public boolean onMyLocationButtonClick() {
		return false;
	}*/

	/*@Override
	public void onConnectionFailed(ConnectionResult result) {

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(REQUEST, this);
	}

	@Override
	public void onDisconnected() {

	}*/

}
