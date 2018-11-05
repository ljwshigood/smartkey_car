
package com.zzteck.msafe.activity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zzteck.msafe.R;
import com.zzteck.msafe.adapter.CategoryAdapter;
import com.zzteck.msafe.application.AppContext;
import com.zzteck.msafe.bean.Category;
import com.zzteck.msafe.bean.DeviceSetInfo;
import com.zzteck.msafe.bean.DisturbInfo;
import com.zzteck.msafe.bean.SoundInfo;
import com.zzteck.msafe.db.DatabaseManager;
import com.zzteck.msafe.dialog.SystemHintsDialog_dialog;
import com.zzteck.msafe.impl.IDismissListener;
import com.zzteck.msafe.service.BluetoothLeService;
import com.zzteck.msafe.util.AlarmManager;
import com.zzteck.msafe.view.FollowProgressDialog;
import com.zzteck.msafe.view.SystemHintsDialog;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
@SuppressLint("NewApi")
public class DeviceScanActivity extends Activity implements OnClickListener ,IDismissListener{

	//private LeDeviceListAdapter mLeDeviceListAdapter;
	private CategoryAdapter mLeDeviceListAdapter ;

	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 10000;
	
	private ListView mLvBlueDevice;

	private Context mContext;

	private BluetoothDevice mDevice;

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				String address = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
				
				Log.e("liujw","#########################ACTION_GATT_SERVICES_DISCOVERED");
				
				if (mDialogProgress != null) {
					mDialogProgress.dismiss();
				}
				
