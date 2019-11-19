package com.facecoders.facecode.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.facecoders.facecode.R;

public class MenuActivity extends AppCompatActivity {

    ImageButton photoImageButton;
    ImageButton cameraImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        photoImageButton = findViewById(R.id.photoImageButton);
        cameraImageButton = findViewById(R.id.realtimeImageText);

        photoImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PhotoActivity.class);
            startActivity(intent);
        });

        cameraImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RealtimeActivity.class);
            startActivity(intent);
        });

    }
}
