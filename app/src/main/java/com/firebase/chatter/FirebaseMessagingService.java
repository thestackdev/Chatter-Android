package com.firebase.chatter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.firebase.chatter.activities.MessageActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private NotificationManager notificationManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {

            final String messageID = remoteMessage.getData().get("messageID");
            final String pushID = remoteMessage.getData().get("pushID");
            final String userName = remoteMessage.getData().get("userName");
            final String message = remoteMessage.getData().get("message");
            final String fromId = remoteMessage.getData().get("fromID");
            final String userImage = remoteMessage.getData().get("userImage");
            final String userThumb = remoteMessage.getData().get("userThumb");
            final String times = remoteMessage.getData().get("times");

            assert times != null;
            String[] split = times.split("," , 3);

            split[1] = dateFormat.format(new Date());

            assert messageID != null;
            assert pushID != null;

            DatabaseReference deliveredStatus = FirebaseDatabase.getInstance().getReference().child("messages")
                    .child(messageID).child("Conversation").child(pushID);

                    deliveredStatus.child("state").setValue("2").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            deliveredStatus.child("times").setValue(split[0]+","+split[1]+",null")
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        }
                    });


            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //Setting up Notification channels for android O and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setupChannels();
            }
            int notificationId = new Random().nextInt(60000);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "Chatter")
                    .setSmallIcon(R.drawable.emoji_1f3c3)
                    .setContentTitle(userName)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            Intent intent = new Intent(getApplicationContext(), MessageActivity.class);

            intent.putExtra("profile_user_id", fromId);
            intent.putExtra("userName", userName);
            intent.putExtra("thumbnail", userThumb);
            intent.putExtra("image", userImage);
            intent.putExtra("messageNode", messageID);

            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(contentIntent);

            assert notificationManager != null;
            notificationManager.notify(notificationId, notificationBuilder.build());


        }


    }


        @RequiresApi(api = Build.VERSION_CODES.O)
        private void setupChannels(){
            CharSequence adminChannelName = "Messages";
            String adminChannelDescription = "New Message";

            NotificationChannel adminChannel;
            adminChannel = new NotificationChannel("Chatter", adminChannelName, NotificationManager.IMPORTANCE_LOW);
            adminChannel.setDescription(adminChannelDescription);
            adminChannel.enableLights(true);
            adminChannel.setLightColor(Color.RED);
            adminChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(adminChannel);

            }
        }
    }