				if (AppContext.mBluetoothLeService != null) {
					displayGattServices(AppContext.mBluetoothLeService.getSupportedGattServices(),address);
				}
			}
		}
	};

	private void displayGattServices(List<BluetoothGattService> gattServices,
			String address) {
		if (gattServices == null)
			return;
		for (BluetoothGattService gattService : gattServices) {
			if (gattService.getUuid().toString().startsWith("0000fff0")) {
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					if (gattCharacteristic.getUuid().toString().startsWith("0000fff1")) {
						AppContext.mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
						saveDatabaseAndStartActivity();
					}
				}
			}
		}
	}

	private ImageView mIvBack;

	private AudioManager mAudioManager;

	private Handler delayHandler;

	private AlarmManager mAlarmManager;

	private Category mCategoryDataabase ,mCategoryScanner ;


	private ArrayList<Category> getData() {

		ArrayList<Category> listData = new ArrayList<Category>();
		Category categoryOne = new Category("路人甲");
		DeviceSetInfo deviceSetInfo = new DeviceSetInfo() ;
		deviceSetInfo.setmDeviceName("马三立");
		categoryOne.addItem(deviceSetInfo);
		DeviceSetInfo deviceSetInfo1 = new DeviceSetInfo() ;
		deviceSetInfo1.setmDeviceName("赵本山");
		categoryOne.addItem(deviceSetInfo1);

		DeviceSetInfo deviceSetInfo2 = new DeviceSetInfo() ;
		deviceSetInfo2.setmDeviceName("赵本山");

		categoryOne.addItem(deviceSetInfo2);

		DeviceSetInfo deviceSetInfo3 = new DeviceSetInfo() ;
		deviceSetInfo3.setmDeviceName("周立波");

		categoryOne.addItem(deviceSetInfo3);

		Category categoryTwo = new Category("事件乙");

		DeviceSetInfo deviceSetInfo4 = new DeviceSetInfo() ;
		deviceSetInfo4.setmDeviceName("**贪污");

		categoryTwo.addItem(deviceSetInfo4);

		DeviceSetInfo deviceSetInfo5 = new DeviceSetInfo() ;
		deviceSetInfo5.setmDeviceName("**照门");

		categoryTwo.addItem(deviceSetInfo5);
		/*
		Category categoryThree = new Category("书籍丙");
		categoryThree.addItem("10天学会***");
		categoryThree.addItem("**大全");
		categoryThree.addItem("**秘籍");
		categoryThree.addItem("**宝典");
		categoryThree.addItem("10天学会***");
		categoryThree.addItem("10天学会***");
		categoryThree.addItem("10天学会***");
		categoryThree.addItem("10天学会***");
		Category categoryFour = new Category("书籍丙");
		categoryFour.addItem("河南");
		categoryFour.addItem("天津");
		categoryFour.addItem("北京");
		categoryFour.addItem("上海");
		categoryFour.addItem("广州");
		categoryFour.addItem("湖北");
		categoryFour.addItem("重庆");
		categoryFour.addItem("山东");
		categoryFour.addItem("陕西");*/

		listData.add(categoryOne);
		listData.add(categoryTwo);
		/*listData.add(categoryThree);
		listData.add(categoryFour);*/

		return listData;
	}


	private ArrayList<Category> listData = new ArrayList<Category>();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mHandler = new Handler();

		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		setContentView(R.layout.activity_device_scan);
		mLvBlueDevice = (ListView) findViewById(R.id.lv_scan_device);
		mContext = this ;

		mCategoryDataabase = new Category("已连接设备") ;
		mCategoryScanner = new Category("扫描设备") ;

		// Initializes list view adapter.
		/*mLeDeviceListAdapter = new LeDeviceListAdapter();
		mLvBlueDevice.setAdapter(mLeDeviceListAdapter);
		mLvBlueDevice.setOnItemClickListener(mLeDeviceListAdapter);*/

		mLeDeviceListAdapter = new CategoryAdapter(mContext,listData) ;
		mLvBlueDevice.setAdapter(mLeDeviceListAdapter) ;
		mLvBlueDevice.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
				if(AppContext.mHashMapConnectGatt.size() > 0){
					Toast.makeText(mContext, mContext.getString(R.string.already_one_device_conncect), 1).show();
					return ;
				}

				SystemHintsDialog_dialog mSystemDialog  = new SystemHintsDialog_dialog(mContext,"确定要配对吗",mContext.getString(R.string.cancel),mContext.getString(R.string.ok),0);
				mSystemDialog.setmIDialogListener(new SystemHintsDialog_dialog.IDialogListener() {
					@Override
					public void dialogOk() {

						/*final Category device = */mLeDeviceListAdapter.getItem(position);
						Log.e("liujw","##########################device : onItemClick : "+mLeDeviceListAdapter.getItem(position)) ;
						/*if (device == null)
							return;
						if (AppContext.mBluetoothLeService != null) {
							AppContext.mBluetoothLeService.connect(device.getAddress());
						}*/
						showProgressBarDialog();
					}
				});
				mSystemDialog.show();

				//final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
				/*final Category device = mLeDeviceListAdapter.getItem(position);
				mDevice = device;
				if (device == null)
					return;
				if (AppContext.mBluetoothLeService != null) {
					AppContext.mBluetoothLeService.connect(device.getAddress());
				}*/
				//showProgressBarDialog();
				//isConnectedTimeout();
			}
		}) ;

		mAlarmManager = AlarmManager.getInstance(mContext);
		initView();

		delayHandler = new Handler();

		setTitle(mContext.getString(R.string.device_scanning));
		
		if(AppContext.mBluetoothLeService != null){
			AppContext.mBluetoothLeService.setmIDismissListener(this);
		}

		mLeDeviceListAdapter.notifyCategoryList(getData());
		/*ArrayList<DeviceSetInfo> deviceList = DatabaseManager.getInstance(mContext).selectDeviceInfo() ;
		if(deviceList != null && deviceList.size() > 0){
			for(int i = 0 ;i < deviceList.size() ;i++){
				mCategoryDataabase.addItem(deviceList.get(i)) ;
			}
			listData.add(mCategoryDataabase) ;
			mLeDeviceListAdapter.notifyDataSetChanged();
		}*/

	}

	private View mView;

	private TextView mTvTitleInfo;

	private void setTitle(String info) {
		mView = (View) findViewById(R.id.include_head);
		mTvTitleInfo = (TextView) mView.findViewById(R.id.tv_title_info);
		mTvTitleInfo.setText(info);
	}

	private LinearLayout mLLInfo ;

	private RelativeLayout mRlTitle ;
	
	private void initView() {
		mRlTitle = (RelativeLayout)findViewById(R.id.rl_title);
		mRlTitle.setBackgroundResource(R.drawable.bg_scanning);
		mLLInfo = (LinearLayout)findViewById(R.id.ll_info);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mIvBack.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		scanLeDevice(true);
	}
	


	@Override
	protected void onDestroy() {
		scanLeDevice(false);
		if (mDialogProgress != null) {
			mDialogProgress.dismiss();
			mDialogProgress = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		unregisterReceiver(mGattUpdateReceiver);
		
	}

	/**
	 * @param enable
	 */
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			/*mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);*/

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private ArrayList<BluetoothDevice> mLeDevices;

	private DatabaseManager mDatabaseManager;

	private void saveDatabaseAndStartActivity() {
		if(mDevice == null){
			return ;
		}
		mDatabaseManager = DatabaseManager.getInstance(mContext);
		mDatabaseManager.deleteAllDeviceInfo();
		// query mac address
		ArrayList<DeviceSetInfo> deviceList = mDatabaseManager.selectDeviceInfo(mDevice.getAddress());
		if (deviceList.size() == 0) {
			DeviceSetInfo info = new DeviceSetInfo();
			info.setDistanceType(2);
			info.setDisturb(false);
			info.setFilePath(null);
			info.setLocation(true);
			info.setmDeviceAddress(mDevice.getAddress());
			info.setmDeviceName(mDevice.getName());
			info.setConnected(true);
			info.setVisible(false);
			info.setActive(true);
			info.setLat(String.valueOf(AppContext.mLatitude));
			info.setLng(String.valueOf(AppContext.mLongitude));
			DisturbInfo disturbInfo = new DisturbInfo();
			disturbInfo.setDisturb(false);
			disturbInfo.setEndTime("23:59");
			disturbInfo.setStartTime("00:00");
			SoundInfo soundInfo = new SoundInfo();
			soundInfo.setDurationTime(180);
			soundInfo.setRingId(R.raw.crickets);
			soundInfo.setRingName(mContext.getString(R.string.ringset_qsmusic));
			soundInfo.setRingVolume(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			soundInfo.setShock(true);
			mDatabaseManager.insertDeviceInfo(mDevice.getAddress(), info);
			mDatabaseManager.insertDisurbInfo(mDevice.getAddress(), disturbInfo);
			mDatabaseManager.insertSoundInfo(mDevice.getAddress(), soundInfo);
		}
		if (mScanning) {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanning = false;
		}
		Intent intent = new Intent(mContext, DeviceDisplayActivity.class);
		intent.putExtra(DeviceDisplayActivity.EXTRAS_DEVICE_NAME,mDevice.getName());
		intent.putExtra(DeviceDisplayActivity.EXTRAS_DEVICE_ADDRESS,mDevice.getAddress());
		intent.putExtra("device", mDevice);
		setResult(DeviceDisplayActivity.RESULT_ADRESS, intent);
		finish();
	}

	/*private class LeDeviceListAdapter extends BaseAdapter implements OnItemClickListener {

		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = DeviceScanActivity.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			if (view == null) {
				view = mInflator.inflate(R.layout.list_item_device, null);
				viewHolder = new ViewHolder();
				viewHolder.ivDevice = (ImageView) view.findViewById(R.id.iv_device);
				viewHolder.deviceName = (TextView) view.findViewById(R.id.tv_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);

			DeviceSetInfo info = new DeviceSetInfo();
			info.setFilePath("null");
			Bitmap circleBitmap = mAlarmManager.getDeviceBitmap(info, mContext);
			viewHolder.ivDevice.setImageBitmap(circleBitmap);

			return view;
		}
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) {
			
			if(AppContext.mHashMapConnectGatt.size() > 0){
				Toast.makeText(mContext, mContext.getString(R.string.already_one_device_conncect), 1).show();
				return ;
			}
			
			final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
			mDevice = device;
			if (device == null)
				return;
			if (AppContext.mBluetoothLeService != null) {
				AppContext.mBluetoothLeService.connect(device.getAddress());
			}
			showProgressBarDialog();
			//isConnectedTimeout();
		}
	}*/

	public FollowProgressDialog mDialogProgress = null;

	private void showProgressBarDialog() {
		String info = mContext.getString(R.string.device_connected_title);
		mDialogProgress = new FollowProgressDialog(mContext, R.style.MyDialog,info);
		mDialogProgress.show();
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLvBlueDevice.setVisibility(View.VISIBLE);
					mLLInfo.setVisibility(View.GONE) ;
					/*if(TextUtils.isEmpty(device.getName())){
						return ;
					}*/
				/*	if(mCategoryScanner.getmCategoryItem() != null && mCategoryScanner.getmCategoryItem().size() > 0){
						List<DeviceSetInfo> list = mCategoryScanner.getmCategoryItem();
						boolean isExist = false ;
						for(int i = 0 ;i < list.size();i++){
							String address = list.get(i).getmDeviceAddress() ;
							if(address.equals(device.getAddress())){
								isExist = true ;
							}
						}
						if(!isExist){
							DeviceSetInfo bean = new DeviceSetInfo() ;
							bean.setmDeviceAddress(device.getAddress()) ;
							bean.setmDeviceName(device.getName()) ;
							mCategoryScanner.addItem(bean) ;
							mLeDeviceListAdapter.notifyDataSetChanged();
						}
					}else{
						DeviceSetInfo bean = new DeviceSetInfo() ;
						bean.setmDeviceAddress(device.getAddress()) ;
						bean.setmDeviceName(device.getName()) ;
						mCategoryScanner.addItem(bean) ;
						listData.add(mCategoryScanner) ;
						mLeDeviceListAdapter.notifyDataSetChanged();
					}*/

				}
			});
		}
	};
	
	public boolean iteraGattHashMap(Map map, String address) {
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (key.toString().equals(address)) {
				return true;
			}
		}
		return false;
	}

	static class ViewHolder {
		TextView deviceName;
		ImageView ivDevice;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		default:
			break;
		}
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE);
		return intentFilter;
	}

	@Override
	public void dismiss() {
		if (mDialogProgress != null) {
			mDialogProgress.dismiss();
		}
	}

}