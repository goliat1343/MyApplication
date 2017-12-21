package com.example.marekwieciech.myapplication;

import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Marek.Wieciech on 28.11.2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());


        try{
            Map<String, String> data = remoteMessage.getData();

            //you can get your text message here.
            String text = "Z data: " +  data.get("text");


            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    // optional, this is to make beautiful icon
                    .setLargeIcon(BitmapFactory.decodeResource(
                            getResources(), R.mipmap.ic_launcher))
                    .setSmallIcon(R.mipmap.ic_launcher_round);  //mandatory


            mBuilder.setContentTitle("By Goliat - data");

            if (text != null) {
                mBuilder.setContentText(text);
            } else {
                mBuilder.setContentText("Brak data.");
            }




            // Sets an ID for the notification
            int mNotificationId = 001;

            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        } catch (Exception e){
            Log.d("blad firebase", "blad kurcze");
        }
    }
}
