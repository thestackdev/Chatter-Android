package com.firebase.chatter.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.firebase.chatter.Chatter;
import com.firebase.chatter.R;
import com.firebase.chatter.activities.MessageActivity;
import com.firebase.chatter.helper.GetTimeAgo;
import com.firebase.chatter.models.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.Random;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ChatsService extends Service {

    private Handler handler;
    private Runnable runnable;
    private DatabaseReference myChatData;
    private DatabaseReference messageData;
    private String currentUID;
    private Chat chat;
    private NotificationManager notificationManager;


    @Override
    public void onCreate() {
        super.onCreate();

        messageData = FirebaseDatabase.getInstance().getReference().child("messages");

        currentUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        myChatData = FirebaseDatabase.getInstance().getReference().child("Chat").child(currentUID);

        myChatData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot children : dataSnapshot.getChildren()) {

                    chat = children.getValue(Chat.class);
                    Log.i(TAG, "onDataChange: "+children);
                    assert chat != null;
                    int unSeen = chat.getUnSeen();
                    if(unSeen != 0) {
                        Log.i(TAG, "onDataChange: new message"+unSeen);
                        showMessages(chat , dataSnapshot.getKey() , chat.getMessageNode());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          //  showBgNotification();
        }

    }

    private void showMessages(Chat chat , String uID , String messageNode) {
        messageData.child(messageNode).limitToLast(chat.getUnSeen()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot children : dataSnapshot.getChildren()) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

 /*   @RequiresApi(api = Build.VERSION_CODES.O)
    private void showBgNotification() {

        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);

    } */

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("RestartService");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("HEY","HEY SERVICE");
        return null;
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
