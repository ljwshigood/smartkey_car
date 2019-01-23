package com.zzteck.msafe.util;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

import com.qihoo360.mobilesafe.opti.processclear.ProcessClear;
import com.qihoo360.mobilesafe.opti.processclear.SysClearUtils;
import com.zzteck.msafe.R;

public class AccelerationAction_bak extends Thread {


	private static final long VIBATE_TIME = 200;
	private static final int DELAY_TIMER = 30000;
	private static Stated sState = Stated.SencondAfter30;

	private static enum Stated {
		SencondAfter30, FirstUnFinish, SecondBefore30
	};

//	private static Toast sToast;
	private NotificationManager notificationManager;
	private Context context;

	private Handler mHandler ;

	public AccelerationAction_bak(Context context,Handler handler) {
		this.context = context ;
		this.mHandler = handler ;
	}

	@Override
	public void run() {
		final Context ctx = context ;

		Log.e("liujw","##########################AccelerationAction_bak run");

		Message msg = new Message();
		msg.what = 0 ;
		msg.obj = context.getString(R.string.action_acceleration_ing) ;

		mHandler.removeMessages(0);
		mHandler.sendMessage(msg) ;

		// 该类相关操作会耗时，请在线程中使用，避免出现ANR
		ProcessClear processClear = new ProcessClear(ctx);
		// 执行清理操作，返回清理结果 [0]:清理的个数 [1]:释放的内存数{单位是kb}
		final long[] clearResult = processClear.clearProcess();

		new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				final long total = SysClearUtils.getMemoryTotalKb();
				final String msg = ctx.getString(R.string.action_acceleration_msg);

				if (100 * clearResult[1] / total == 0) {

					String formatMessage = String.format(msg,(int) (Math.random() * 10)) ;
					Message msg1 = new Message();
					msg1.what = 0 ;
					msg1.obj = formatMessage ;

					mHandler.removeMessages(0);
					mHandler.sendMessage(msg1) ;

				} else {
					String formatMessage = String.format(msg, 100 * clearResult[1] / total) ;

					Message msg1 = new Message();
					msg1.what = 0 ;
					msg1.obj = formatMessage ;

					mHandler.removeMessages(0);
					mHandler.sendMessage(msg1) ;

				}
			}

		});

	}

	private static boolean changeState(Stated newState) {
		switch (newState) {
		case SencondAfter30:
			if (sState != Stated.SecondBefore30)
				return false;
			break;
		case FirstUnFinish:
			if (sState != Stated.SencondAfter30)
				return false;
			break;
		case SecondBefore30:
			if (sState != Stated.FirstUnFinish)
				return false;
			break;
		}
		sState = newState;
		return true;
	}

	private static void verbiate(Context context) {
		//if (SmartKey.isVibratorEnabled()) {
			Vibrator vibrator = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(VIBATE_TIME);
		//}
	}

	private void verbiateComplete(Context context) {
		//if (SmartKey.isVibratorEnabled()) {
			Vibrator vibrator = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = { 0, 50, 200, 50, 200 };
			vibrator.vibrate(pattern, -1);
		//}
	}

	/*private void showNotification(String msg) {
		if (this.notificationManager == null) {
			this.notificationManager = (NotificationManager) SystemServices
					.getNotificationManager(context);
		}
		this.notificationManager.cancel(0);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(SmartKey.getSmartKeyLauncher());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
		Notification no = new Notification();
		no.contentIntent = pi;
		// no.deleteIntent = delPi;
		no.icon = R.drawable.action_acceleration_icon;
		no.tickerText = msg;
		no.contentView = new RemoteViews(context.getPackageName(),
				R.layout.action_acceleration_notification_layout);
		no.when = System.currentTimeMillis();
		no.contentView.setTextViewText(
				R.id.action_acceleration_notification_tv, msg);
		no.flags = Notification.FLAG_AUTO_CANCEL;
		this.notificationManager.notify(0, no);
	}*/
}
