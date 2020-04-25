package com.firebase.chatter;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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


public class FriendRequests extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference usersData , requestsData;
    private FirebaseRecyclerAdapter<Friend_req , RequestsViewHolder> requestsAdapter;


    FriendRequests() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);

        String current_Uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference rootData = FirebaseDatabase.getInstance().getReference();

        requestsData = rootData.child("Friend_req").child(current_Uid);

        usersData = rootData.child("Users");

        recyclerView = view.findViewById(R.id.requests_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friend_req > reqFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Friend_req>()
                .setQuery(requestsData , Friend_req.class).build();

        requestsAdapter = new FirebaseRecyclerAdapter<Friend_req, RequestsViewHolder>(reqFirebaseRecyclerOptions) {

            @Override
              protected void onBindViewHolder(@NonNull final RequestsViewHolder requestsViewHolder, int i, @NonNull final Friend_req friend_req) {

                  final String uid = getRef(i).getKey();

                  assert uid != null;

                  usersData.child(uid).addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                          requestsViewHolder.name.setText(Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());

                          final String online = dataSnapshot.child("online").getValue().toString();

                          if(!online.equals("true")) {
                              requestsViewHolder.online.setColorFilter(Color.RED);

                          }



                          final String thumbnail = Objects.requireNonNull(dataSnapshot.child("thumbnail").getValue()).toString();

                          if (!thumbnail.equals("default")) {
                              Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                                      .placeholder(R.drawable.avatar).into(requestsViewHolder.userImage, new Callback() {
                                  @Override
                                  public void onSuccess() {
                                  }

                                  @Override
                                  public void onError(Exception e) {
                                      Picasso.get().load(thumbnail).placeholder(R.drawable.avatar).into(requestsViewHolder.userImage);

                                  }
                              });
                          }

                          requestsViewHolder.userImage.setOnClickListener(new View.OnClickListener() {
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

                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  });

                  requestsViewHolder.type.setText("Request Type : "+friend_req.getReq_type());

                  requestsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          Intent intent = new Intent(v.getContext(),ProfileActivity.class);
                          intent.putExtra("profile_user_id",uid);
                          startActivity(intent);
                      }
                  });
              }

              @NonNull
              @Override
              public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                  View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                  return new RequestsViewHolder(view);
              }
          };

        recyclerView.setAdapter(requestsAdapter);
        requestsAdapter.startListening();

    }

    private static class RequestsViewHolder extends RecyclerView.ViewHolder {

        private TextView name , type;
        private CircleImageView userImage , online;

        RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.single_name);
            type = itemView.findViewById(R.id.single_status);
            userImage = itemView.findViewById(R.id.users_single_image);
            online = itemView.findViewById(R.id.online);

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        assert requestsAdapter != null;
        requestsAdapter.stopListening();
    }
}