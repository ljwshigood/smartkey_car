package com.zzteck.msafe.util;


import android.Manifest;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.backup.BackupDataOutput;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.qihoo360.mobilesafe.opti.processclear.SysClearUtils;
import com.zzteck.msafe.R;
import com.zzteck.msafe.activity.AntilostCameraActivity;
import com.zzteck.msafe.activity.BackgroundCameraActivity;
import com.zzteck.msafe.activity.CallActivity;
import com.zzteck.msafe.activity.FlashActivity;
import com.zzteck.msafe.activity.OpenAppActivity;
import com.zzteck.msafe.activity.RecordActivity;
import com.zzteck.msafe.activity.SosActivity;
import com.zzteck.msafe.application.AppContext;
import com.zzteck.msafe.bean.ContactBean;
import com.zzteck.msafe.db.DatabaseManager;
import com.zzteck.msafe.impl.ComfirmListener;
import com.zzteck.msafe.view.SystemHintsDialog;

import java.util.List;

public class KeyFunctionUtil {

	private Context mContext;

	private static KeyFunctionUtil mInstance;

	private DatabaseManager mDatabaseManager;

	private int mMaxAudio ;

	private Camera camera ;

	private Camera.Parameters params;

	private void getCamera() {
		if (camera == null) {
			try {
				camera = Camera.open();
				params = camera.getParameters();
			} catch (RuntimeException e) {
				Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
			}
		}
	}

	private void turnOnFlash() {

		if (camera == null || params == null) {
			showDialog() ;
			return;
		}
		// play sound
		if(!getTopActivity().equals("com.zzteck.msafe.activity.AntilostCameraActivity")){
			params = camera.getParameters();
			params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			camera.setParameters(params);
			camera.startPreview();

			SharePerfenceUtil.setParam(mContext,"camera",1) ;
		}else{
			showDialog() ;
		}

	}

	public void processCamera(boolean light){
		islight = light ;
		if(camera == null){
			getCamera();
			Message message = new Message() ;
			message.obj = 9 ;
			handler.sendMessageDelayed(message,1000) ;
		}else {
			if (islight) {
				turnOnFlash();
				islight = false;
			} else {
				turnOffFlash();
				islight = true;
			}
		}

		//mContext.sendBroadcast(new Intent(Constant.FINISH));

	}

	private void showDialog(){


		Log.e("liujw","##########################KeyFunctionUtils####SystemHintsDialog : ");

		Intent intent = new Intent(mContext,SystemHintsDialog.class) ;
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
		intent.putExtra("type",1);
		mContext.startActivity(intent);
	}


	private void turnOffFlash() {
		if (camera == null || params == null) {
			return;
		}

		params = camera.getParameters();
		params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		camera.setParameters(params);
		camera.stopPreview();
		releaseCamera();
	}


