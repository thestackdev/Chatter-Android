package com.firebase.chatter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.bumptech.glide.Glide;
import java.util.Objects;


public class FullScreenImageView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_view);

        Toolbar toolBar = findViewById(R.id.fullScreenBar);
        setSupportActionBar(toolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile Image");

        ImageView imageView = findViewById(R.id.layout_imageView);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getData() != null) {

                Uri imageUri = intent.getData();
                if (imageUri != null && imageView != null) {
                    Glide.with(this).load(imageUri).into(imageView);
                }
            }
        } else {
            Picasso.get().load(R.drawable.avatar).into(imageView);
        }

    }
}