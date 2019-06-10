package com.facecoders.facecode.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

import com.facecoders.facecode.R;


public class PhotoActivity extends AppCompatActivity {


    ImageButton takePhotoImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        takePhotoImageButton = findViewById(R.id.takePhotoImageButton);
        takePhotoImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PhotoResultActivity.class);
            startActivity(intent);
        });
    }
}
