package com.firebase.chatter.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.chatter.R;
import com.firebase.chatter.helper.AppAccents;
import com.squareup.picasso.Picasso;

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

        AppAccents appAccents = new AppAccents(this);
        appAccents.init();

        toolBar.setBackgroundColor(Color.parseColor(appAccents.getAccentColor()));
        toolBar.setTitleTextColor(Color.parseColor(appAccents.getTextColor()));

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(appAccents.getAccentColor()));

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