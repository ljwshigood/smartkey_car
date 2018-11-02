package com.zzteck.msafe.util;

import android.app.NotificationManager;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.qihoo360.mobilesafe.opti.processclear.ProcessClear;
import com.qihoo360.mobilesafe.opti.processclear.SysClearUtils;
import com.zzteck.msafe.R;

public class AccelerationAction extends Thread{


	private static final long VIBATE_TIME = 200;
	private static final int DELAY_TIMER = 30000;
	private static Stated sState = Stated.SencondAfter30;

	private static enum Stated {
		SencondAfter30, FirstUnFinish, SecondBefore30
	};

	private NotificationManager notificationManager;

	private Context context;

	private Handler mHandler ;

	public AccelerationAction(Context context,Handler handler) {
		this.context = context ;
		this.mHandler = handler ;
	}

	@Override
	public void run() {
		final Context ctx = context ;

	/*	if (sState == Stated.SencondAfter30) {
		} else if (sState == Stated.FirstUnFinish) {
			return;
		} else if (sState == Stated.SecondBefore30) {
			return;
		}

		if (!changeState(Stated.FirstUnFinish)) {
			return;
		}*/
		ProcessClear processClear = new ProcessClear(ctx);
		// 执行清理操作，返回清理结果 [0]:清理的个数 [1]:释放的内存数{单位是kb}
		//final long[] clearResult = processClear.clearProcess();
        String str = Utils.oneSpeed(context);
        Message msg = new Message();
        msg.what = 0 ;
        msg.obj = str ;

		mHandler.removeMessages(0);
		mHandler.sendMessage(msg) ;

	/*	new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				final long total = SysClearUtils.getMemoryTotalKb();
				final String msg = ctx
						.getString(R.string.action_acceleration_msg);

				if (100 * clearResult[1] / total == 0) {

					Toast.makeText(context,String.format(msg,
							(int) (Math.random() * 10)),1).show();

				} else {
					Toast.makeText(context,String.format(msg, 100
							* clearResult[1] / total),1).show() ;
				}

			}*/


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
		sState = newState ;
		return true;
	}

}
