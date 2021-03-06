package com.zzteck.msafe.util;


import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.zzteck.msafe.R;
import com.zzteck.msafe.activity.MainFollowActivity;
import com.zzteck.msafe.bean.DeviceSetInfo;
import com.zzteck.msafe.bean.DisturbInfo;
import com.zzteck.msafe.service.BgMusicControlService;

public class AlarmManager {

	private Context mContext;

	private static AlarmManager mInstance;

	private AlarmManager(Context context) {
		this.mContext = context;
	}

	public static AlarmManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new AlarmManager(context);
		}
		return mInstance;
	}

	public boolean isDeviceMoreDistance(int rssi, String address,
			DeviceSetInfo info, DisturbInfo disturnInfo) {
		float d = GattAttributes.getRSSIMeter(rssi);
		Log.e("liujw", "######d : " + d);
		Log.e("liujw", "######rssi : " + rssi);
		boolean isMoreDistance = false;
		Log.e("liujw", "######Distance : " + info.getDistanceType());
		Log.e("liujw", "######isDisturb : " + disturnInfo.isDisturb());
		isMoreDistance = isInDurationDistance(info.getDistanceType(), d);
		return isMoreDistance;
	}

	private boolean isInDurationDistance(int type,float rssiDistance) {
		switch (type) {
		case 0:
			if(rssiDistance >= 5){
				return true ;
			}
			break;
		case 1:
			if(rssiDistance > 5 && rssiDistance <= 8){
				return true ;
			}
			break;
		case 2:
			if(rssiDistance > 8){
				return true ;
			}
			break;
		default:
			break;
		}
		return false ;
	}

	public boolean isMoreDistanceAlarm(String address, DeviceSetInfo info,
			DisturbInfo disturnInfo) {
		/*if (disturnInfo.isDisturb()
				&& !FomatTimeUtil.isInDisturb(disturnInfo.getStartTime(),
						disturnInfo.getEndTime()))*/
			/*Intent intentDistance = new Intent(BgMusicControlService.CTL_ACTION);
			intentDistance.putExtra("control", 1);
			intentDistance.putExtra("address", address);
			mContext.sendBroadcast(intentDistance);
			if (info != null) {
				return true;
			}

		} else {*/// 没有开启勿扰模式
			Intent intentDistance = new Intent(BgMusicControlService.CTL_ACTION);
			intentDistance.putExtra("address", address);
			intentDistance.putExtra("control", 1);
			mContext.sendBroadcast(intentDistance);
			if (info != null) {
				return true;
			}
		/*	}*/
		return false;
	}

	public boolean DeviceDisconnectAlarm(DeviceSetInfo info, String address,
			String alarmInfo) {
		if (info != null) {
			Intent intentDistance = new Intent(BgMusicControlService.CTL_ACTION);
			intentDistance.putExtra("control", 1);
			intentDistance.putExtra("address", address);
			mContext.sendBroadcast(intentDistance);
			return true;
		}
		return false;
	}

	public Bitmap getConnectDeviceBitmap(DeviceSetInfo info, Context context) {
		if (info.getFilePath().equals("null")) {
			Bitmap bitmap = getRes("ic_launcher_icon", context);
			return ImageTools.toRoundBitmap(bitmap, 180);
		} else {
			Bitmap bitmap = ImageTools.getBitmapFromFile(info.getFilePath(), 8);
			if (bitmap == null) {
				bitmap = getRes("ic_launcher_icon", context);
			}
			return ImageTools.toRoundBitmap(bitmap, 180);
		}
	}

	public Bitmap getDeviceBitmap(DeviceSetInfo info, Context context) {
		if (info.getFilePath().equals("null")) {
			Bitmap bitmap = getRes("ic_launcher_icon", context);
			return ImageTools.toRoundBitmap(bitmap, 180);
		} else {
			Bitmap bitmap = ImageTools.getBitmapFromFile(info.getFilePath(), 8);
			if (bitmap == null) {
				bitmap = getRes("ic_launcher_icon", context);
			}
			return ImageTools.toRoundBitmap(bitmap, 180);
		}
	}

	public Bitmap getRes(String name, Context context) {
		ApplicationInfo appInfo = context.getApplicationInfo();
		int resID = context.getResources().getIdentifier(name, "drawable",
				appInfo.packageName);
		return BitmapFactory.decodeResource(context.getResources(), resID);
	}

	/**
	 * 
	 * @description: 判断当前应用程序处于前台还是后台
	 */
	public boolean isApplicationBroughtToBackground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	private static final int NOTICE_ID = 1222;

	/**
	 * @description : 报警的notifycation
	 * @param context
	 * @param address
	 * @param string
	 */
	public void notifycationAlarm(Context context, String address, String string) {
		Intent intent = new Intent(context, MainFollowActivity.class);
		final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher,"AntiLost Alarming", System.currentTimeMillis());
		PendingIntent pendIntent = PendingIntent.getActivity(context, 0,intent, 0);
		/*notification.setLatestEventInfo(context, "Follow", string, pendIntent);*/

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setContentTitle("Follow")
				.setContentText(string)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
				.setSmallIcon(R.mipmap.ic_launcher)
				.setWhen(System.currentTimeMillis())
				.setTicker(string)
				.setContentIntent(pendIntent)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_SOUND);

		manager.notify(NOTICE_ID, notification);
	}

}
