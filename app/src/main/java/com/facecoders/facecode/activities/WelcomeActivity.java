package com.facecoders.facecode.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageButton;

import com.facecoders.facecode.R;

public class WelcomeActivity extends AppCompatActivity {

    ImageButton candidImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        candidImageButton = findViewById(R.id.candidImageButton);
        candidImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed(){
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}
