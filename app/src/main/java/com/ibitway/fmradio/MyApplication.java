package com.ibitway.fmradio;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
public class MyApplication extends Application {

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        //this.firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}