package com.firebase.chatter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private String current_user_id;
    private FirebaseRecyclerAdapter<Users , SearchFragment.SearchViewHolder> adapter;


    SearchFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        EditText search = view.findViewById(R.id.et_search);

        current_user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = view.findViewById(R.id.search_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                firebaseSearch(s.toString());
            }
        });

        return view;
    }

    private void firebaseSearch(String search) {

        if (search.length() == 0) {
            recyclerView.setAdapter(null);
            if (adapter!=null)
                adapter.stopListening();
        } else {

            Query query = databaseReference.orderByChild("name").startAt(search).endAt(search + "\uf8ff");
            FirebaseRecyclerOptions<Users> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Users>()
                    .setQuery(query, Users.class).build();

            adapter = new FirebaseRecyclerAdapter<Users, SearchViewHolder>(firebaseRecyclerOptions) {
                @Override
                protected void onBindViewHolder(@NonNull final SearchViewHolder searchViewHolder, final int i, @NonNull final Users users) {
                    final String userId = getRef(i).getKey();
                    assert userId != null;
                    databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String online = Objects.requireNonNull(dataSnapshot.child("online").getValue()).toString();

                            if(!online.equals("true")) {
                                //searchViewHolder.online.setColorFilter(Color.RED);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    searchViewHolder.name.setText(users.getName());
                    searchViewHolder.status.setText(users.getStatus());
                    final String thumbnail = users.getThumbnail();

                    if (!thumbnail.equals("default")) {
                        Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.avatar).into(searchViewHolder.user_image, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(thumbnail).placeholder(R.drawable.avatar).into(searchViewHolder.user_image);

                            }
                        });
                    }

                    searchViewHolder.user_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!thumbnail.equals("default")) {
                                final String image = users.getImage();
                                Uri imageUri = Uri.parse(image);
                                Intent intent = new Intent(v.getContext() , FullScreenImageView.class);
                                intent.setData(imageUri);
                                startActivity(intent);
                            } else {
                                Toast.makeText(v.getContext(), "No Profile Picture", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                    searchViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(userId.equals(current_user_id)) {
                                /*FragmentManager fragmentManager = getFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.main_frame , new SettingsFragment()).commit();*/
                            } else {
                                Intent intent = new Intent(v.getContext(),ProfileActivity.class);
                                intent.putExtra("profile_user_id" , userId);
                                startActivity(intent);
                            }
                        }
                    });

                }

                @NonNull
                @Override
                public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                    return new SearchViewHolder(view);
                }
            };
            recyclerView.setAdapter(adapter);
            adapter.startListening();
        }
    }

    private static class SearchViewHolder extends RecyclerView.ViewHolder {
        private TextView name , status;
        private CircleImageView user_image;
        //private ImageView online;

        SearchViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.users_single_image);
            name = itemView.findViewById(R.id.single_name);
            status = itemView.findViewById(R.id.single_status);
            //online = itemView.findViewById(R.id.online);

        }
    }
}
