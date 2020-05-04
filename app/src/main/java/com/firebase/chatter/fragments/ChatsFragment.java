package com.firebase.chatter.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.chatter.R;
import com.firebase.chatter.activities.FullScreenImageView;
import com.firebase.chatter.activities.MessageActivity;
import com.firebase.chatter.models.Chat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference usersData;
    private DatabaseReference messageData;
    private DatabaseReference chatRef;

    private LinearLayout chat_bar_layout;
    private TextView chat_selected_count;
    private ImageView back_btn_chat_selected, chat_selected_delete, chat_selected_info,
            chat_selected_block, select_all_chat, chat_selected_readAll;
    private Map<Integer, View> selectedItems = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        initUI(view);

        DatabaseReference rootData = FirebaseDatabase.getInstance().getReference();
        String current_Uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        messageData = rootData.child("messages");
        usersData = rootData.child("Users");
        chatRef = rootData.child("Chat").child(current_Uid);

        back_btn_chat_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelected();
            }
        });

        return view;
    }

    private void initUI(View view) {

        chat_bar_layout = view.findViewById(R.id.chat_bar_layout);
        chat_selected_count = view.findViewById(R.id.chat_selected_count);
        back_btn_chat_selected = view.findViewById(R.id.back_btn_chat_selected);
        chat_selected_delete = view.findViewById(R.id.chat_selected_delete);
        chat_selected_info = view.findViewById(R.id.chat_selected_info);
        chat_selected_block = view.findViewById(R.id.chat_selected_block);
        select_all_chat = view.findViewById(R.id.select_all_chat);
        chat_selected_readAll = view.findViewById(R.id.chat_selected_readAll);

        recyclerView = view.findViewById(R.id.chat_frg_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(layoutManager);

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        private TextView name , message , time;
        private CircleImageView userImage;
        private ImageView stamp;

        ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.chatsName);
            message = itemView.findViewById(R.id.chats_message);
            time = itemView.findViewById(R.id.chatsTime);
            userImage = itemView.findViewById(R.id.chatsImage);
            stamp = itemView.findViewById(R.id.chatsStamp);

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = chatRef.orderByChild("timeStamp");

        FirebaseRecyclerOptions<Chat> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class).build();

        FirebaseRecyclerAdapter<Chat, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Chat, ChatsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder chatsViewHolder, final int i, @NonNull final Chat chat) {

                final String key = getRef(i).getKey();

                usersData.child(Objects.requireNonNull(getRef(i).getKey())).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                        final String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                        final String thumbnail = Objects.requireNonNull(dataSnapshot.child("thumbnail").getValue()).toString();

                        chatsViewHolder.name.setText(name);

                        if (!thumbnail.equals("default")) {
                            Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.avatar).into(chatsViewHolder.userImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(thumbnail).placeholder(R.drawable.avatar).into(chatsViewHolder.userImage);

                                }
                            });
                        }

                        final String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                        chatsViewHolder.userImage.setOnClickListener(new View.OnClickListener() {
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

                        Query lastMsg = messageData.child(chat.messageNode).child("Conversation").orderByKey().limitToLast(1);

                                lastMsg.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        chatsViewHolder.message.setText(Objects.requireNonNull(dataSnapshot.child("message").getValue()).toString());

                                       String times = Objects.requireNonNull(dataSnapshot.child("times").getValue()).toString();

                                       String[] split = times.split("," , 2);

                                       chatsViewHolder.time.setText(split[0]);

                                        String from = Objects.requireNonNull(dataSnapshot.child("from").getValue()).toString();

                                        if(!from.equals(key)) {
                                            String state = Objects.requireNonNull(dataSnapshot.child("state").getValue()).toString();
                                            if (state.equals("2")) {
                                                chatsViewHolder.stamp.setBackgroundResource(R.drawable.greentick);
                                            } else if (state.equals("1")) {
                                                chatsViewHolder.stamp.setBackgroundResource(R.drawable.blacktick);
                                            } else {
                                                chatsViewHolder.stamp.setBackgroundResource(R.drawable.timer_stamp);
                                            }

                                        }else {
                                            chatsViewHolder.stamp.setVisibility(View.GONE);
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

                                if(chat.seen) {

                                    messageData.child(chat.messageNode).orderByChild("state").equalTo("1")
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Log.i("TAG", "onDataChange: "+dataSnapshot.getChildrenCount());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }


                        chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (selectedItems.size()>0){

                                    if (selectedItems.containsKey(i)){
                                        selectedItems.remove(i);
                                        deSelectMsg(v);
                                        return;
                                    }

                                    selectedItems.put(i,v);
                                    selectMsg(v);
                                    return;

                                }

                                Intent intent = new Intent(v.getContext(), MessageActivity.class);
                                intent.putExtra("profile_user_id", key );
                                intent.putExtra("userName", name);
                                intent.putExtra("thumbnail", thumbnail);
                                intent.putExtra("image", image);
                                intent.putExtra("messageNode", chat.messageNode);
                                startActivity(intent);

                            }
                        });

                        chatsViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                selectedItems.put(i,v);
                                selectMsg(v);
                                return true;
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_layout, parent, false);
                return new ChatsViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void selectMsg(View view){

        if (selectedItems.size()>0){
            chat_bar_layout.setVisibility(View.VISIBLE);
        }

        if (selectedItems.size()>1){
            chat_selected_info.setVisibility(View.GONE);
            chat_selected_block.setVisibility(View.GONE);
            chat_selected_readAll.setVisibility(View.GONE);
        }

        view.setBackgroundColor(getResources().getColor(R.color.message_selected));
        chat_selected_count.setText(String.valueOf(selectedItems.size()));
    }

    private void deSelectMsg(View view){
        view.setBackgroundColor(Color.TRANSPARENT);
        chat_selected_count.setText(String.valueOf(selectedItems.size()));

        if (selectedItems.size()==0){
            chat_bar_layout.setVisibility(View.GONE);
        }

        if (selectedItems.size()>1){
            chat_selected_info.setVisibility(View.GONE);
            chat_selected_block.setVisibility(View.GONE);
            chat_selected_readAll.setVisibility(View.GONE);
        }else {
            chat_selected_info.setVisibility(View.VISIBLE);
            chat_selected_block.setVisibility(View.VISIBLE);
            chat_selected_readAll.setVisibility(View.VISIBLE);
        }

    }

    private void clearSelected(){

        if (selectedItems.size()!=0){

            for (int key : selectedItems.keySet()){
                View view = selectedItems.get(key);
                deSelectMsg(view);
            }

            selectedItems.clear();
            chat_bar_layout.setVisibility(View.GONE);

        }

    }

}
