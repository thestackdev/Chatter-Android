package com.firebase.chatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    // profile user details
    private String profile_user_id;
    private String userName , userThumbnail , userStatus , userImage;

    private Button btn1, btn2, btn3;
    private TextView profile_name, profile_status;
    private DatabaseReference friends_database , frq_database , rootDatabase , chatRef;
    private String current_uid;
    private String current_state;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        current_state = "not_friends";


        Toolbar toolbar = findViewById(R.id.profile_bar);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        profile_name = findViewById(R.id.profile_name);
        profile_status = findViewById(R.id.profile_status);
        profileImage = findViewById(R.id.profile_imageView);
        profile_user_id = getIntent().getStringExtra("profile_user_id");

        DatabaseReference profileUserData = FirebaseDatabase.getInstance().getReference().child("Users").child(profile_user_id);
        profileUserData.keepSynced(true);

        rootDatabase = FirebaseDatabase.getInstance().getReference();
        rootDatabase.keepSynced(true);

        frq_database = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        frq_database.keepSynced(true);

        current_uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        friends_database = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_uid);
        friends_database.keepSynced(true);

        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat");

        TextView title = findViewById(R.id.title_toolbar);
        title.setText("Profile");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

     //   btn1.setVisibility(View.INVISIBLE);
    //    btn2.setVisibility(View.INVISIBLE);
    //    btn3.setVisibility(View.INVISIBLE);

    //    btn1.setEnabled(false);
    //    btn2.setEnabled(false);
    //    btn3.setEnabled(false);


        profileUserData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                profile_name.setText(userName);

                userStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                profile_status.setText(userStatus);

                userThumbnail = Objects.requireNonNull(dataSnapshot.child("thumbnail").getValue()).toString();

                userImage = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                if (!userThumbnail.equals("default")) {
                    Picasso.get().load(userThumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(profileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(userThumbnail).placeholder(R.drawable.avatar).into(profileImage);

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        frq_database.child(current_uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(profile_user_id)) {
                    String req_type = dataSnapshot.child(profile_user_id).child("req_type").getValue().toString();
                    if (req_type.equals("received")) {
                        current_state = "req_received";
                        btn2.setText("Accept Friend Request");
                        btn2.setVisibility(View.VISIBLE);
                        btn2.setEnabled(true);
                        btn3.setText("Decline Friend Request");
                        btn3.setVisibility(View.VISIBLE);
                        btn3.setEnabled(true);
                        btn1.setVisibility(View.INVISIBLE);
                        btn1.setEnabled(false);

                    } else if (req_type.equals("sent")) {
                        current_state = "req_sent";
                        Log.i("TAG", "onCreate: 2" + current_state);
                        btn2.setVisibility(View.INVISIBLE);
                        btn2.setEnabled(false);
                        btn3.setVisibility(View.INVISIBLE);
                        btn3.setEnabled(false);
                        btn1.setText("Cancel Friend Request");
                        btn1.setVisibility(View.VISIBLE);
                        btn1.setEnabled(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        friends_database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(profile_user_id)) {
                    current_state = "friends";
                    btn2.setVisibility(View.VISIBLE);
                    btn2.setText("UnFRIEND");
                    btn3.setEnabled(true);
                    btn3.setVisibility(View.VISIBLE);
                    btn3.setText("Message");
                    btn2.setEnabled(true);
                    btn1.setVisibility(View.INVISIBLE);
                    btn1.setEnabled(false);
                } else if(current_state.equals("not_friends")){
                    btn2.setVisibility(View.INVISIBLE);
                    btn3.setEnabled(false);
                    btn3.setVisibility(View.INVISIBLE);
                    btn2.setEnabled(false);
                    btn1.setText("Send Friend Request");
                    btn1.setVisibility(View.VISIBLE);
                    btn1.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn1.setEnabled(false);
                if (current_state.equals("not_friends")) {
                    Map request_map = new HashMap();
                    request_map.put("Friend_req/" + current_uid + "/" + profile_user_id + "/req_type", "sent");
                    request_map.put("Friend_req/" + profile_user_id + "/" + current_uid + "/req_type", "received");
                    rootDatabase.updateChildren(request_map, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                current_state = "req_sent";
                                btn1.setText("Cancel friend Request");
                            }
                            btn1.setEnabled(true);
                        }
                    });
                } else if (current_state.equals("req_sent")) {
                    frq_database.child(current_uid).child(profile_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                frq_database.child(profile_user_id).child(current_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        current_state = "not_friends";
                                        btn1.setText("Send Friend Request");
                                        btn1.setEnabled(true);
                                    }
                                });

                            }
                        }
                    });
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_state.equals("req_received")) {
                    friends_database.child(current_uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final String date = DateFormat.getDateTimeInstance().format(new Date());
                            Map friendsMap = new HashMap();
                            friendsMap.put("Friends/"+current_uid+"/"+profile_user_id+"/date",date);
                            friendsMap.put("Friends/"+profile_user_id+"/"+current_uid+"/date",date);

                            friendsMap.put("Friend_req/"+current_uid+"/"+profile_user_id,null);
                            friendsMap.put("Friend_req/"+profile_user_id+"/"+current_uid,null);

                            rootDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if(databaseError == null) {
                                        current_state = "friends";
                                        btn2.setVisibility(View.VISIBLE);
                                        btn2.setText("UNFRIEND");
                                        btn3.setEnabled(true);
                                        btn3.setVisibility(View.VISIBLE);
                                        btn3.setText("Message");
                                        btn2.setEnabled(true);
                                        btn1.setVisibility(View.INVISIBLE);
                                        btn1.setEnabled(false);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else if (current_state.equals("friends")) {
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/"+current_uid+"/"+profile_user_id, null);
                    friendsMap.put("Friends/"+profile_user_id+"/"+current_uid, null);

                    rootDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null) {
                                current_state = "not_friends";
                                btn2.setVisibility(View.INVISIBLE);
                                btn3.setEnabled(false);
                                btn3.setVisibility(View.INVISIBLE);
                                btn2.setEnabled(false);
                                btn1.setText("Send Friend Request");
                                btn1.setVisibility(View.VISIBLE);
                                btn1.setEnabled(true);
                            }
                        }
                    });

                }

            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_state.equals("friends")) {

                    chatRef.child(current_uid).addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             if (!dataSnapshot.hasChild(profile_user_id)) {
                                 Map addChatMap = new HashMap();

                                 addChatMap.put("seen", false);
                                 addChatMap.put("timeStamp", ServerValue.TIMESTAMP);
                                 addChatMap.put("lastSeenMsg", "null");
                                 addChatMap.put("messageNode" , current_uid+profile_user_id);

                                 Map chatUserMap = new HashMap();
                                 chatUserMap.put("Chat/" + current_uid + "/" + profile_user_id, addChatMap);
                                 chatUserMap.put("Chat/" + profile_user_id + "/" + current_uid, addChatMap);

                                 rootDatabase.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                     @Override
                                     public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                         rootDatabase.child("messages").child(current_uid+profile_user_id).child("lastMsg").setValue("null");
                                     }
                                 });
                             }
                         }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    rootDatabase.child("Chat").child(current_uid).child(profile_user_id).child("messageNode")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String messageNode = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                                    Intent intent = new Intent(ProfileActivity.this , MessageActivity.class);
                                    intent.putExtra("profile_user_id",profile_user_id);
                                    intent.putExtra("userName",userName);
                                    intent.putExtra("thumbnail" , userThumbnail);
                                    intent.putExtra("image" , userImage);
                                    intent.putExtra("messageNode" , messageNode);
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                } else {
                    friends_database.child(current_uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map friendsMap = new HashMap();

                            friendsMap.put("Friend_req/"+current_uid+"/"+profile_user_id,null);
                            friendsMap.put("Friend_req/"+profile_user_id+"/"+current_uid,null);

                            rootDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if(databaseError == null) {
                                        current_state = "not_friends";
                                        btn2.setVisibility(View.INVISIBLE);
                                        btn3.setEnabled(false);
                                        btn3.setVisibility(View.INVISIBLE);
                                        btn2.setEnabled(false);
                                        btn1.setText("Send Friend Request");
                                        btn1.setVisibility(View.VISIBLE);
                                        btn1.setEnabled(true);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }
}