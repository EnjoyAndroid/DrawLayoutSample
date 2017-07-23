package com.lee.drawlayoutsample;

import android.app.Application;

import com.lee.drawlayoutsample.utils.BaseUtils;

/**
 * Created by Lee on 2017/7/24.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BaseUtils.initialize(this);
    }
}
