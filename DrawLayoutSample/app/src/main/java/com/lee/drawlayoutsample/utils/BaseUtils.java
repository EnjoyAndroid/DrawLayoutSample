package com.lee.drawlayoutsample.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

public class BaseUtils {

    private static Context context;

    public static void initialize(@NonNull Context context) {
        BaseUtils.context = context;
    }

    public static Context getContext() {
        synchronized (BaseUtils.class) {
            if (BaseUtils.context == null)
                throw new NullPointerException("Call BaseUtils.initialize(context) within your Application onCreate() method.");

            return BaseUtils.context.getApplicationContext();
        }
    }

    public static Resources getResources() {
        return BaseUtils.getContext().getResources();
    }

    public static Resources.Theme getTheme() {
        return BaseUtils.getContext().getTheme();
    }

    public static AssetManager getAssets() {
        return BaseUtils.getContext().getAssets();
    }

    public static Configuration getConfiguration() {
        return BaseUtils.getResources().getConfiguration();
    }

    public static DisplayMetrics getDisplayMetrics() {
        return BaseUtils.getResources().getDisplayMetrics();
    }
}
