package com.facecoders.facecode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facecoders.facecode.activities.MenuActivity;
import com.facecoders.facecode.tflite.ClassifierHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static int IMAGE_SIZE = 48;
    public static String MODEL = "FLOAT";
    public static String DEVICE = "CPU";
    public static int NUMBER_OF_THREADS = 4;

    public static String MODEL_PATH = "FLOAT";
    public static String LABELS_PATH = "CPU";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (requestMultiplePermissions()) {
            ClassifierHandler.initialize(this, this, MODEL, DEVICE, NUMBER_OF_THREADS, IMAGE_SIZE);
            Intent k = new Intent(this, MenuActivity.class);
            startActivity(k);
        }
    }

    private boolean requestMultiplePermissions() {

        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String cameraPermission = Manifest.permission.CAMERA;

        int hasStoragePermission = ActivityCompat.checkSelfPermission(this, storagePermission);
        int hasCameraPermission = ActivityCompat.checkSelfPermission(this, cameraPermission);

        List<String> permissions = new ArrayList<>();
        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(storagePermission);
        }

        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(cameraPermission);
        }

        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, 108);
        }

        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED || hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }
}
