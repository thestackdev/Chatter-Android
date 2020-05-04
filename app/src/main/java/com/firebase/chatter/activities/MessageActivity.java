package com.firebase.chatter.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.chatter.DetailsActivity;
import com.firebase.chatter.R;
import com.firebase.chatter.helper.AppAccents;
import com.firebase.chatter.helper.GetTimeAgo;
import com.firebase.chatter.helper.PopUpMenuHelper;
import com.firebase.chatter.models.Messages;
import com.firebase.chatter.models.SelectedItemsModel;
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

    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");


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

    private LinearLayout message_selected_bar, message_bar;
    private TextView msg_selected_count;
    private ImageView back_btn_msg_selected, msg_selected_reply, msg_selected_fav,
            msg_selected_details, msg_selected_delete, msg_selected_forward, msg_selected_copy;
    private AppAccents appAccents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setUpUiViews();
        setUpReferences();
        setUpChatUser();

        back_btn_msg_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

            final String messageRef = "messages/" + messageNode + "/Conversation";

            final String key = messageData.push().getKey();

            final Map<String, String> ourMessageMap = new HashMap<>();

            ourMessageMap.put("message", message);
            ourMessageMap.put("type", "text");
            ourMessageMap.put("from", currentUid);
            ourMessageMap.put("state", "0");
            ourMessageMap.put("times" , dateFormat.format(new Date()) + ",null,null");
            ourMessageMap.put("delete" , "null");

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

        messageData.child(currentUid).setValue(false);

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
                        .setQuery(messageData.child("Conversation").limitToLast(presentIndex) , Messages.class).build();


                messageAdapter = new FirebaseRecyclerAdapter<Messages, MessageViewHolder>(messagesFirebaseRecyclerOptions) {

                    @NonNull
                    @Override
                    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
                        return new MessageViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position, @NonNull final Messages messages) {

                        String state = messages.getState();
                        String times = messages.getTimes();

                        String[] split = times.split("," , 2);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            messageViewHolder.message.setMaxWidth(Math.toIntExact(Math.round(getMessageMaxWidth())));
                        }

                        if (messages.getFrom().equals(currentUid)) {

                            messageViewHolder.messageLayout.setGravity(Gravity.END);

                            messageViewHolder.message.setText(messages.getMessage());
                            Drawable bg_right = DrawableCompat.wrap(
                                    Objects.requireNonNull(getDrawable(R.drawable.background_right)));
                            DrawableCompat.setTint(bg_right,Color.parseColor(appAccents.getAccentColor()));
                            messageViewHolder.layout_bg.setBackground(bg_right);
                            messageViewHolder.stamp.setVisibility(View.VISIBLE);
                            messageViewHolder.message_time.setText(messages.getTimes());

                            if(messages.getDelete().equals(currentUid)) {
                                messageViewHolder.message.setText(R.string.deleted_for_you);
                                messageViewHolder.message.setTypeface(messageViewHolder.message.getTypeface(),Typeface.ITALIC);
                                messageViewHolder.message_time.setText(split[0]);
                                messageViewHolder.stamp.setVisibility(View.GONE);
                            } else {

                                messageViewHolder.message.setTypeface(messageViewHolder.message.getTypeface(),Typeface.NORMAL);

                                messageViewHolder.message.setText(messages.getMessage());
                                messageViewHolder.stamp.setVisibility(View.VISIBLE);

                                messageViewHolder.message_time.setText(split[0]);

                                if (state.equals("2")) {
                                    messageViewHolder.stamp.setBackgroundResource(R.drawable.greentick);
                                } else if (state.equals("1")) {
                                    messageViewHolder.stamp.setBackgroundResource(R.drawable.blacktick);
                                } else {
                                    messageViewHolder.stamp.setBackgroundResource(R.drawable.timer_stamp);
                                }
                            }

                        } else if (messages.getFrom().equals(chatUserId)) {

                            if(!state.equals("2")) {

                                messageData.child(Objects.requireNonNull(getRef(position).getKey())).child("times")
                                        .setValue(split[0] + "null" +","+dateFormat.format(new Date()));

                                messageData.child(Objects.requireNonNull(getRef(position).getKey())).child("state").setValue("2");
                            }

                            messageViewHolder.layout_bg.setBackgroundResource(R.drawable.background_left);
                            messageViewHolder.stamp.setVisibility(View.GONE);
                            messageViewHolder.messageLayout.setGravity(Gravity.START);
                            messageViewHolder.message.setText(messages.getMessage());

                            messageViewHolder.message_time.setText(split[0]);

                        }

                        final SelectedItemsModel selectedItemsModel = new SelectedItemsModel(
                                position,
                                messageViewHolder.itemView,
                                currentUid,
                                messages.getFrom());

                        if (selectedItems.containsKey(position)){
                            selectMsg(messageViewHolder.itemView);
                            checkMsg(selectedItemsModel);
                        }

                        messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                if (!messages.getDelete().equals("null")){
                                    return false;
                                }

                                selectedItems.put(position, selectedItemsModel);
                                message_bar.setVisibility(View.INVISIBLE);
                                message_selected_bar.setVisibility(View.VISIBLE);
                                selectMsg(messageViewHolder.itemView);
                                checkMsg(selectedItemsModel);
                                return true;
                            }
                        });

                        messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                if(!messages.getDelete().equals(currentUid)) {
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
                                }

                                if (messages.getState().equals("0")){
                                    return;
                                }

                                if (!messages.getDelete().equals("null")){
                                    return;
                                }

                                PopupMenu popupMenu = new PopupMenu(v.getContext(), messageViewHolder.message);
                                popupMenu.inflate(R.menu.message_popup_menu);
                                PopUpMenuHelper.insertMenuItemIcons(messageViewHolder.itemView.getContext(), popupMenu);
                                popupMenu.show();

                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()){

                                            case R.id.copy_menu:
                                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                                ClipData clip = ClipData.newPlainText("chatter_message", messages.getMessage());

                                                assert clipboard != null;
                                                clipboard.setPrimaryClip(clip);
                                                Toast.makeText(MessageActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                                                break;

                                            case R.id.forward_menu:
                                                //TODO
                                                Toast.makeText(MessageActivity.this, "Forward W.I.P", Toast.LENGTH_SHORT).show();
                                                break;

                                            case R.id.delete_for_me_menu:

                                                messageData.child(Objects.requireNonNull(getRef(position).getKey())).child("delete")
                                                        .addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if(dataSnapshot.hasChild(chatUserId)) {
                                                                    getRef(position).removeValue();
                                                                    notifyDataSetChanged();
                                                                } else {
                                                                    getRef(position).child("delete").setValue(currentUid);
                                                                    notifyDataSetChanged();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                                break;

                                            case R.id.delete_for_all_menu:

                                                getRef(position).removeValue(new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        notifyDataSetChanged();
                                                    }
                                                });

                                                break;

                                            case R.id.details_menu:
                                                Intent intent = new Intent(MessageActivity.this , DetailsActivity.class);
                                                startActivity(intent);
                                                break;
                                        }
                                        return true;
                                    }
                                });
                            }
                        });

                    }

                    @Override
                    public void onChildChanged(@NonNull ChangeEventType type, @NonNull DataSnapshot snapshot, int newIndex, int oldIndex) {
                        super.onChildChanged(type, snapshot, newIndex, oldIndex);

                        userChatRef.child("seen").setValue(true);
                        userChatRef.child("timeStamp").setValue(ServerValue.TIMESTAMP);

                        notifyDataSetChanged();

                        if(newIndex > oldIndex) {
                            messageRecyclerView.smoothScrollToPosition(newIndex);
                        }
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

        MessageViewHolder(@NonNull final View itemView) {
            super(itemView);

            stamp = itemView.findViewById(R.id.stamp);
            message = itemView.findViewById(R.id.user_message);
            messageLayout = itemView.findViewById(R.id.message_single_layout);
            layout_bg = itemView.findViewById(R.id.layout_bg);
            message_time = itemView.findViewById(R.id.message_time);
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
        double s = Double.parseDouble(String.valueOf(size.x)) - (Double.parseDouble(String.valueOf(size.x)) * .50);
        return s;
    }

    @Override
    public void onBackPressed() {
        if (selectedItems.size()>0){

            for (int key : selectedItems.keySet()){
                SelectedItemsModel selectedItemsModel = selectedItems.get(key);
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
        super.onPause();
        messageData.child(currentUid).setValue(true);
    }
}

