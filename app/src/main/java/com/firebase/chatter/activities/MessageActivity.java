package com.firebase.chatter.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.chatter.R;
import com.firebase.chatter.helper.AppAccents;
import com.firebase.chatter.helper.GetTimeAgo;
import com.firebase.chatter.helper.RecyclerItemTouchHelper;
import com.firebase.chatter.helper.RecyclerItemTouchHelperListener;
import com.firebase.chatter.models.Chat;
import com.firebase.chatter.models.Messages;
import com.firebase.chatter.models.SelectedItemsModel;
import com.firebase.chatter.models.Users;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static a.gautham.library.helper.Helper.isNetworkAvailable;

public class MessageActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    private DatabaseReference messageData;
    private DatabaseReference notificationData;

    private String messageNode;

    private Chat chat;
    private Users users;

    Handler handler = new Handler();
    Runnable runnable;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);

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
    private Map<Integer, SelectedItemsModel> selectedItems = new HashMap<>();

    private LinearLayout message_selected_bar, message_bar, message_container;
    private TextView msg_selected_count;
    private ImageView back_btn_msg_selected, msg_selected_reply, msg_selected_fav,
            msg_selected_details, msg_selected_delete, msg_selected_forward, msg_selected_copy, message_reply_close;
    private AppAccents appAccents;
    private RelativeLayout message_reply_container;
    private TextView reply_username, reply_message;
    private String copiedMessages = "", replyMsg = "", replyName = "";
    private boolean isReply = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setUpUiViews();
        setUpReferences();
        setUpChatUser();

        back_btn_msg_selected.setOnClickListener(v -> onBackPressed());

        back_btn.setOnClickListener(v -> onBackPressed());

        swipeRefreshLayout.setOnRefreshListener(() -> {

            presentIndex = presentIndex + 20;

            setFirebaseAdapter(presentIndex);

            swipeRefreshLayout.setRefreshing(false);

        });

        btnSend.setOnClickListener(v -> {
                    userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                createChatPage(currentUid, chatUserId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    sendMessage();
                }
        );

        final RelativeLayout rootView = findViewById(R.id.root);

        icons.setOnClickListener(v -> {

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
        });

    }

    private void sendMessage() {

        try {

            final String message = messageInput.getText().toString();

            if (!TextUtils.isEmpty(message)) {

                final String key = messageData.push().getKey();
                final String currentTime = dateFormat.format(new Date());

                String notificationRoute = "Notifications/" + key;

                final Map<Object, Object> MessageMap = new HashMap<>();

                MessageMap.put("message", message);
                MessageMap.put("type", "text");
                MessageMap.put("from", currentUid);
                MessageMap.put("state", 0);
                MessageMap.put("delete" , "null");

                if (isReply && !replyMsg.equals("") && !replyName.equals("")) {
                    MessageMap.put("reply_message", replyMsg);
                    MessageMap.put("reply_username", replyName);
                    isReply = false;
                    message_reply_container.setVisibility(View.GONE);
                    message_container.setBackground(getDrawable(R.drawable.message_background));

                } else {

                    MessageMap.put("reply_message", "null");
                    MessageMap.put("reply_username", "null");
                }


                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("messages/" + messageNode + "/" + key, MessageMap);

                messageInput.setText("");

                userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {

                        chat = dataSnapshot.getValue(Chat.class);

                        int unSeen = chat.getUnSeen();

                            userChatRef.child("unSeen").setValue(++unSeen).addOnSuccessListener(aVoid -> { });

                            if (!chat.getWatching()) {

                                Map<String, String> notificationMap = new HashMap<>();
                                notificationMap.put("from", currentUid);
                                notificationMap.put("to", chatUserId);
                                notificationMap.put("message", message);
                                notificationMap.put("times", currentTime + ",null,null");
                                MessageMap.put("times", currentTime + ",null,null");
                                messageMap.put(notificationRoute, notificationMap);

                                rootDatabase.updateChildren(messageMap, (databaseError, databaseReference) -> {
                                    if (databaseError == null) {
                                        userChatRef.child("seen").setValue(false);
                                        userChatRef.child("timeStamp").setValue(ServerValue.TIMESTAMP);
                                        currentChatRef.child("timeStamp").setValue(ServerValue.TIMESTAMP);
                                    }
                                });
                            } else {
                                MessageMap.put("times", currentTime + "," + currentTime + ",null");
                                rootDatabase.updateChildren(messageMap, (databaseError, databaseReference) -> {
                                    if (databaseError == null) {
                                        userChatRef.child("seen").setValue(false);
                                        userChatRef.child("timeStamp").setValue(ServerValue.TIMESTAMP);
                                        currentChatRef.child("timeStamp").setValue(ServerValue.TIMESTAMP);
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(MessageActivity.this , "SomeThing Went Wrong" , Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {

        lastSeen.setText("Fetching...");

        currentChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    currentChatRef.child("unSeen").setValue(0);
                    currentChatRef.child("watching").setValue(true);
                    currentChatRef.child("watching").onDisconnect().setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        handler.postDelayed(runnable = () -> {
            handler.postDelayed(runnable , 2000);

            usersData.child(chatUserId).child("online").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String seenTime = Objects.requireNonNull(dataSnapshot.getValue()).toString();

                    if (seenTime.equals("true")) {
                        lastSeen.setText(R.string.online);
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
        }, 2000);

            usersData.child(currentUid).child("online").setValue("true").addOnSuccessListener(aVoid -> {});

        super.onResume();

        setFirebaseAdapter(presentIndex);

        btnSend.setVisibility(View.GONE);

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

                if(s.toString().trim().length() > 0) {
                    btnSend.setVisibility(View.VISIBLE);
                } else {
                    btnSend.setVisibility(View.GONE);
                }
            }
        });

    }

    private void setUpUiViews() {

        users = (Users) getIntent().getSerializableExtra("users");

        back_btn = findViewById(R.id.back_btn);
        user_image = findViewById(R.id.user_image);
        user_name = findViewById(R.id.user_name);
        lastSeen = findViewById(R.id.lastSeen);
        swipeRefreshLayout = findViewById(R.id.swipeToRefresh);

        camera = findViewById(R.id.camera);
        icons = findViewById(R.id.emoji);

        btnSend = findViewById(R.id.send_message);
        messageInput = findViewById(R.id.message_input);

        messageRecyclerView = findViewById(R.id.message_recyclerView);

        message_selected_bar = findViewById(R.id.message_selected_bar);
        message_bar = findViewById(R.id.message_bar);
        msg_selected_count = findViewById(R.id.msg_selected_count);
        back_btn_msg_selected = findViewById(R.id.back_btn_msg_selected);
        msg_selected_reply = findViewById(R.id.msg_selected_reply);
        msg_selected_fav = findViewById(R.id.msg_selected_fav);
        msg_selected_details = findViewById(R.id.msg_selected_details);
        msg_selected_delete = findViewById(R.id.msg_selected_delete);
        msg_selected_forward = findViewById(R.id.msg_selected_forward);
        msg_selected_copy = findViewById(R.id.msg_selected_copy);

        message_container = findViewById(R.id.message_container);
        message_reply_container = findViewById(R.id.message_reply_container);
        message_reply_close = findViewById(R.id.message_reply_close);
        reply_username = findViewById(R.id.reply_username);
        reply_message = findViewById(R.id.reply_message);

        appAccents = new AppAccents(this);
        appAccents.init();

        message_bar.setBackgroundColor(Color.parseColor(appAccents.getAccentColor()));
        back_btn.setColorFilter(Color.parseColor(appAccents.getTextColor()));

        message_selected_bar.setBackgroundColor(Color.parseColor(appAccents.getAccentColor()));
        back_btn_msg_selected.setColorFilter(Color.parseColor(appAccents.getTextColor()));
        msg_selected_count.setTextColor(Color.parseColor(appAccents.getTextColor()));
        msg_selected_reply.setColorFilter(Color.parseColor(appAccents.getTextColor()));
        msg_selected_fav.setColorFilter(Color.parseColor(appAccents.getTextColor()));
        msg_selected_details.setColorFilter(Color.parseColor(appAccents.getTextColor()));
        msg_selected_delete.setColorFilter(Color.parseColor(appAccents.getTextColor()));
        msg_selected_copy.setColorFilter(Color.parseColor(appAccents.getTextColor()));
        msg_selected_forward.setColorFilter(Color.parseColor(appAccents.getTextColor()));

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(appAccents.getAccentColor()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);

        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //Swipe To Reply Message
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
                new RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(messageRecyclerView);

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

        messageData = rootDatabase.child("messages").child(messageNode);

        notificationData = rootDatabase.child("Notifications");

    }

    private void setUpChatUser() {

        user_name.setText(users.getName());

        if (!users.getThumbnail().equals("default")) {
            Picasso.get().load(users.getThumbnail()).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.avatar).into(user_image, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(users.getThumbnail()).placeholder(R.drawable.avatar).into(user_image);

                }
            });
        }

        user_image.setOnClickListener(v -> {
            if (!users.getThumbnail().equals("default")) {
                Uri imageUri = Uri.parse(users.getImage());
                Intent intent = new Intent(v.getContext(), FullScreenImageView.class);
                intent.setData(imageUri);
                startActivity(intent);
            } else {
                Toast.makeText(v.getContext(), "No Profile Picture", Toast.LENGTH_SHORT).show();
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
                    protected void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position, @NonNull final Messages messages) {

                        try {

                            if(messages.getDelete() != null && messages.getDelete().equals(currentUid)) {
                                messageViewHolder.itemView.setVisibility(View.GONE);
                                messageViewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0 , 0));
                            }

                            final String times = messages.getTimes();
                            String[] split = times.split("," , 3);

                            split[2] = dateFormat.format(new Date());


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            messageViewHolder.message.setMaxWidth(Math.toIntExact(Math.round(getMessageMaxWidth())));
                        }

                        messageViewHolder.message_time.setVisibility(View.GONE);

                        msg_selected_reply.setOnClickListener(v -> replyMessage(position));

                        msg_selected_fav.setOnClickListener(v -> {
                            //TODO
                        });

                        msg_selected_delete.setOnClickListener(v -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setTitle("Are You Sure ?").setCancelable(false);

                            builder.setPositiveButton("Delete For EveryOne", (dialog, which) -> {
                                for(int items : selectedItems.keySet()) {

                                    getRef(items).removeValue().addOnSuccessListener(aVoid -> { });
                                    messageData.child(getRef(items).getKey()).removeValue();
                                    deSelectMsg(Objects.requireNonNull(selectedItems.get(items)).getView());

                                }
                                message_selected_bar.setVisibility(View.INVISIBLE);
                                message_bar.setVisibility(View.VISIBLE);
                                selectedItems.clear();
                            });

                            builder.setNegativeButton("Delete For Me" , ((dialog, which) -> {
                                for(int items : selectedItems.keySet()) {

                                    getRef(items).child("delete").setValue(currentUid).addOnSuccessListener(aVoid -> {});

                                    deSelectMsg(Objects.requireNonNull(selectedItems.get(items)).getView());

                                }

                                message_selected_bar.setVisibility(View.INVISIBLE);
                                message_bar.setVisibility(View.VISIBLE);
                                selectedItems.clear();
                            }));

                            builder.setNeutralButton("Cancel" , ((dialog, which) -> {
                                dialog.dismiss();
                            }));

                            builder.create().show();
                        });

                        msg_selected_forward.setOnClickListener(v -> {
                            //TODO
                        });

                        msg_selected_copy.setOnClickListener(v -> {

                                for (int key : selectedItems.keySet()){

                                    if (copiedMessages.equals("")) {
                                        copiedMessages = String.format("%s",selectedItems.get(key).getMessage());
                                    }
                                    else {
                                        copiedMessages = String.format("%s\n%s",copiedMessages,selectedItems.get(key).getMessage());
                                    }

                                    deSelectMsg(Objects.requireNonNull(selectedItems.get(key)).getView());
                                }

                                message_selected_bar.setVisibility(View.INVISIBLE);
                                message_bar.setVisibility(View.VISIBLE);
                                selectedItems.clear();


                            copySelectedMessagesToClipBoard(copiedMessages);

                        });

                        if (Objects.requireNonNull(messages.getReply_message()).equals("null")){

                            messageViewHolder.replied_message_layout.setVisibility(View.GONE);
                            messageViewHolder.replied_username_tv.setVisibility(View.GONE);
                            messageViewHolder.replied_message_tv.setVisibility(View.GONE);

                        }else {

                            messageViewHolder.replied_username_tv.setText(messages.getReply_username());
                            messageViewHolder.replied_message_tv.setText(messages.getReply_message());

                            messageViewHolder.replied_message_layout.setVisibility(View.VISIBLE);
                            messageViewHolder.replied_username_tv.setVisibility(View.VISIBLE);
                            messageViewHolder.replied_message_tv.setVisibility(View.VISIBLE);

                        }

                        if (messages.getFrom().equals(currentUid)) {
                            if(messages.getState() == 0 && isNetworkAvailable(getApplicationContext())) {
                                messageData.child(Objects.requireNonNull(getRef(position).getKey()))
                                        .child("state").setValue(1).addOnSuccessListener(aVoid -> {
                                });
                            }

                            messageViewHolder.messageLayout.setGravity(Gravity.END);
                            messageViewHolder.message_time.setGravity(Gravity.END);

                            messageViewHolder.message.setText(messages.getMessage());

                            Drawable bg_right = DrawableCompat.wrap(Objects.requireNonNull(getDrawable(R.drawable.background_right)));

                            DrawableCompat.setTint(bg_right,Color.parseColor(appAccents.getAccentColor()));
                            messageViewHolder.layout_bg.setBackground(bg_right);
                            messageViewHolder.stamp.setVisibility(View.VISIBLE);
                            messageViewHolder.message_time.setText(split[0]);
                            messageViewHolder.message.setText(messages.getMessage());

                                switch (messages.getState()) {
                                    case 3:
                                        messageViewHolder.stamp.setBackgroundResource(R.drawable.ic_tick_green);
                                        break;
                                    case 2:
                                        messageViewHolder.stamp.setBackgroundResource(R.drawable.delivered_stamp);
                                        break;
                                    case 1:
                                        messageViewHolder.stamp.setBackgroundResource(R.drawable.ic_tick_blue);
                                        break;
                                    default:
                                        messageViewHolder.stamp.setBackgroundResource(R.drawable.ic_message_pending);
                                        break;
                                }


                        } else if (messages.getFrom().equals(chatUserId)) {

                            if(messages.getState() == 2) {

                                userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) {

                                            messageData.child(Objects.requireNonNull(getRef(position).getKey())).child("times")
                                                    .setValue(split[0] +","+ split[1] +","+split[2]);

                                            messageData.child(Objects.requireNonNull(getRef(position)
                                                    .getKey())).child("state").setValue("3");

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            messageViewHolder.layout_bg.setBackgroundResource(R.drawable.background_left);
                            messageViewHolder.stamp.setVisibility(View.GONE);

                            messageViewHolder.messageLayout.setGravity(Gravity.START);
                            messageViewHolder.message_time.setGravity(Gravity.START);

                            messageViewHolder.message.setText(messages.getMessage());

                            messageViewHolder.message_time.setText(split[0]);

                        }

                        } catch (NullPointerException e) {
                            finish();
                            e.printStackTrace();
                        }

                        final SelectedItemsModel selectedItemsModel = new SelectedItemsModel(
                                position,
                                messageViewHolder.itemView,
                                currentUid,
                                messages.getFrom(),
                                messages.getMessage(),
                                messages.getTimes());

                        if (selectedItems.containsKey(position)){
                            selectMsg(messageViewHolder.itemView);
                            checkMsg(selectedItemsModel);
                        }

                        messageViewHolder.itemView.setOnLongClickListener(v -> {

                            selectedItems.put(position, selectedItemsModel);
                            message_bar.setVisibility(View.INVISIBLE);
                            message_selected_bar.setVisibility(View.VISIBLE);
                            selectMsg(messageViewHolder.itemView);
                            checkMsg(selectedItemsModel);
                            return true;
                        });

                        messageViewHolder.itemView.setOnClickListener(v -> {

                            if (selectedItems.size()>0){

                                if (selectedItems.containsKey(position)){
                                    selectedItems.remove(position);
                                    deSelectMsg(messageViewHolder.itemView);
                                    checkMsg(selectedItemsModel);
                                    return;
                                }

                                selectedItems.put(position,selectedItemsModel);
                                selectMsg(messageViewHolder.itemView);
                                checkMsg(selectedItemsModel);
                                return;

                            }

                            if (messages.getState() == 0){
                                return;
                            }

                            if (messageViewHolder.message_time.getVisibility() == View.VISIBLE)
                                messageViewHolder.message_time.setVisibility(View.GONE);
                            else
                                messageViewHolder.message_time.setVisibility(View.VISIBLE);

                        });

                    }

                    @Override
                    public void onChildChanged(@NonNull ChangeEventType type, @NonNull DataSnapshot snapshot, int newIndex, int oldIndex) {
                        super.onChildChanged(type, snapshot, newIndex, oldIndex);

                        if(newIndex > oldIndex) {

                            userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        currentChatRef.child("seen").setValue(true);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            messageRecyclerView.smoothScrollToPosition(newIndex);

                            notifyDataSetChanged();
                        }
                    }
                };


                messageRecyclerView.setAdapter(messageAdapter);

                messageAdapter.startListening();

    }

    private void copySelectedMessagesToClipBoard(String copiedMessages) {

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText("chatter_message", copiedMessages);

        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MessageActivity.this, "Copied", Toast.LENGTH_SHORT).show();

    }

    private void replyMessage(int position){

        isReply = true;

        messageInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(messageInput, InputMethodManager.SHOW_IMPLICIT);
        }
        message_container.setBackground(getDrawable(R.drawable.message_background_reply));
        message_reply_container.setVisibility(View.VISIBLE);
        messageAdapter.notifyDataSetChanged();

        String message = messageAdapter.getItem(position).getMessage();
        String from = messageAdapter.getItem(position).getFrom();

        if (from.equals(currentUid)){
            reply_username.setText(R.string.you);
        }else {
            reply_username.setText(users.getName());
        }

        reply_message.setText(message);

        message_reply_close.setOnClickListener(v -> {
            message_container.setBackground(getDrawable(R.drawable.message_background));
            message_reply_container.setVisibility(View.GONE);
            isReply = false;
        });

        replyMsg = messageAdapter.getItem(position).getMessage();
        if (messageAdapter.getItem(position).getFrom().equals(currentUid))
            replyName = "You";
        else
            replyName = users.getName();

    }

    private void showMessageTime(int position, RecyclerView.ViewHolder viewHolder){

        messageAdapter.notifyDataSetChanged();
        TextView time = viewHolder.itemView.findViewById(R.id.message_time);
        time.setVisibility(View.VISIBLE);

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof MessageViewHolder){

            replyMessage(position);

        }

    }

    // Firebase Content Completed

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView message, message_time, replied_username_tv, replied_message_tv;
        private ImageView stamp;
        private LinearLayout messageLayout, layout_bg, replied_message_layout;

        MessageViewHolder(@NonNull final View itemView) {
            super(itemView);

            stamp = itemView.findViewById(R.id.stamp);
            message = itemView.findViewById(R.id.user_message);
            messageLayout = itemView.findViewById(R.id.message_single_layout);
            layout_bg = itemView.findViewById(R.id.layout_bg);
            message_time = itemView.findViewById(R.id.message_time);
            replied_message_layout = itemView.findViewById(R.id.replied_message_layout);
            replied_username_tv = itemView.findViewById(R.id.replied_username);
            replied_message_tv = itemView.findViewById(R.id.replied_message);

        }
    }

    private void selectMsg(View view){
        view.setBackgroundColor(getResources().getColor(R.color.message_selected));
        msg_selected_count.setText(String.valueOf(selectedItems.size()));
    }

    private void deSelectMsg(View view){
        view.setBackgroundColor(Color.TRANSPARENT);
        msg_selected_count.setText(String.valueOf(selectedItems.size()));

        if (selectedItems.size()==0){
            message_bar.setVisibility(View.VISIBLE);
            message_selected_bar.setVisibility(View.GONE);
        }

    }

    private void checkMsg(SelectedItemsModel selectedItemsModel){

        if (selectedItems.size()>1){
            msg_selected_reply.setVisibility(View.GONE);
            msg_selected_fav.setVisibility(View.VISIBLE);
            msg_selected_details.setVisibility(View.GONE);
            msg_selected_delete.setVisibility(View.VISIBLE);
            msg_selected_copy.setVisibility(View.VISIBLE);
            msg_selected_forward.setVisibility(View.VISIBLE);
            return;
        }

        if (selectedItems.size()==1){

            if (selectedItemsModel.getFromUid().equals(selectedItemsModel.getCurrentUid())){

                msg_selected_reply.setVisibility(View.VISIBLE);
                msg_selected_fav.setVisibility(View.VISIBLE);
                msg_selected_details.setVisibility(View.VISIBLE);
                msg_selected_delete.setVisibility(View.VISIBLE);
                msg_selected_copy.setVisibility(View.VISIBLE);
                msg_selected_forward.setVisibility(View.VISIBLE);

                return;

            }

            msg_selected_reply.setVisibility(View.VISIBLE);
            msg_selected_fav.setVisibility(View.VISIBLE);
            msg_selected_details.setVisibility(View.GONE);
            msg_selected_delete.setVisibility(View.VISIBLE);
            msg_selected_copy.setVisibility(View.VISIBLE);
            msg_selected_forward.setVisibility(View.VISIBLE);

        }

    }

    public double getMessageMaxWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return Double.parseDouble(String.valueOf(size.x)) - (Double.parseDouble(String.valueOf(size.x)) * .50);
    }

    @Override
    public void onBackPressed() {
        if (selectedItems.size()>0){

            for (int key : selectedItems.keySet()){
                SelectedItemsModel selectedItemsModel = selectedItems.get(key);
                assert selectedItemsModel != null;
                View view = selectedItemsModel.getView();

                deSelectMsg(view);
            }

            selectedItems.clear();
            message_bar.setVisibility(View.VISIBLE);
            message_selected_bar.setVisibility(View.GONE);

            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {

        currentChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    currentChatRef.child("watching").setValue(false);
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        super.onPause();

        handler.removeCallbacks(runnable);

        messageAdapter.stopListening();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            String userID = firebaseUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(userID).child("online");
            databaseReference.setValue(ServerValue.TIMESTAMP).addOnSuccessListener(aVoid -> {

            });
        }
    }

    private void createChatPage(final String current_uid, final String profile_user_id) {

        Map<String, Object> addChatMap = new HashMap<>();

        addChatMap.put("seen", false);
        addChatMap.put("timeStamp", ServerValue.TIMESTAMP);
        addChatMap.put("watching" , false);
        addChatMap.put("unSeen" , 0);
        addChatMap.put("messageNode" , messageNode);

        Map<String, Object> chatUserMap = new HashMap<>();

        chatUserMap.put("Chat/" + profile_user_id + "/" + current_uid, addChatMap);
        chatUserMap.put("Chat/" + current_uid + "/" + profile_user_id, addChatMap);


        rootDatabase.updateChildren(chatUserMap, (databaseError, databaseReference) -> {

        });
    }

}

