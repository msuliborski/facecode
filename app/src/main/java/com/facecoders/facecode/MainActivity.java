package com.facecoders.facecode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facecoders.facecode.activities.MenuActivity;
import com.facecoders.facecode.activities.WelcomeActivity;
import com.facecoders.facecode.tflite.Classifier;
import com.facecoders.facecode.tflite.Classifier.Model;
import com.facecoders.facecode.tflite.Classifier.Device;
import com.facecoders.facecode.tflite.ClassifierHandler;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int imageSize = 48;
//        int imageSize = 224;

        Model modelVariant = Model.FLOAT;
//        Model modelVariant = Model.QUANTIZED;

        Device deviceVariant = Device.CPU;
//        Device deviceVariant = Device.GPU;

        int numberOfThreads = 4;
//        int numberOfThreads = 8;

//        String modelPath = "model-20-10.tflite";
//        String modelPath = "model-50-20.tflite";
        String modelPath = "model-150-60.tflite";
//        String modelPath = "model-180-50.tflite";


        String labelsPath = "labels.txt";

        ClassifierHandler.initialize(this, this, modelVariant, deviceVariant, numberOfThreads, imageSize, modelPath, labelsPath);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        } else {
            startActivity(new Intent(this, WelcomeActivity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this, WelcomeActivity.class));
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
            }
        }
    }
}
