package com.firebase.chatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.chatter.activities.FullScreenImageView;
import com.firebase.chatter.activities.ProfileActivity;
import com.firebase.chatter.fragments.FriendsFragment;
import com.firebase.chatter.models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ForwardActivity extends AppCompatActivity {
    private RecyclerView forwardRecyclerView;
    private DatabaseReference friendsData , usersData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward);

         forwardRecyclerView = findViewById(R.id.forward_recyclerView);

        FirebaseRecyclerOptions<Friends> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendsData , Friends.class).build();

        //friendsViewHolder.online.setColorFilter(Color.RED);
        FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendsViewHolder>(firebaseRecyclerOptions) {

            @Override
            protected void onBindViewHolder(@NonNull final FriendsFragment.FriendsViewHolder friendsViewHolder, int i, @NonNull final Friends friends) {

                final String friendUId = getRef(i).getKey();
                friendsViewHolder.time_user.setVisibility(View.GONE);

                assert friendUId != null;
                usersData.child(friendUId).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                        final String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                        final String thumbnail = Objects.requireNonNull(dataSnapshot.child("thumbnail").getValue()).toString();
                        final String online = Objects.requireNonNull(dataSnapshot.child("online").getValue()).toString();

                        friendsViewHolder.name.setText(name);
                        friendsViewHolder.status.setText(friends.getDate());

                        if (!online.equals("true")) {
                            //friendsViewHolder.online.setColorFilter(Color.RED);
                        }

                        if (!thumbnail.equals("default")) {
                            Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.avatar).into(friendsViewHolder.user_image, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(thumbnail).placeholder(R.drawable.avatar).into(friendsViewHolder.user_image);
                                }
                            });
                        }

                        friendsViewHolder.user_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!thumbnail.equals("default")) {
                                    String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                                    Uri imageUri = Uri.parse(image);
                                    Intent intent = new Intent(v.getContext(), FullScreenImageView.class);
                                    intent.setData(imageUri);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(v.getContext(), "No Profile Picture", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                friendsViewHolder.user_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });


                friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                        intent.putExtra("profile_user_id", friendUId);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FriendsFragment.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);

                return new FriendsFragment.FriendsViewHolder(view);
            }
        };
        forwardRecyclerView.setAdapter(adapter);
        adapter.startListening();


    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        private TextView name , status, time_user;
        CircleImageView user_image;

        FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.single_name);
            status = itemView.findViewById(R.id.single_status);
            user_image = itemView.findViewById(R.id.users_single_image);
            time_user = itemView.findViewById(R.id.time_user);

        }
    }
}
