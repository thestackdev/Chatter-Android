package com.firebase.chatter;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textview.MaterialTextView;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        MaterialTextView sentTime = findViewById(R.id.details_sent_time);
        MaterialTextView deliveredTime = findViewById(R.id.details_delivered_time);
        MaterialTextView seenTime = findViewById(R.id.details_seen_time);
        MaterialTextView message = findViewById(R.id.details_message);


        String details = getIntent().getStringExtra("details");
        String getMessage = getIntent().getStringExtra("message");

        message.setText(getMessage);

        assert details != null;
        String[] split = details.split("," , 3);

        sentTime.setText(split[0]);
        deliveredTime.setText(split[1]);
        seenTime.setText(split[2]);

    }
}
