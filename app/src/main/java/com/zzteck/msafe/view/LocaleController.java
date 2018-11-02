package com.zzteck.msafe.view;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;

import java.util.Locale;

public class LocaleController {

    private Locale systemDefaultLocale;
    private String localLanguage;
    private String localCountry;

    public static boolean isRTL = false;

    private static volatile LocaleController Instance = null;

    public static LocaleController getInstance(Context context) {
        LocaleController localInstance = Instance;
        if (localInstance == null) {
            synchronized (LocaleController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new LocaleController(context);
                }
            }
        }
        return localInstance;
    }

    public static boolean isTextRTL() {
        return false;
//        return isRTL || getInstance().localLanguage.equals("fa") || getInstance().localLanguage.equals("ar");
    }

    private LocaleController(Context context) {
        init(context);
    }

    public void onDeviceConfigurationChange(Context context,Configuration newConfig) {
        init(context);
    }

    private void init(Context context) {
        systemDefaultLocale = Locale.getDefault();

        Resources resources = context.getResources();// 获得res资源对象
        Configuration config = resources.getConfiguration();// 获得设置对象
        if (!TextUtils.isEmpty(localCountry)) {
            config.locale = new Locale(localLanguage, localCountry);
        } else {
            config.locale = new Locale(localLanguage);
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public String getLocalCountry(){
        return localCountry;
    }

    public String getCountry() {
        return systemDefaultLocale.getCountry();
    }

    private String[] getArray(int res) {
        String[] value = null;
        try {
        } catch (Exception e) {
            String TAG = "LocaleController";
        }
        if (value == null) {
            value = new String[0];
        }
        return value;
    }


    private String getStringInternal(int res, Object... args) {
        String value = null;
        try {

        } catch (Exception e) {
            String TAG = "LocaleController";
        }
        if (value == null) {
            value = "LOC_ERR:" + res;
        }
        try {
            return String.format(value, args);
        } catch (Exception e) {
            String TAG = "LocaleController";
        }
        return value;
    }


    public Locale getSystemDefaultLocale() {
        return systemDefaultLocale;
    }

}
