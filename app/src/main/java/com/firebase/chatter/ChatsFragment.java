package com.firebase.chatter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private String name;
    private String thumbnail;
    private String online;
    private String image;

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

        messageData = rootData.child("messages").child(current_Uid);

        usersData = rootData.child("Users");

        chatRef = rootData.child("Chat").child(current_Uid);

        return view;
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        private TextView name , message;
        private CircleImageView userImage;

        ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.single_name);
            message = itemView.findViewById(R.id.single_status);
            userImage = itemView.findViewById(R.id.users_single_image);
            //online = itemView.findViewById(R.id.online);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = chatRef.orderByChild("timeStamp");

        FirebaseRecyclerOptions<Chat> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Chat>()
                .setQuery(query , Chat.class).build();

        FirebaseRecyclerAdapter<Chat, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Chat, ChatsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder chatsViewHolder, final int i, @NonNull final Chat chat) {
                final String chatUid = getRef(i).getKey();

                assert chatUid != null;
                usersData.child(chatUid).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                        thumbnail = Objects.requireNonNull(dataSnapshot.child("thumbnail").getValue()).toString();
                        image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

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


                        Query lastMsgQuery = messageData.child(chatUid).limitToLast(1);

                        lastMsgQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                if (!chat.seen) {
                                    chatsViewHolder.message.setText("new message");
                                } else {
                                    chatsViewHolder.message.setText(Objects.requireNonNull(dataSnapshot.child("message").getValue()).toString());

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

                chatsViewHolder.userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!image.equals("default")) {
                            Uri imageUri = Uri.parse(image);
                            Intent intent = new Intent(v.getContext(), FullScreenImageView.class);
                            intent.setData(imageUri);
                            startActivity(intent);
                        } else {
                            Toast.makeText(v.getContext(), "No Profile Picture", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), MessageActivity.class);
                        intent.putExtra("profile_user_id", chatUid);
                        intent.putExtra("userName", name);
                        intent.putExtra("thumbnail", thumbnail);
                        intent.putExtra("image" , image);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                return new ChatsViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
}