	public String getTopActivity(){
		ActivityManager activityManager =( ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfo = activityManager.getRunningTasks(1);
		ComponentName topActivity = runningTaskInfo.get(0).topActivity ;
		String activityName = (runningTaskInfo.get(0).topActivity).toString();
		return topActivity.getClassName() ;
	}

	private KeyFunctionUtil(Context context) {
		this.mContext = context;
		mDatabaseManager = DatabaseManager.getInstance(context);
		
		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
		
		mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		
		mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		mMaxAudio = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		getCamera() ;
	}
	
	private AudioManager mAudioManager ;

	public static KeyFunctionUtil getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new KeyFunctionUtil(context);
		}
		return mInstance;
	}
	
	private boolean islight = true ;
	
	private PowerManager pm;  
	
	private PowerManager.WakeLock wakeLock;  
		
	private KeyguardManager mKeyguardManager = null;    
	
	private KeyguardLock mKeyguardLock = null;    
	
	
	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			mKeyguardLock = mKeyguardManager.newKeyguardLock("");    
			mKeyguardLock.disableKeyguard();  
		}
	};
	
	private MediaPlayer mMediaPlayer = null; ;
	
	private void createMediaPlayer(int id, float volume) {
		
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
			mMediaPlayer.release();
			mMediaPlayer = null;
		}else{
			
			mMediaPlayer = MediaPlayer.create(mContext, id);
			
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mMaxAudio, 0); 
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			
			mMediaPlayer.setVolume(mMaxAudio,mMaxAudio);
			mMediaPlayer.start();
			
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
	
				@Override
				public void onCompletion(MediaPlayer mp) {
					
					mp.seekTo(0);
					mp.start();
				}
			});
		}
	}
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			int action = (int) msg.obj;
			Intent intent = null ;
			switch (action) {
				case 9:
					if (islight) {
						turnOnFlash();
						islight = false;
					} else {
						turnOffFlash();
						islight = true;
					}
					break ;
				case 0: // camera

					mContext.sendBroadcast(new Intent(Constant.FINISH));
					if(AppContext.isStart){
						intent = new Intent(mContext, BackgroundCameraActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(intent);
						AppContext.isStart = false ;
					}
					break;
				case 1: // light
					if(camera == null){
						getCamera();
						Message message = new Message() ;
						message.obj = 9 ;
						handler.sendMessageDelayed(message,1000) ;
					}else {
						if (islight) {
							turnOnFlash();
							islight = false;
						} else {
							turnOffFlash();
							islight = true;
						}
					}

					/*if (islight) {
						intent = new Intent(context, FlashActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);
						islight = false;
					}else{
						context.sendBroadcast(new Intent(Constant.FINISH));
						islight = true;
					}*/

					/*Camera mCamera = Camera.open();
					if (islight) {
						Parameters mParameters = mCamera.getParameters();
						mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
						mCamera.setParameters(mParameters);
						islight = false;
					} else {
						Parameters mParameters = mCamera.getParameters();
						mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
						mCamera.setParameters(mParameters);
						islight = true;
					}*/
					break;
				case 2: // start app
					/*if(wakeLock != null){
						wakeLock.release();
					}
					wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
					wakeLock.acquire();

					mKeyguardLock = mKeyguardManager.newKeyguardLock("");
					mKeyguardLock.disableKeyguard();
					*/


					mContext.sendBroadcast(new Intent(Constant.FINISH));
					intent = new Intent(mContext, OpenAppActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);

					//AppInfo bean = mDatabaseManager.selectAppInfo();
					//openApp(context,bean.getPackageName());
					break;
				case 3: // anti_call
					mContext.sendBroadcast(new Intent(Constant.FINISH));
					intent = new Intent(mContext, CallActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
					ContactBean contact = mDatabaseManager.selectAntiContact();
					/*if(contact != null){
						createMediaPlayer(R.raw.strum,20);
					}*/

					break;
				case 4: // sos
					mContext.sendBroadcast(new Intent(Constant.FINISH));
					if(AppContext.isStart){
						intent = new Intent(mContext,SosActivity.class);
						intent.putExtra("action", 4);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(intent);
						AppContext.isStart = false ;
					}
					break;
				case 5: // call
					mContext.sendBroadcast(new Intent(Constant.FINISH));
					ContactBean contactBean = mDatabaseManager.selectContact();
					startCallActivity(contactBean.getNumber());
					break ;
				case 6:
					mContext.sendBroadcast(new Intent(Constant.FINISH));
					intent = new Intent(mContext, AntilostCameraActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
					break;
				case 7:
					new AccelerationAction(mContext,testHandler).start();
					/*context.sendBroadcast(new Intent(Constant.FINISH));
					intent = new Intent(context,RecordActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("flag", 1);
					context.startActivity(intent);*/
					break ;
				case 8:
					mContext.sendBroadcast(new Intent(Constant.FINISH));
					intent = new Intent(mContext,RecordActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("flag", 1);
					mContext.startActivity(intent);

					break;
				default:
					break;
			}

		}
	};

	public void releaseCamera(){
		if (camera != null) {
			camera.release();
			camera = null;
			SharePerfenceUtil.setParam(mContext,"camera",0) ;
		}
	}

	public void actionKeyFunction(Context context, int action) {

		Message msg = new Message() ;
		msg.obj  = action ;
		handler.sendMessageDelayed(msg,20) ;

	}

	private Handler testHandler  = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			final long total = SysClearUtils.getMemoryTotalKb();
			final String msg1 = (String) msg.obj;

			Toast.makeText(mContext,msg1,1).show();
		}
	};
	
	public void releaseWake(){
		if(wakeLock != null){
			wakeLock.release();
		}
	}
	
	public void startCallActivity(String number){
		if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED) {
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}
	}

}
