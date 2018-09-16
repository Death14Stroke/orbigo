package com.orbigo.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import java.util.Objects;

public class GPSReceiver extends BroadcastReceiver {

    public static GpsStatusListener gpsStatusListener;

    public GPSReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.requireNonNull(intent.getAction()).matches("android.location.PROVIDERS_CHANGED")) {
            final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
            assert manager != null;
            boolean enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(gpsStatusListener!=null){
                gpsStatusListener.onGpsStatusChanged(enabled);
                Log.v("broadcast","status:"+enabled);
            }
        }
    }

    public interface GpsStatusListener{
        void onGpsStatusChanged(boolean enabled);
    }
}