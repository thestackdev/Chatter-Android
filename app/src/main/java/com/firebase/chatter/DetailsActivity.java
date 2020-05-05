package com.firebase.chatter;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textview.MaterialTextView;

public class DetailsActivity extends AppCompatActivity {
    private MaterialTextView sentTime , deliveredTime , seenTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        sentTime = findViewById(R.id.details_sent_time);
        deliveredTime = findViewById(R.id.details_delivered_time);
        seenTime = findViewById(R.id.details_seen_time);


        String details = getIntent().getStringExtra("details");

        assert details != null;
        String[] split = details.split("," , 3);

        sentTime.setText(split[0]);
        deliveredTime.setText(split[1]);
        seenTime.setText(split[2]);

    }
}
