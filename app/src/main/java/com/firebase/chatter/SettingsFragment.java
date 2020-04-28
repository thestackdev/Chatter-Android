package com.firebase.chatter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsFragment extends Fragment {

    private TextView user_name, user_status, edit_info, done_editing;
    private CircleImageView user_image;
    private LinearLayout logout;
    private ImageView change_name, change_status, change_email, change_password;
    private DatabaseReference userData;
    private Uri imageUri;
    private String image;
    private ImageView changeImage;
    private static final int GALLERY_PICK = 1;
    private ProgressDialog progressDialog;
    private StorageReference mStorageRef;
    private Task mUploadTask;
    private String thumbUrl, imageUrl;
    private String current_uid;
    private View view;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings,container,false);
        setUpUiViews();

        edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                change_name.setVisibility(View.VISIBLE);
                change_status.setVisibility(View.VISIBLE);
                change_email.setVisibility(View.VISIBLE);
                change_password.setVisibility(View.VISIBLE);
                edit_info.setVisibility(View.GONE);
                done_editing.setVisibility(View.VISIBLE);

                change_name.animate().translationY(0);
                change_status.animate().translationY(0);
                change_email.animate().translationY(0);
                change_password.animate().translationY(0);
                edit_info.animate().translationY(0);
                done_editing.animate().translationY(0);

                done_editing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        change_name.setVisibility(View.GONE);
                        change_status.setVisibility(View.GONE);
                        change_email.setVisibility(View.GONE);
                        change_password.setVisibility(View.GONE);
                        done_editing.setVisibility(View.GONE);
                        edit_info.setVisibility(View.VISIBLE);

                        change_name.animate().translationY(1);
                        change_status.animate().translationY(1);
                        change_email.animate().translationY(1);
                        change_password.animate().translationY(1);
                        edit_info.animate().translationY(1);
                        done_editing.animate().translationY(1);

                    }
                });

            }
        });

        userData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                final String thumbnail = Objects.requireNonNull(dataSnapshot.child("thumbnail").getValue()).toString();

                user_name.setText(name);
                user_status.setText(status);

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUri = Uri.parse(image);
                Intent intent = new Intent(getContext() , FullScreenImageView.class);
                intent.setData(imageUri);
                startActivity(intent);
            }
        });

        change_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameDialog usernameDialog = new usernameDialog();
                assert getFragmentManager() != null;
                usernameDialog.show(getFragmentManager(),"Change Name");
            }
        });

        change_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Change_Status change_status = new Change_Status();
                assert getFragmentManager() != null;
                change_status.show(getFragmentManager(),"Change Status");
            }
        });

        change_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Change_email change_email = new Change_email();
                assert getFragmentManager() != null;
                change_email.show(getFragmentManager(), "Change Email ID");
            }
        });

        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Change_Password change_password = new Change_Password();
                assert getFragmentManager() != null;
                change_password.show(getFragmentManager(),"Change Password");
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setTitle("Logout").setCancelable(false).setMessage("Are You Sure");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getContext(),LoginActivity.class);
                            startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }

        });


        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGalleryIntent();
            }
        });

        return view;
    }

    private void callGalleryIntent() {

        Intent intent = CropImage.activity()
                .setAspectRatio(1, 1)
                .setMinCropWindowSize(500, 500)
                .getIntent(Objects.requireNonNull(getContext()));
        startActivityForResult(intent , CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && CropImage.getActivityResult(data) != null) {

            Log.i("TAG", "onActivityResult: "+ "entered");
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result.getUri() != null) {

                final Uri resultUri = result.getUri();
                final File thumbPath = new File(Objects.requireNonNull(resultUri.getPath()));

                Bitmap thumbnail_map = new Compressor(Objects.requireNonNull(getContext()))
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumbPath);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbnail_map.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                final StorageReference storageReference = mStorageRef.child("profile_images").child(current_uid + ".jpg");
                final StorageReference thumb_Ref = mStorageRef.child("thumbnails").child(current_uid + ".jpg");

                mUploadTask = storageReference.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if (Objects.requireNonNull(taskSnapshot.getMetadata()).getReference() != null) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            imageUrl = uri.toString();
                                        }
                                    });

                                    mUploadTask = thumb_Ref.putBytes(thumb_byte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            if (Objects.requireNonNull(taskSnapshot.getMetadata()).getReference() != null) {
                                                thumb_Ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        thumbUrl = uri.toString();

                                                        Map imageMap = new HashMap<>();
                                                        imageMap.put("image" , imageUrl);
                                                        imageMap.put("thumbnail" , thumbUrl);

                                                        userData.updateChildren(imageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                progressDialog.dismiss();
                                                            }
                                                        });
                                                    }
                                                });


                                            }
                                        }
                                    });

                                } else {
                                    progressDialog.dismiss();
                                }
                            }
                        });
            }
        }
    }

    private void setUpUiViews() {

        current_uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        userData = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        user_name = view.findViewById(R.id.settings_name);
        user_image = view.findViewById(R.id.settings_circleImageView);
        user_status = view.findViewById(R.id.settings_status);
        change_name = view.findViewById(R.id.change_name);
        change_status = view.findViewById(R.id.change_status);
        change_email = view.findViewById(R.id.change_email);
        change_password = view.findViewById(R.id.change_pass);
        changeImage = view.findViewById(R.id.change_image);
        progressDialog = new ProgressDialog(view.getContext());
        edit_info = view.findViewById(R.id.edit_info);
        done_editing = view.findViewById(R.id.done_editing);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        logout = view.findViewById(R.id.layout_logout);

    }
}
