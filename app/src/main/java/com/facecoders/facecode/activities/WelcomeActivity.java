package com.facecoders.facecode.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageButton;

import com.facecoders.facecode.R;

public class WelcomeActivity extends AppCompatActivity {

    ImageButton candidImageButton;
    Button tempButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        candidImageButton = findViewById(R.id.candidImageButton);
        candidImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });
        tempButton = findViewById(R.id.tempButton);
        tempButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, Camera2Activity.class);
            startActivity(intent);
        });

    }

}
