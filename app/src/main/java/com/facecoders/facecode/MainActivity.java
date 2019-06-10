package com.facecoders.facecode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent k = new Intent(this, com.facecoders.facecode.activities.CameraActivity.class);
        startActivity(k);
    }
}
