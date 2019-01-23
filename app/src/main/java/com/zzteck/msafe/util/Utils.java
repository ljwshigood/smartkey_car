package com.zzteck.msafe.util;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.zzteck.msafe.service.ClearService;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Jianwei on 2018/9/16.
 */

public class Utils {




    public static String oneSpeed(Context context) {


        //To change body of implemented methods use File | Settings | File Templates.
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(100);

        long beforeMem = getAvailMemory(context);
        int count = 0;
        if (infoList != null) {
            for (int i = 0; i < infoList.size(); ++i) {
                ActivityManager.RunningAppProcessInfo appProcessInfo = infoList.get(i);
                //importance 该进程的重要程度  分为几个级别，数值越低就越重要。

                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_SERVICE的进程都长时间没用或者空进程了
                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_VISIBLE的进程都是非可见进程，也就是在后台运行着
                if (appProcessInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    String[] pkgList = appProcessInfo.pkgList;
                    for (int j = 0; j < pkgList.length; j++) {//pkgList 得到该进程下运行的包名

                        Log.e("liujw", "######################pkgList : " + pkgList[j]);
                        if (!pkgList[j].equals("com.zzteck.msafe")) {
                            am.killBackgroundProcesses(pkgList[j]);
                            count++;
                        }

                    }
                }

            }
        }

        long afterMem = getAvailMemory(context);

        return "clear " + count + " process, " + (afterMem - beforeMem) + "M";
    }

    //获取可用内存大小
    private static long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存
        //return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
        return mi.availMem / (1024 * 1024);
    }
}
