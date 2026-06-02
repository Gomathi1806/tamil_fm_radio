package com.ibitway.fmradio.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.ibitway.fmradio.utilities.Log;
import com.ibitway.fmradio.utilities.Tools;

public class RadioManager {

    private static RadioManager instance = null;
    private static RadioService service;
    private boolean serviceBound;

    private RadioManager() {
        serviceBound = false;
    }

    public static RadioManager with() {
        if (instance == null)
            instance = new RadioManager();
        return instance;
    }

    public static RadioService getService() {
        return service;
    }

    public RadioService getRadioService(){
        return service;
    }

    public void playOrPause(String streamUrl) {
        if (streamUrl == null && service != null)
            service.stop();
        else if (service != null)
            service.playOrPause(streamUrl);
    }

    public void playResume(String streamUrl) {
        if (streamUrl == null && service != null)
            service.pause();
        else if (service != null)
            service.playOrPause(streamUrl);
    }

    public void stopServices() {
        if (service != null) {
            service.stop();
        }
    }

    public boolean isPlaying() {

        if (service == null){
            return false;
        }

        return service.isPlaying();
    }

    public void bind(Context context) {
        //Perhaps also catch a LeakedServiceConnection, and if caught: then call unbind and then try to bind again
        if (!serviceBound) {
            Intent intent = new Intent(context, RadioService.class);
            context.startService(intent);
            boolean bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            if (service != null)
                Tools.onEvent(service.getStatus());
        }
    }

    public void unbind(Context context) {
        if (serviceBound) {
            try {
                service.stop();
                context.unbindService(serviceConnection);
                context.stopService(new Intent(context, RadioService.class));
                serviceBound = false;
            } catch (IllegalArgumentException e) {
                Log.printStackTrace(e);
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            service = ((RadioService.LocalBinder) binder).getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

}
