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

    private String lastMsg;

    private Thread thread;

    private boolean check = true;

    private ImageView back_btn , btnSend;
    private CircleImageView user_image;
    private TextView user_name , lastSeen;
    private EmojiconEditText messageInput;
    private RecyclerView messageRecyclerView;
    private int presentIndex = 20;
    private DatabaseReference rootDatabase , usersData , myMessageData , userMessageData , chatRef , currentChatRef , userChatRef;
    private String currentUid , chatUserId , currentTime , push_id;
    private SwipeRefreshLayout swipeRefreshLayout;
    FirebaseRecyclerAdapter<Messages, MessageViewHolder> messageAdapter;
    private FirebaseRecyclerOptions<Messages> messagesFirebaseRecyclerOptions;
    private ImageView camera;
    private ImageView icons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setUpUiViews();
        setUpReferences();
        setUpChatUser();
        setUpChatPage();


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                check = false;
                thread.interrupt();
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

        icons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EmojIconActions  emojIcon=new EmojIconActions(MessageActivity.this,rootView,messageInput,icons);
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

    private void sendMessage() {
        final String message = messageInput.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String currentUserRef = "messages/" + currentUid + "/" + chatUserId;
            final String chatUserRef = "messages/" + chatUserId + "/" + currentUid;

            DatabaseReference user_message_push = rootDatabase.child("messages").child(currentUserRef).child(chatUserId).push();
            push_id = user_message_push.getKey();

            final Map<String, String> ourMessageMap = new HashMap<>();
            ourMessageMap.put("message", message);
            ourMessageMap.put("type", "text");
            ourMessageMap.put("time", currentTime);
            ourMessageMap.put("from", currentUid);
            ourMessageMap.put("state", "0");

            final Map<String, String> userMessageMap = new HashMap<>();
            userMessageMap.put("message", message);
            userMessageMap.put("type", "text");
            userMessageMap.put("time", currentTime);
            userMessageMap.put("from", currentUid);
            userMessageMap.put("state" , "0");

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(currentUserRef + "/" + push_id, ourMessageMap);
            messageUserMap.put(chatUserRef + "/" + push_id, userMessageMap);

            messageInput.setText("");

            currentChatRef.child("timeStamp").setValue(ServerValue.TIMESTAMP);

            userChatRef.child("seen").setValue(false);
            userChatRef.child("timeStamp").setValue(ServerValue.TIMESTAMP);

            rootDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(final DatabaseError databaseError, final DatabaseReference databaseReference) {
                    if (databaseError == null) {

                        /////////////////////////////////////////////////////////////////////

                        userChatRef.child("lastSeenMsg").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String uid = Objects.requireNonNull(dataSnapshot.getValue()).toString();

                                if(uid.equals("null")) {
                                    userChatRef.child("lastSeenMsg").setValue(push_id).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            currentChatRef.child("lastSeenMsg").setValue(push_id);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                        ////////////////////////////////////////////////////////////////////////////////////////

                            currentChatRef.child("lastMsg").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    lastMsg = Objects.requireNonNull(dataSnapshot.getValue()).toString();

                                    if(!lastMsg.equals("null")) {

                                        myMessageData.orderByKey().startAt(lastMsg).addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                String uid = dataSnapshot.getKey();
                                                if(isConnected()) {
                                                    assert uid != null;

                                                    if(!uid.equals(lastMsg)) {

                                                        myMessageData.child(uid).child("state").setValue("1").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                            }
                                                        });
                                                    }
                                                }

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

                                    } else {
                                        if(isConnected()) {
                                            myMessageData.child(push_id).child("state").setValue("1").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            userChatRef.child("lastMsg").setValue(push_id).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    currentChatRef.child("lastMsg").setValue(push_id).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    });
                                }
                            });


                    }
                }
            });



        }
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

    private void setUpUiViews() {

        back_btn = findViewById(R.id.back_btn);
        user_image = findViewById(R.id.user_image);
        user_name = findViewById(R.id.user_name);
        lastSeen = findViewById(R.id.lastSeen);
        swipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        ImageView image_pick = findViewById(R.id.image_pick);
        camera = findViewById(R.id.camera);
        icons = findViewById(R.id.emoji);

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

    private void setUpReferences()  {

        rootDatabase = FirebaseDatabase.getInstance().getReference();
        rootDatabase.keepSynced(true);

        chatUserId = getIntent().getStringExtra("profile_user_id");

        usersData = rootDatabase.child("Users");

        currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        myMessageData = rootDatabase.child("messages").child(currentUid).child(chatUserId);

        userMessageData = rootDatabase.child("messages").child(chatUserId).child(currentUid);

        chatRef = rootDatabase.child("Chat");

        currentChatRef = chatRef.child(currentUid).child(chatUserId);

        userChatRef = chatRef.child(chatUserId).child(currentUid);

    }

    private void setUpChatUser() {

        final String thumbnail = getIntent().getStringExtra("thumbnail");

        final String image = getIntent().getStringExtra("image");

        final String userName = getIntent().getStringExtra("userName");

        user_name.setText(userName);


        assert thumbnail != null;
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
                if (!thumbnail.equals("default")) {
                    Uri imageUri = Uri.parse(image);
                    Intent intent = new Intent(v.getContext(), FullScreenImageView.class);
                    intent.setData(imageUri);
                    startActivity(intent);
                } else {
                    Toast.makeText(v.getContext(), "No Profile Picture", Toast.LENGTH_SHORT).show();
                }
            }
        });

        usersData.child(chatUserId).child("online").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String seenTime = Objects.requireNonNull(dataSnapshot.getValue()).toString();

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

    private void setUpChatPage() {

        currentChatRef.child("seen").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null) {
            return true;
        } else return false;
    }


    private void setFirebaseAdapter(int presentIndex) {

        // Firebase Content

        messagesFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(myMessageData.limitToLast(presentIndex) , Messages.class).build();

        messageAdapter = new FirebaseRecyclerAdapter<Messages, MessageViewHolder>(messagesFirebaseRecyclerOptions) {



            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
                return new MessageViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i, @NonNull Messages messages) {

                if (messages.getFrom().equals(currentUid)) {
                    messageViewHolder.messageLayout.setGravity(Gravity.RIGHT);
                    messageViewHolder.message.setText(messages.getMessage());
                    messageViewHolder.layout_bg.setBackgroundResource(R.drawable.background_right);
                    messageViewHolder.stamp.setVisibility(View.VISIBLE);
                    messageViewHolder.message_time.setText(messages.getTime());

                    String state = messages.getState();

                    if (state.equals("2")) {
                        messageViewHolder.stamp.setBackgroundResource(R.drawable.greentick);
                    } else if (state.equals("1")) {
                        messageViewHolder.stamp.setBackgroundResource(R.drawable.blacktick);
                    } else {
                        messageViewHolder.stamp.setBackgroundResource(R.drawable.timer);
                    }

                } else if (messages.getFrom().equals(chatUserId)) {
                    messageViewHolder.layout_bg.setBackgroundResource(R.drawable.background_left);
                    messageViewHolder.stamp.setVisibility(View.GONE);
                    messageViewHolder.messageLayout.setGravity(Gravity.LEFT);
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
                messageRecyclerView.smoothScrollToPosition(newIndex);
            }
        };

        messageRecyclerView.setAdapter(messageAdapter);

        messageAdapter.startListening();

        // Firebase Content Completed

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

    @Override
    protected void onStart() {
        super.onStart();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (check) {

                    try {

                        Thread.sleep(500);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    currentChatRef.child("lastSeenMsg").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String uid = Objects.requireNonNull(dataSnapshot.getValue()).toString();

                            myMessageData.orderByKey().startAt(uid).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    String from = Objects.requireNonNull(dataSnapshot.child("from").getValue()).toString();

                                    if (!from.equals(currentUid) && check) {

                                        final String key = dataSnapshot.getKey();
                                        assert key != null;

                                        userMessageData.child(key).child("state").setValue("2").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                chatRef.child(currentUid).child(chatUserId).child("lastSeenMsg").setValue(key).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });
                                            }
                                        });
                                    }

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

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        }); thread.start();


    }

    @Override
    protected void onStop() {

        check = false;

        thread.interrupt();

        super.onStop();


    }
}

