package com.zzteck.msafe.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zzteck.msafe.R;
import com.zzteck.msafe.activity.BaseActivity.IUpdateConnectStatus;
import com.zzteck.msafe.adapter.KeySetAdapter;
import com.zzteck.msafe.adapter.KeySetAdapter.IKeyRemove;
import com.zzteck.msafe.application.AppContext;
import com.zzteck.msafe.bean.DeviceSetInfo;
import com.zzteck.msafe.bean.KeySetBean;
import com.zzteck.msafe.bean.MsgEvent;
import com.zzteck.msafe.db.DatabaseManager;
import com.zzteck.msafe.impl.ComfirmListener;
import com.zzteck.msafe.service.AlarmService;
import com.zzteck.msafe.service.BgMusicControlService;
import com.zzteck.msafe.service.BluetoothLeService;
import com.zzteck.msafe.util.AppManager;
import com.zzteck.msafe.util.PermissionUtils;
import com.zzteck.msafe.view.FollowInfoDialog.IUpdateUI;
import com.zzteck.msafe.view.NavPopWindows;
import com.zzteck.msafe.view.SystemHintsDialog;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

public class KeySetActivity extends BaseActivity  implements IUpdateUI,IKeyRemove,IUpdateConnectStatus,View.OnClickListener{
	
	private Context mContext ;
	
	private KeySetAdapter mKeySetAdapter ;
	
	private DatabaseManager mDataBaseManager ;

	private ArrayList<DeviceSetInfo> mDeviceList = new ArrayList<DeviceSetInfo>();
	
	private DatabaseManager mDatabaseManager ;
	
