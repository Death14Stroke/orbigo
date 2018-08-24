package com.ds14.darren.orbigo.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.activities.TimelineActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    String TAG = "geofencelogs";
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        Log.v(TAG,"service started");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = geofencingEvent.getErrorCode()+"";
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        String geoId = triggeringGeofences.get(0).getRequestId();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            sendNotification("You have reached ",geoId,geofenceTransition);
        }
        else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            sendNotification("You have left ",geoId,geofenceTransition);
        } else {// Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type));
        }
    }

    private void sendNotification(String text, String name, int geofenceTransition) {
        Intent intent = new Intent(this, TimelineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_ENTER)
            intent.putExtra("enter_poi",name);
        else if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_EXIT)
            intent.putExtra("exit_poi",name);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Trip status")
                .setContentText(text+name)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}