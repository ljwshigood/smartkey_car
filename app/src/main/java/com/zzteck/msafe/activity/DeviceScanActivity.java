
package com.zzteck.msafe.activity;

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
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.zzteck.msafe.bean.MsgEvent;
import com.zzteck.msafe.bean.SoundInfo;
import com.zzteck.msafe.db.DatabaseManager;
import com.zzteck.msafe.dialog.SystemHintsDialog_dialog;
import com.zzteck.msafe.impl.IDismissListener;
import com.zzteck.msafe.service.BluetoothLeService;
import com.zzteck.msafe.util.AlarmManager;
import com.zzteck.msafe.view.FollowProgressDialog;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
@SuppressLint("NewApi")
public class DeviceScanActivity extends Activity implements OnClickListener ,IDismissListener{

	//private LeDeviceListAdapter mLeDeviceListAdapter;
	private CategoryAdapter mLeDeviceListAdapter ;

	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == 0){
				if(mDialogProgress != null){
					mDialogProgress.dismiss();
				}
				Toast.makeText(mContext,"connect time out",1).show();

			}
		}
	};

	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 10000;
	
	private ListView mLvBlueDevice;

	private Context mContext;

	private DeviceSetInfo mDevice;

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				String address = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
				
				Log.e("liujw","#########################ACTION_GATT_SERVICES_DISCOVERED");

				Toast.makeText(getApplicationContext(),"##############ACTION_GATT_SERVICES_DISCOVERED ",1).show();
				if (mDialogProgress != null) {
					mDialogProgress.dismiss();
				}
				
				if (AppContext.mBluetoothLeService != null) {
					displayGattServices(AppContext.mBluetoothLeService.getSupportedGattServices(),address);
				}
			}else if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
				Toast.makeText(getApplicationContext(),"##############ACTION_GATT_CONNECTED ",1).show();
			}
		}
	};

	private void displayGattServices(List<BluetoothGattService> gattServices,String address) {
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


	private boolean isConnectDevice(Context context ,String address){
		ArrayList<DeviceSetInfo> deviceList = DatabaseManager.getInstance(context).selectDeviceInfo(address);
		boolean isExist = false ;
		for(int i = 0;i < deviceList.size();i++){
			DeviceSetInfo info = deviceList.get(i) ;
			if(info.getmDeviceAddress().equals(address) && info.isConnected()){
				isExist = true ;
				break ;
			}
		}
		return isExist ;
	}

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

		EventBus.getDefault().register(this);

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
					Toast.makeText(mContext, mContext.getString(R.string.already_one_device_conncect), Toast.LENGTH_SHORT).show();
					return ;
				}

				if(mLeDeviceListAdapter.getItem(position) instanceof  DeviceSetInfo){

					final DeviceSetInfo deviceSetInfo = (DeviceSetInfo) mLeDeviceListAdapter.getItem(position);
					boolean connectStatus = isConnectDevice(mContext,deviceSetInfo.getmDeviceAddress()) ;

					if(connectStatus){
						SystemHintsDialog_dialog mSystemDialog = new SystemHintsDialog_dialog(mContext, "要与" + deviceSetInfo.getmDeviceName() + "取消配对吗", mContext.getString(R.string.cancel), mContext.getString(R.string.ok), 0);
						mSystemDialog.setmIDialogListener(new SystemHintsDialog_dialog.IDialogListener() {
							@Override
							public void dialogOk() {

								scanLeDevice(false);

								if (deviceSetInfo == null)
									return;

								if (AppContext.mBluetoothLeService == null) {
									return;
								}
								DatabaseManager.getInstance(mContext).deleteAllDeviceInfo(deviceSetInfo.getmDeviceAddress());
								AppContext.mBluetoothLeService.disconnect();
								AppContext.mBluetoothLeService.close();

								ArrayList<DeviceSetInfo> deviceList = DatabaseManager.getInstance(mContext).selectDeviceInfo() ;
								if(deviceList != null && deviceList.size() > 0){
									for(int i = 0 ;i < deviceList.size() ;i++){
										mCategoryDataabase.addItem(deviceList.get(i)) ;
									}
									listData.add(mCategoryDataabase) ;
									mLeDeviceListAdapter.notifyDataSetChanged();
								}else{
									listData.remove(mCategoryDataabase) ;
									mLeDeviceListAdapter.notifyDataSetChanged();
								}

								EventBus.getDefault().post(new MsgEvent("",5));
							}
						});
						mSystemDialog.show();
					}else {

						SystemHintsDialog_dialog mSystemDialog = new SystemHintsDialog_dialog(mContext, "要与" + deviceSetInfo.getmDeviceName() + "配对吗", mContext.getString(R.string.cancel), mContext.getString(R.string.ok), 0);
						mSystemDialog.setmIDialogListener(new SystemHintsDialog_dialog.IDialogListener() {
							@Override
							public void dialogOk() {

								if(AppContext.mBluetoothLeService != null && AppContext.mBluetoothLeService.isConnect()){
									Toast.makeText(mContext, mContext.getString(R.string.already_one_device_conncect), Toast.LENGTH_SHORT).show();
									return ;
								}

								if (deviceSetInfo == null)
									return;
								if (AppContext.mBluetoothLeService != null) {
									mDevice = deviceSetInfo;
									AppContext.mBluetoothLeService.connect(deviceSetInfo.getmDeviceAddress());
								}
								showProgressBarDialog();
							}
						});
						mSystemDialog.show();
					}
				}
			}
		}) ;

		mAlarmManager = AlarmManager.getInstance(mContext);
		initView();

		setTitle(mContext.getString(R.string.device_list));

		initDeviceList();

		scanLeDevice(true);

	}

	private void initDeviceList(){
		ArrayList<DeviceSetInfo> deviceList = DatabaseManager.getInstance(mContext).selectDeviceInfo() ;
		if(deviceList != null && deviceList.size() > 0){
			for(int i = 0 ;i < deviceList.size() ;i++){
				mCategoryDataabase.addItem(deviceList.get(i)) ;
			}
			listData.add(mCategoryDataabase) ;
			mLeDeviceListAdapter.notifyDataSetChanged();
		}
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

	private LinearLayout mLlRefresh ;
	
	private void initView() {
		mLlRefresh = (LinearLayout)findViewById(R.id.ll_refresh) ;
		mRlTitle = (RelativeLayout)findViewById(R.id.rl_title);
		mLLInfo = (LinearLayout)findViewById(R.id.ll_info);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mIvBack.setOnClickListener(this);
		mLlRefresh.setOnClickListener(this);
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
	}
	


	@Override
	protected void onDestroy() {
		scanLeDevice(false);
		if (mDialogProgress != null) {
			mDialogProgress.dismiss();
			mDialogProgress = null;
		}
		super.onDestroy();
		EventBus.getDefault().unregister(this);
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
			/*mHandler.postDelayed(new Runnableo {
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

	@Subscriber
	public void onEventMainThread(MsgEvent msg){
		mLeDeviceListAdapter.notifyDataSetChanged();
	}

	private ArrayList<BluetoothDevice> mLeDevices;

	private DatabaseManager mDatabaseManager;

	private void saveDatabaseAndStartActivity() {
		if(mDevice == null){
			return ;
		}
		mDatabaseManager = DatabaseManager.getInstance(mContext);
		//mDatabaseManager.deleteAllDeviceInfo();
		// query mac address
		ArrayList<DeviceSetInfo> deviceList = mDatabaseManager.selectDeviceInfo(mDevice.getmDeviceAddress());
		if (deviceList.size() == 0) {
			DeviceSetInfo info = new DeviceSetInfo();
			info.setDistanceType(2);
			info.setDisturb(false);
			info.setFilePath(null);
			info.setLocation(true);
			info.setmDeviceAddress(mDevice.getmDeviceAddress());
			info.setmDeviceName(mDevice.getmDeviceName());
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
			mDatabaseManager.insertDeviceInfo(mDevice.getmDeviceAddress(), info);
			mDatabaseManager.insertDisurbInfo(mDevice.getmDeviceAddress(), disturbInfo);
			mDatabaseManager.insertSoundInfo(mDevice.getmDeviceAddress(), soundInfo);

		}else{ // 修改连接
			mDatabaseManager.updateDeviceConnect(mDevice.getmDeviceAddress(),1);
		}


		if (mScanning) {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mScanning = false;
		}
		Intent intent = new Intent(mContext, DeviceDisplayActivity.class);
		intent.putExtra(DeviceDisplayActivity.EXTRAS_DEVICE_NAME,mDevice.getmDeviceName());
		intent.putExtra(DeviceDisplayActivity.EXTRAS_DEVICE_ADDRESS,mDevice.getmDeviceAddress());
		intent.putExtra("device", mDevice);
		setResult(DeviceDisplayActivity.RESULT_ADRESS, intent);
		finish();
	}

	public FollowProgressDialog mDialogProgress = null;

	private void showProgressBarDialog() {
		String info = mContext.getString(R.string.device_connected_title);
		mDialogProgress = new FollowProgressDialog(mContext, R.style.MyDialog,info);
		mDialogProgress.show();

		mHandler.sendEmptyMessageDelayed(0,10000) ;
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
					if(TextUtils.isEmpty(device.getName())){
						return ;
					}

					if(DatabaseManager.getInstance(mContext).isExistDeviceInfo(device.getAddress())){
						return ;
					}
					if(mCategoryScanner.getmCategoryItem() != null && mCategoryScanner.getmCategoryItem().size() > 0){
						List<DeviceSetInfo> list = mCategoryScanner.getmCategoryItem();
						boolean isExist = false ;
						for(int i = 0 ;i < list.size();i++){
							String address = list.get(i).getmDeviceAddress() ;
							if(address.equals(device.getAddress())){
								isExist = true ;
								break ;
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
					}

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
		case R.id.ll_refresh:
			if(listData != null){
				listData.remove(mCategoryScanner) ;
				mCategoryScanner.getmCategoryItem().clear();
				mLeDeviceListAdapter.notifyDataSetChanged();
			}

			mBluetoothAdapter.startLeScan(mLeScanCallback);
			break ;
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