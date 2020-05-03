package com.firebase.chatter.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference usersData;
    private DatabaseReference messageData;
    private DatabaseReference chatRef;

    public ChatsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.chat_frg_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setLayoutManager(layoutManager);

        DatabaseReference rootData = FirebaseDatabase.getInstance().getReference();
        String current_Uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        messageData = rootData.child("messages");

        usersData = rootData.child("Users");

        chatRef = rootData.child("Chat").child(current_Uid);

        return view;
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

                        Query lastMsg = messageData.child(chat.messageNode).orderByKey().limitToLast(1);

                                lastMsg.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                        chatsViewHolder.message.setText(Objects.requireNonNull(dataSnapshot.child("message").getValue()).toString());
                                        chatsViewHolder.time.setText(Objects.requireNonNull(dataSnapshot.child("time").getValue()).toString());

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

                                Intent intent = new Intent(v.getContext(), MessageActivity.class);
                                intent.putExtra("profile_user_id", key );
                                intent.putExtra("userName", name);
                                intent.putExtra("thumbnail", thumbnail);
                                intent.putExtra("image", image);
                                intent.putExtra("messageNode", chat.messageNode);
                                startActivity(intent);

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
}
