package com.firebase.chatter;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MessageActivity extends AppCompatActivity {

    private ImageView back_btn , btnSend;
    private CircleImageView user_image;
    private TextView user_name , lastSeen;
    private EmojiconEditText messageInput;
    private RecyclerView messageRecyclerView;
    private int chatUserIndex , presentIndex = 20;
    private DatabaseReference rootDatabase , chatUserData , messageData , seenRef , chatRef;
    private String currentUid , chatUserId , currentTime , push_id;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseRecyclerAdapter<Messages , MessageActivity.MessageViewHolder> messageAdapter;
    private FirebaseRecyclerOptions<Messages> messagesFirebaseRecyclerOptions;
    private ImageView image_pick, camera, emoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setUpUiViews();
        setUpReferences();
        setUpChatPage();
        setUpChatUser();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presentIndex = presentIndex + 20;

                setFirebaseAdapter(presentIndex);

                swipeRefreshLayout.setRefreshing(false);

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        final RelativeLayout rootView = findViewById(R.id.root);

        emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EmojIconActions  emojIcon=new EmojIconActions(MessageActivity.this,rootView,messageInput,emoji);
                emojIcon.ShowEmojIcon();
                emojIcon.setUseSystemEmoji(true);
                emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
                    @Override
                    public void onKeyboardOpen() {

                    }

                    @Override
                    public void onKeyboardClose() {

                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setFirebaseAdapter(presentIndex);

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                camera.setVisibility(View.GONE);
                camera.animate().translationY(0);
                if (count==0){
                    camera.setVisibility(View.VISIBLE);
                    camera.animate().translationY(0);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {



            }
        });

    }

    private void setUpChatPage() {

        chatRef.child(currentUid).child(chatUserId).child("seen").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    private void setFirebaseAdapter(final int presentIndex) {

        // Firebase Content

        messagesFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(messageData.limitToLast(presentIndex) , Messages.class).build();

        messageAdapter = new FirebaseRecyclerAdapter<Messages, MessageViewHolder>(messagesFirebaseRecyclerOptions) {
            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
                return new MessageViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i, @NonNull Messages messages) {
                String messagePosition = getRef(i).getKey();

                if (messages.getFrom().equals(currentUid)) {
                    messageViewHolder.messageLayout.setGravity(Gravity.END);
                    messageViewHolder.message.setText(messages.getMessage());
                    messageViewHolder.layout_bg.setBackgroundResource(R.drawable.background_right);
                    messageViewHolder.stamp.setVisibility(View.VISIBLE);
                    messageViewHolder.message_time.setText(messages.getTime());

                    String state = "0";
                    state = messages.getState();

                    if (isConnected()) {
                        if (state.equals("1") || state.equals("0")) {

                            messageViewHolder.stamp.setBackgroundResource(R.drawable.blacktick);

                        } else if(state.equals("2")){

                            messageViewHolder.stamp.setBackgroundResource(R.drawable.greentick);

                        }

                    } else {
                        switch (state) {
                            case "1":
                                messageViewHolder.stamp.setBackgroundResource(R.drawable.blacktick);
                                break;
                            case "2":
                                messageViewHolder.stamp.setBackgroundResource(R.drawable.greentick);
                                break;
                            default:
                                messageViewHolder.stamp.setBackgroundResource(R.drawable.timer);
                        }
                    }

                } else if (messages.getFrom().equals(chatUserId)) {
                    messageViewHolder.layout_bg.setBackgroundResource(R.drawable.background_left);
                    messageViewHolder.stamp.setVisibility(View.GONE);
                    messageViewHolder.messageLayout.setGravity(Gravity.START);
                    messageViewHolder.message.setText(messages.getMessage());
                    messageViewHolder.message_time.setText(messages.getTime());

                }



            }


            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            public void onChildChanged(@NonNull ChangeEventType type, @NonNull DataSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
                messageRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                Log.d("TEST",snapshot.getKey());
            }
        };

        messageRecyclerView.setAdapter(messageAdapter);
        messageAdapter.notifyDataSetChanged();
        messageAdapter.startListening();

        // Firebase Content Completed

    }

    private void setUpChatUser() {

        chatUserData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                final String thumbnail = Objects.requireNonNull(dataSnapshot.child("thumbnail").getValue()).toString();

                if (!thumbnail.equals("default")) {
                    Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(user_image, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(thumbnail).placeholder(R.drawable.avatar).into(user_image);

                        }
                    });
                }

                user_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!thumbnail.equals("default")) {
                            final String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                            Uri imageUri = Uri.parse(image);
                            Intent intent = new Intent(v.getContext() , FullScreenImageView.class);
                            intent.setData(imageUri);
                            startActivity(intent);
                        } else {
                            Toast.makeText(v.getContext(), "No Profile Picture", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                user_name.setText(Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());

                String seenTime = Objects.requireNonNull(dataSnapshot.child("online").getValue()).toString();

                if (seenTime.equals("true")) {
                    lastSeen.setText("online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long getTime = Long.parseLong(seenTime);
                    lastSeen.setText(getTimeAgo.getTimeAgo(getTime, getApplicationContext()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setUpReferences() {

        rootDatabase = FirebaseDatabase.getInstance().getReference();

        currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        chatUserId = getIntent().getStringExtra("profile_user_id");
        chatUserData = rootDatabase.child("Users").child(chatUserId);

        messageData = rootDatabase.child("messages").child(currentUid).child(chatUserId);
        messageData.keepSynced(true);

        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat");

        seenRef = rootDatabase.child("messages").child(chatUserId).child(currentUid);

    }

    private void setUpUiViews() {

        user_image = findViewById(R.id.user_image);
        back_btn = findViewById(R.id.back_btn);
        user_image = findViewById(R.id.user_image);
        user_name = findViewById(R.id.user_name);
        lastSeen = findViewById(R.id.lastSeen);
        swipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        image_pick = findViewById(R.id.image_pick);
        camera = findViewById(R.id.camera);
        emoji = findViewById(R.id.emoji);

        btnSend = findViewById(R.id.send_message);
        messageInput = findViewById(R.id.message_input);

        messageRecyclerView = findViewById(R.id.message_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);

        messageRecyclerView.setLayoutManager(layoutManager);

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        currentTime = dateFormat.format(date);

    }

    private void sendMessage() {

        String message = messageInput.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String currentUserRef = "messages/" + currentUid + "/" + chatUserId;
            String chatUserRef = "messages/" + chatUserId + "/" + currentUid;

            DatabaseReference user_message_push = rootDatabase.child("messages").child(currentUserRef).child(chatUserId).push();
            push_id = user_message_push.getKey();

            final Map<String, String> messageMap = new HashMap<>();
            messageMap.put("message" , message);
            messageMap.put("type", "text");
            messageMap.put("time", currentTime);
            messageMap.put("from", currentUid);
            messageMap.put("state" , "0");

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(currentUserRef + "/" + push_id, messageMap);
            messageUserMap.put(chatUserRef + "/" + push_id, messageMap);

            messageInput.setText(null);

            chatRef.child(chatUserId).child(currentUid).child("index").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    chatUserIndex = Integer.parseInt(Objects.requireNonNull(dataSnapshot.getValue()).toString());

                    int pageIndex = messageAdapter.getItemCount() + 1;

                    Log.i("TAG", "onDataChange: "+pageIndex +" "+chatUserIndex);

                    chatRef.child(currentUid).child(chatUserId).child("index").setValue(pageIndex);

                    chatRef.child(chatUserId).child(currentUid).child("toBeSeen").setValue(pageIndex - chatUserIndex);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            chatRef.child(currentUid).child(chatUserId).child("timeStamp").setValue(ServerValue.TIMESTAMP);

            chatRef.child(chatUserId).child(currentUid).child("seen").setValue(false);
            chatRef.child(chatUserId).child(currentUid).child("timeStamp").setValue(ServerValue.TIMESTAMP);

            rootDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        messageData.child(push_id).child("state").setValue("1").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });
                    }
                }
            });
        }
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null) {
            return true;
        } else return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        chatRef.child(currentUid).child(chatUserId).child("toBeSeen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int index = Integer.parseInt(Objects.requireNonNull(dataSnapshot.getValue()).toString());

                if (index != 0){
                    seenRef.orderByKey().limitToLast(index).addChildEventListener(new ChildEventListener() {

                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            String uid = dataSnapshot.getKey();
                            assert uid != null;
                            seenRef.child(uid).child("state").setValue("2").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                chatRef.child(currentUid).child(chatUserId).child("toBeSeen").setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView message, message_time;
        private ImageView stamp;
        private LinearLayout messageLayout, layout_bg;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            stamp = itemView.findViewById(R.id.stamp);
            message = itemView.findViewById(R.id.user_message);
            messageLayout = itemView.findViewById(R.id.message_single_layout);
            layout_bg = itemView.findViewById(R.id.layout_bg);
            message_time = itemView.findViewById(R.id.message_time);
        }
    }
}

