package com.zzteck.msafe.activity;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager;

import com.zzteck.msafe.R;
import com.zzteck.msafe.application.AppContext;
import com.zzteck.msafe.bean.DeviceSetInfo;
import com.zzteck.msafe.bean.NotificationBean;
import com.zzteck.msafe.db.DatabaseManager;
import com.zzteck.msafe.impl.ICallBack;
import com.zzteck.msafe.service.BgMusicControlService;
import com.zzteck.msafe.service.BluetoothLeService;
import com.zzteck.msafe.util.AlarmManager;
import com.zzteck.msafe.util.Constant;
import com.zzteck.msafe.util.ShakeListener;
import com.zzteck.msafe.util.ShakeListener.OnShakeListener;
import com.zzteck.msafe.view.FollowAlarmActivity;

/**
 * @author liujw
 * @description:
 *
 */
public class BaseActivity extends FragmentActivity implements ICallBack{

	public Context mContext ;
	
	private AlarmManager mAlarmManager ;
	
	private DatabaseManager mDatabaseManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		
		
		mContext = BaseActivity.this ;
		mAlarmManager = AlarmManager.getInstance(mContext);
		mDatabaseManager = DatabaseManager.getInstance(mContext);
		initShackListener();
		mNotificationMnager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	private ShakeListener mShakeListener;
	
	
	private void initShackListener() {
		mShakeListener = new ShakeListener(mContext);
		mShakeListener.setOnShakeListener(shakeListener);
	}
	
	OnShakeListener shakeListener = new OnShakeListener() {

		@Override
		public void onShake() {
			Intent intentDistance = new Intent(BgMusicControlService.CTL_ACTION);
			intentDistance.putExtra("control", 3);
			sendBroadcast(intentDistance);
		}
	};
	
	private NotificationManager mNotificationMnager ;
	
	protected void  disconnectStatus() {
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());
		NotificationBean bean = AppContext.mNotificationBean;
		if(bean.isShowNotificationDialog()){
			ArrayList<DeviceSetInfo> list = mDatabaseManager.selectDeviceInfo(bean.getAddress());
			DeviceSetInfo info = null;
			if (list.size() > 0) {
				info = list.get(0);
			}
			alarmDialog(BaseActivity.this,info,bean.getAlarmInfo(),bean.getAlarmType());
			if(bean.getAlarmType() == Constant.DISCONNECT){
				disconnectStatus();
			}
			bean.setShowNotificationDialog(false);
			
		}
		mNotificationMnager.cancel(bean.getNotificationID());
		setShakeConfig();
	}
	
	public void setShakeConfig() {
		SharedPreferences settings = mContext.getSharedPreferences("config", 0);
		
		Log.e("liujw","###################setShakeConfig#"+settings.getBoolean("switch", true));
		if (settings.getBoolean("switch", true)) {
			mShakeListener.start();
		} else {
			mShakeListener.stop();
		}
		Log.e("liujw","###############setShakeConfig");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_RSSI);
		return intentFilter;
	}

	private Handler mHandler = new Handler();
	
	private Intent mIntent ;
	
	
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			mIntent = intent ;
			if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				
			} else if (BluetoothLeService.ACTION_GATT_RSSI.equals(action)) { // 超距离报警
				
			} else if (BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE.equals(action)) { // 设备寻找手机报警
				
			}else if(BluetoothLeService.ACTION_READ_DATA_AVAILABLE.equals(action)){
				progressBatteryData(intent);
			}else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				
				if(mIUpdateConnectStatus != null){
					mIUpdateConnectStatus.updateConnectStatus(0);
				}
				
			}
		}
	};
	
	private void progressBatteryData(Intent intent){
		String deviceAddress = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
		ArrayList<DeviceSetInfo>  list = mDatabaseManager.selectDeviceInfo(deviceAddress);
		byte[] msg = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
		if (msg != null) {
			String message = msg.toString();
			int battery = Integer.parseInt(message);
			if(Integer.parseInt(message) < 30){
				DeviceSetInfo info = list.get(0);
				alarmDialog(BaseActivity.this, info, mContext.getString(R.string.battery),Constant.READBATTERY);	
			};
		}
	}
	
	private void dismissBleActivity() {
		Intent intent = new Intent(Constant.DIALOG_FINISH);
		sendBroadcast(intent);
	}
	
	
	public void alarmDialog(Context context ,DeviceSetInfo info,String alarmInfo, int type) {
		Intent intent = null ;
		switch (type) {
		case Constant.DISCONNECT:
			intent = new Intent(context,FollowAlarmActivity.class);
			intent.putExtra("type", type);
			intent.putExtra("alarm_info", alarmInfo);
			intent.putExtra("deviceinfo", info);
			startActivity(intent);
			break;
		case Constant.DISTANCE:
			
			dismissBleActivity();
			
			intent = new Intent(context,FollowAlarmActivity.class);
			intent.putExtra("type", type);
			intent.putExtra("alarm_info", alarmInfo);
			intent.putExtra("deviceinfo", info);
			startActivity(intent);
			break;
		case Constant.SENDDATA:
			
			dismissBleActivity();
			intent = new Intent(context,FollowAlarmActivity.class);
			intent.putExtra("type", type);
			intent.putExtra("alarm_info", alarmInfo);
			intent.putExtra("deviceinfo", info);
			startActivity(intent);
			
			break;
		case Constant.READBATTERY:
			
			dismissBleActivity();
			
			intent = new Intent(context,FollowAlarmActivity.class);
			intent.putExtra("type", type);
			intent.putExtra("alarm_info", alarmInfo);
			intent.putExtra("deviceinfo", info);
			startActivity(intent);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void setFollowDialog(DeviceSetInfo info ,int type) {
		
	}
	
	private IUpdateConnectStatus mIUpdateConnectStatus ;
	
	public IUpdateConnectStatus getmIUpdateConnectStatus() {
		return mIUpdateConnectStatus;
	}

	public void setmIUpdateConnectStatus(IUpdateConnectStatus mIUpdateConnectStatus) {
		this.mIUpdateConnectStatus = mIUpdateConnectStatus;
	}

	public interface IUpdateConnectStatus{
		
		public void updateConnectStatus(int status);
		
	}
	
}
