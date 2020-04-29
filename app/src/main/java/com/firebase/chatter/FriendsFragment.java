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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    private RecyclerView recyclerView;
    private DatabaseReference friendsData , usersData;
    private FirebaseRecyclerAdapter<Friends , FriendsViewHolder> adapter;

    public FriendsFragment () {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        String current_user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference rootData = FirebaseDatabase.getInstance().getReference();

        friendsData = rootData.child("Friends").child(current_user_id);

        usersData = rootData.child("Users");

        recyclerView = view.findViewById(R.id.friends_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendsData , Friends.class).build();

        adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(firebaseRecyclerOptions) {

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int i, @NonNull final Friends friends) {

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

                        if(!online.equals("true")) {
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
                                if(!thumbnail.equals("default")) {
                                    String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                                    Uri imageUri = Uri.parse(image);
                                    Intent intent = new Intent(v.getContext() , FullScreenImageView.class);
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
                        Intent intent = new Intent(v.getContext(),ProfileActivity.class);
                        intent.putExtra("profile_user_id",friendUId);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);

                return new FriendsViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();

        assert adapter != null;
        adapter.stopListening();

    }

    private static class FriendsViewHolder extends RecyclerView.ViewHolder {

        private TextView name , status, time_user;
        CircleImageView user_image;

        FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.single_name);
            status = itemView.findViewById(R.id.single_status);
            user_image = itemView.findViewById(R.id.users_single_image);
            time_user = itemView.findViewById(R.id.time_user);
            //online = itemView.findViewById(R.id.online);

        }
    }
}
