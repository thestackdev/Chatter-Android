package com.firebase.chatter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.firebase.chatter.activities.MessageActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private NotificationManager notificationManager;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);

    private DatabaseReference currentMessageData;
    private DatabaseReference chatMessageData;
    private DatabaseReference rootData;
    private DatabaseReference usersData;

    private DatabaseReference currentChatRef;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {

            final String fromID = remoteMessage.getData().get("fromID");
            final String pushID = remoteMessage.getData().get("pushID");
            final String toID = remoteMessage.getData().get("toID");
            final String message = remoteMessage.getData().get("message");
            String times = remoteMessage.getData().get("times");


            assert times != null;
            assert toID != null;
            assert fromID != null;
            assert pushID != null;

            String[] split = times.split("," , 3);

            split[1] = dateFormat.format(new Date());

            rootData = FirebaseDatabase.getInstance().getReference();

            currentMessageData = rootData.child("messages").child(toID).child(fromID).child(pushID);
            chatMessageData = rootData.child("messages").child(fromID).child(toID).child(pushID);
            usersData = rootData.child("Users");

            currentMessageData.child("state").setValue("2");
            currentMessageData.child("times").setValue(split[0]+","+split[1]+","+split[2]);

            chatMessageData.child("state").setValue("2");


            currentChatRef = rootData.child("Chat").child(toID).child(fromID);

                        usersData.child(fromID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String myName = dataSnapshot.child("name").getValue().toString();
                                String image = dataSnapshot.child("image").getValue().toString();
                                String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                                notifyUserWithNotification(myName , fromID , image , thumbnail , message);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

    private void notifyUserWithNotification(String myName , String from , String image , String thumbnail , String message) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setupChannels();
            }
            int notificationId = new Random().nextInt(60000);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "Chatter")
                    .setSmallIcon(R.drawable.emoji_1f3c3)
                    .setContentTitle(myName)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
            intent.putExtra("profile_user_id", from);
            intent.putExtra("userName", myName);
            intent.putExtra("thumbnail", thumbnail);
            intent.putExtra("image", image);

            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(contentIntent);

            assert notificationManager != null;
            notificationManager.notify(notificationId, notificationBuilder.build());

        }


    @RequiresApi(api = Build.VERSION_CODES.O)
        private void setupChannels() {
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

