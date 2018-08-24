package com.ds14.darren.orbigo.helpers;

import android.app.Application;

import com.ds14.darren.orbigo.broadcast_receivers.ConnectivityReceiver;
import com.ds14.darren.orbigo.broadcast_receivers.GPSReceiver;

public class MyApplication extends Application {
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public void setGpsStatusListener(GPSReceiver.GpsStatusListener listener) {
        GPSReceiver.gpsStatusListener = listener;
    }
}
