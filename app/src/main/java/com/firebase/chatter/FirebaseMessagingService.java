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
import com.firebase.chatter.models.Chat;
import com.firebase.chatter.models.Messages;
import com.firebase.chatter.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private NotificationManager notificationManager;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);

    private DatabaseReference messageData;
    private DatabaseReference rootData;
    private DatabaseReference usersData;
    private DatabaseReference chatData;

    private String uID;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {

            rootData = FirebaseDatabase.getInstance().getReference();

            uID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            chatData = rootData.child("Chat").child(uID);

            messageData = rootData.child("messages");

            usersData = rootData.child("Users");

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.cancelAll();

            final String pushID = remoteMessage.getData().get("pushID");
            String times = remoteMessage.getData().get("times");
            String messageNode = remoteMessage.getData().get("messageID");

            String[] split = times.split("," , 3);

            assert messageNode != null;
            assert pushID != null;
            messageData.child(messageNode).child(pushID).child("state").setValue(2);
            messageData.child(messageNode).child(pushID).child("times").setValue(split[0]+","+dateFormat.format(new Date())+",null");

            chatData.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot children : dataSnapshot.getChildren()) {
                        Chat chat = children.getValue(Chat.class);

                        String childKey = children.getKey();

                        assert chat != null;
                        if (chat.getUnSeen() != 0) {

                            usersData.child(childKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Users users = dataSnapshot.getValue(Users.class);
                                    sendNotificationToUser(chat.getUnSeen(), chat.getMessageNode() , users);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void sendNotificationToUser(int unSeen, String messageNode, Users users) {

        messageData.child(messageNode).limitToLast(unSeen).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot children : dataSnapshot.getChildren()) {

                    Messages messages = children.getValue(Messages.class);
                    notifyUserWithNotification(users.getName() , messages.getFrom() , users.getImage() , users.getThumbnail() , messages.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void notifyUserWithNotification(String myName, String from, String image, String thumbnail, String message) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
        int notificationID = new Random().nextInt(6000);

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
        notificationManager.notify(notificationID, notificationBuilder.build());

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

