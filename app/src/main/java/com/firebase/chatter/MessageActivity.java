package com.firebase.chatter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.firebase.auth.FirebaseAuth;
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

    private DatabaseReference messageData;

    private String messageNode;

    private ImageView back_btn, btnSend;
    private CircleImageView user_image;
    private TextView user_name, lastSeen;
    private EmojiconEditText messageInput;
    private RecyclerView messageRecyclerView;
    private int presentIndex = 20;

    private DatabaseReference rootDatabase;
    private DatabaseReference usersData;
    private DatabaseReference currentChatRef;
    private DatabaseReference userChatRef;

    private String currentUid, chatUserId;
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

        icons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EmojIconActions emojIcon = new EmojIconActions(MessageActivity.this, rootView, messageInput, icons);
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

            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");

            final String messageRef = "messages/" + messageNode;

            final String key = messageData.push().getKey();

            final Map<String, String> ourMessageMap = new HashMap<>();

            ourMessageMap.put("message", message);
            ourMessageMap.put("type", "text");
            ourMessageMap.put("time", dateFormat.format(new Date()));
            ourMessageMap.put("from", currentUid);
            ourMessageMap.put("state", "0");

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put(messageRef + "/" + key, ourMessageMap);

            messageInput.setText("");

            rootDatabase.updateChildren(messageMap , new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable final DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        currentChatRef.child("seen").setValue(false);
                        currentChatRef.child("timeStamp").setValue(ServerValue.TIMESTAMP);
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
                if (messageInput.getText().toString().length() ==0) {
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

    }

    private void setUpReferences() {

        rootDatabase = FirebaseDatabase.getInstance().getReference();
        rootDatabase.keepSynced(true);

        usersData = rootDatabase.child("Users");

        chatUserId = getIntent().getStringExtra("profile_user_id");

        currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference chatRef = rootDatabase.child("Chat");

        currentChatRef = chatRef.child(currentUid).child(chatUserId);

        userChatRef = chatRef.child(chatUserId).child(currentUid);

        messageNode = getIntent().getStringExtra("messageNode");

        assert messageNode != null;
        messageData = rootDatabase.child("messages").child(messageNode);


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


    private void setFirebaseAdapter(final int presentIndex) {

        // Firebase Content

                messagesFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Messages>()
                        .setQuery(messageData.limitToLast(presentIndex), Messages.class).build();


                messageAdapter = new FirebaseRecyclerAdapter<Messages, MessageViewHolder>(messagesFirebaseRecyclerOptions) {


                    @NonNull
                    @Override
                    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
                        return new MessageViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i, @NonNull Messages messages) {

                        String state = messages.getState();

                        if (messages.getFrom().equals(currentUid)) {

                            messageViewHolder.messageLayout.setGravity(Gravity.RIGHT);
                            messageViewHolder.message.setText(messages.getMessage());
                            messageViewHolder.layout_bg.setBackgroundResource(R.drawable.background_right);
                            messageViewHolder.stamp.setVisibility(View.VISIBLE);
                            messageViewHolder.message_time.setText(messages.getTime());

                            if (state.equals("2")) {
                                messageViewHolder.stamp.setBackgroundResource(R.drawable.greentick);
                            } else if (state.equals("1")) {
                                messageViewHolder.stamp.setBackgroundResource(R.drawable.blacktick);
                            } else {
                                messageViewHolder.stamp.setBackgroundResource(R.drawable.timer);
                            }

                        } else if (messages.getFrom().equals(chatUserId)) {

                            if(!state.equals("2")) {
                                messageData.child(Objects.requireNonNull(getRef(i).getKey())).child("state").setValue("2");
                            }

                            messageViewHolder.layout_bg.setBackgroundResource(R.drawable.background_left);
                            messageViewHolder.stamp.setVisibility(View.GONE);
                            messageViewHolder.messageLayout.setGravity(Gravity.LEFT);
                            messageViewHolder.message.setText(messages.getMessage());
                            messageViewHolder.message_time.setText(messages.getTime());

                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull ChangeEventType type, @NonNull DataSnapshot snapshot, int newIndex, int oldIndex) {
                        super.onChildChanged(type, snapshot, newIndex, oldIndex);

                        userChatRef.child("seen").setValue(true);
                        userChatRef.child("timeStamp").setValue(ServerValue.TIMESTAMP);

                        messageRecyclerView.smoothScrollToPosition(newIndex);

                    }

                };

                messageRecyclerView.setAdapter(messageAdapter);

                messageAdapter.startListening();

            }

        // Firebase Content Completed

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

    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}