	@Override
	protected void onPause() {
		super.onPause();
	}


	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//isContinue = false ;
		EventBus.getDefault().unregister(this);
	}


	@Subscriber
	public void onEventMainThread(final MsgEvent event){
		if(event.getType() == 5){
			if(AppContext.mBluetoothLeService != null){
				boolean flag =  AppContext.mBluetoothLeService.isConnect() ;
				if(flag == false){
					mIvMenuPower.setImageResource(R.drawable.ic_ble_unconnect);
				}
			}

		}else if(event.getType() == 6){
			mIvMenuPower.setImageResource(R.drawable.ic_connect);
		}

		if(AppContext.mBluetoothLeService != null){
			boolean flag =  AppContext.mBluetoothLeService.isConnect() ;
			if(flag == false){
				mIvMenuPower.setImageResource(R.drawable.ic_ble_unconnect);
			}
		}

	}


	@Override
	protected void onResume() {
		super.onResume();
		sortKeySetList();
		if(AppContext.mBluetoothLeService != null && AppContext.mBluetoothLeService.isConnect()){
			mIvMenuPower.setImageResource(R.drawable.ic_connect);
		}else{
			mIvMenuPower.setImageResource(R.drawable.ic_ble_unconnect);
		}
	}

	private ImageView mIvMenuPower ;

	private ImageView mIvMenuMode ;

	private ImageView mIvMenuRecord ;

	private ImageView mIvMenuCamera ;

	private ImageView mIvMore ;

	private Button mBtnTest ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.key_set_activity);
		mContext = KeySetActivity.this;
		mDatabaseManager = DatabaseManager.getInstance(mContext);

		mBtnTest = findViewById(R.id.btn_test) ;
		mBtnTest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE) ;
				sendBroadcast(intent);
			}
		});

		mBtnTest.setVisibility(View.GONE) ;

		RxPermissions rxPermissions1 = new RxPermissions(this);

		rxPermissions1.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.CAMERA,
				Manifest.permission.BLUETOOTH,
				Manifest.permission.BLUETOOTH_ADMIN,
				Manifest.permission.RECORD_AUDIO,
				Manifest.permission.BLUETOOTH,
				Manifest.permission.READ_SMS,
				Manifest.permission.SEND_SMS,
				Manifest.permission.CALL_PHONE,
				Manifest.permission.READ_CONTACTS,
				Manifest.permission.BLUETOOTH_ADMIN,
				Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Permission>() {
				@Override
				public void accept(Permission permission) throws Exception {
					if (permission.granted) {
						//Toast.makeText(getApplicationContext(),"您已经授权该权限",1).show();
					}else{
						Toast.makeText(getApplicationContext(),"您没有授权该权限，请在设置中打开授权",Toast.LENGTH_LONG).show();
						finish();
					}
				}
		});

		EventBus.getDefault().register(this);

		Intent intent = new Intent(mContext, BgMusicControlService.class);
		startService(intent);

		Intent intentAlarm = new Intent(mContext, AlarmService.class);
		startService(intentAlarm);

		mDataBaseManager = DatabaseManager.getInstance(mContext);
		sortKeySetList();
		mKeySetAdapter = new KeySetAdapter(mContext,mListKeySet);
		mKeySetAdapter.setmIKeyRemove(this);
		initView();

		setmIUpdateConnectStatus(this);

		if(mListKeySet != null && mListKeySet.size() > 0){
			KeySetBean bean = mListKeySet.get(0) ;
			if(bean.getType() == 1) {
				if (bean.getBitmapString() != null && !bean.getBitmapString().equals("null")) {
					mIvMenuMode.setImageDrawable(AppManager.getProgramBitmapByPackageName(mContext, bean.getBitmapString()));
				}
			}else {
				mIvMenuMode.setImageResource(Integer.valueOf(bean.getBitmapString()));
			}
		}

		//.sendEmptyMessage(0) ;
		if(AppContext.mBluetoothLeService != null && AppContext.mBluetoothLeService.isConnect()){
			mIvMenuPower.setImageResource(R.drawable.ic_connect);
		}else{
			mIvMenuPower.setImageResource(R.drawable.ic_ble_unconnect);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1212){
			mListKeySet = mDataBaseManager.selectKeySet();
			if(mListKeySet != null && mListKeySet.size() > 0){
				KeySetBean bean = mListKeySet.get(0) ;
				if(bean.getType() == 1) {
					if (bean.getBitmapString() != null && !bean.getBitmapString().equals("null")) {
						mIvMenuMode.setImageDrawable(AppManager.getProgramBitmapByPackageName(mContext, bean.getBitmapString()));
					}
				}else {
					mIvMenuMode.setImageResource(Integer.valueOf(bean.getBitmapString()));
				}
			}
		}
	}
	
	private List<KeySetBean> mListKeySet ;
	
	private void sortKeySetList(){
		mListKeySet = mDataBaseManager.selectKeySet();
		Collections.sort(mListKeySet, new ComparatorValues());
	}

	private NavPopWindows mPopWindows ;

	@Override
	public void onClick(View view) {
		Intent intent = null ;
		switch (view.getId()){
			case R.id.iv_more :
				mPopWindows = new NavPopWindows(this) ;
				mPopWindows.showAsDropDown(mIvMore);
				break ;
			case R.id.iv_menu_record :
				/*intent = new Intent(mContext, RecordMenuActivity.class);
				startActivity(intent);*/
				break ;
			case R.id.ll_main_device :
				intent = new Intent(mContext, DeviceScanActivity.class);
				startActivity(intent);
				break ;
			case R.id.ll_mode :
				intent = new Intent(mContext, FunctionDetailActivity.class);
				intent.putExtra("count",  1);
				((Activity) mContext).startActivityForResult(intent,1212);
				break ;
			case R.id.iv_menu_camera :
				/*intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivity(intent);*/
				break ;
		}
	}

	public static final class ComparatorValues implements Comparator<KeySetBean>{

        @Override
        public int compare(KeySetBean object1, KeySetBean object2) {
            int m1= object1.getCount();
            int m2= object2.getCount();
            int result=0;
            if(m1>m2) {
                result=1;
            }
            if(m1<m2){
                result=-1;
            }
            return result;
        }        
        
    }

    private RelativeLayout mRlTitle ;

	private LinearLayout mLLDevice ;

	private LinearLayout mLLMode ;

	private void initView(){
		mLLDevice = findViewById(R.id.ll_main_device) ;
		mLLMode = findViewById(R.id.ll_mode) ;
		mRlTitle = findViewById(R.id.rl_title) ;
		mIvMore = findViewById(R.id.iv_more) ;
		mIvMenuPower = findViewById(R.id.iv_power) ;
		mIvMenuMode = findViewById(R.id.iv_mode) ;
		mIvMenuCamera = findViewById(R.id.iv_menu_camera) ;
		mIvMenuRecord = findViewById(R.id.iv_menu_record) ;

		mIvMenuCamera.setOnClickListener(this);
		mIvMenuRecord.setOnClickListener(this);
		mIvMore.setOnClickListener(this);
		mLLDevice.setOnClickListener(this);
		mLLMode.setOnClickListener(this);
	}
	
	
	@Override
	public void updateUI() {
		
	}

	@Override
	public void remove(int count) {
		
		DatabaseManager.getInstance(mContext).updateKeySet(count);
		mDataBaseManager = DatabaseManager.getInstance(mContext);
		sortKeySetList();
		mKeySetAdapter.notifyDataSetKey(mListKeySet);
	} 

	@Override
	public void updateConnectStatus(int status) {
		if(status == 0){
			/*if(AppContext.mBluetoothLeService != null && AppContext.mBluetoothLeService.isConnect()){
				mKeySetAdapter.notifyKeySetStatusChange(mListKeySet,true);
			}else{
				mKeySetAdapter.notifyKeySetStatusChange(mListKeySet,false);
			}*/
		}
	} 
	
	@Override
	protected void disconnectStatus() {
		super.disconnectStatus();
		mKeySetAdapter.notifyKeySetStatusChange(mListKeySet,false);
	}
	
}
