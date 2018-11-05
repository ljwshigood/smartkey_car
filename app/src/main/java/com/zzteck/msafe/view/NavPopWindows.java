package com.zzteck.msafe.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.zzteck.msafe.R;
import com.zzteck.msafe.activity.AboutMeActivity;
import com.zzteck.msafe.activity.DeviceDisplayActivity;
import com.zzteck.msafe.activity.DeviceInfoActivity;
import com.zzteck.msafe.activity.FeedBackActivity;
import com.zzteck.msafe.service.BluetoothLeService;
import com.zzteck.msafe.util.SharePerfenceUtil;


public class NavPopWindows extends PopupWindow implements View.OnClickListener{
	
	private View mMenuView ;

	private LinearLayout mLLUpdateVersion, mLLFeedBack, mLLVibatte, mLLDeviceList, mLLAboutMe ;

	private Context mContext ;

	private CheckBox mCbVibrate ;

	public NavPopWindows(Activity context){
		
		super(context) ;
		mContext  = context ;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.popwindows_right, null);

		mLLUpdateVersion =  mMenuView.findViewById(R.id.ll_update) ;
		mLLFeedBack = mMenuView.findViewById(R.id.ll_feedback);
		mLLVibatte = mMenuView.findViewById(R.id.ll_vibration) ;
		mLLDeviceList =  mMenuView.findViewById(R.id.ll_device_list) ;
		mLLAboutMe =  mMenuView.findViewById(R.id.ll_about_me)  ;
		mCbVibrate = mMenuView.findViewById(R.id.cb_vibration) ;
		mLLUpdateVersion.setOnClickListener(this) ;
		mLLVibatte.setOnClickListener(this) ;
		mLLFeedBack.setOnClickListener(this) ;
		mLLDeviceList.setOnClickListener(this) ;
		mLLAboutMe.setOnClickListener(this) ;
		mCbVibrate.setOnClickListener(this);
		this.setContentView(mMenuView);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		
		this.setBackgroundDrawable(dw);
		
		mMenuView.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				int height = mMenuView.findViewById(R.id.pop_layout).getTop();
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y<height){
						dismiss();
					}
				}				
				return true;
			}
		});
		String isVibrate = (String) SharePerfenceUtil.getParam(mContext,"vibrate","0");
		if(isVibrate.equals("0")){
			mCbVibrate.setChecked(false);
		}else{
			mCbVibrate.setChecked(true);
		}

	}

	@Override
	public void onClick(View v) {
		Intent intent = null ;
		switch (v.getId()){
			case R.id.ll_device_list :
				intent = new Intent(mContext, DeviceInfoActivity.class) ;
				mContext.startActivity(intent) ;
				break ;
			case R.id.ll_update :

				intent = new Intent(BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE) ;
				intent.putExtra(BluetoothLeService.EXTRA_DATA,"E3 07 A1 01 01 A3 E5") ;
				mContext.sendBroadcast(intent);
				//Toast.makeText(mContext,"当前已经是最新版本",1).show();
				break ;
			case R.id.ll_about_me :

				/*intent = new Intent(BluetoothLeService.ACTION_NOTIFY_DATA_AVAILABLE) ;
				intent.putExtra(BluetoothLeService.EXTRA_DATA,"E3 07 A1 01 01 A1 E5") ;
				mContext.sendBroadcast(intent);*/

				intent = new Intent(mContext, AboutMeActivity.class) ;
				mContext.startActivity(intent) ;

				break ;
			case R.id.ll_feedback :
				intent = new Intent(mContext, FeedBackActivity.class) ;
				mContext.startActivity(intent) ;
				break ;
			case R.id.cb_vibration :
				if(mCbVibrate.isChecked()){
					SharePerfenceUtil.setParam(mContext,"vibrate","1");
				}else{
					SharePerfenceUtil.setParam(mContext,"vibrate","0");
				}
				break ;
		}
	}
}
