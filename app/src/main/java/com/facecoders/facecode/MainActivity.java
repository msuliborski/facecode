package com.facecoders.facecode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView faceImageView;
    Button detectFaceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        faceImageView = findViewById(R.id.faceImageView);
        detectFaceButton = findViewById(R.id.detectFaceButton);
    }
}
