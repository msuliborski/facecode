package com.facecoders.facecode.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.facecoders.facecode.R;
import com.facecoders.facecode.camera.CameraHandler;
import com.facecoders.facecode.camera.CameraPreview;
import com.facecoders.facecode.tflite.Classifier;
import com.facecoders.facecode.tflite.ClassifierHandler;

import java.util.List;


public class PhotoActivity extends AppCompatActivity {

    private Camera camera;

    FrameLayout cameraPreviewFrameLayout;
    ImageButton takePhotoImageButton;
    boolean cameraFacingFront = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        cameraPreviewFrameLayout = findViewById(R.id.cameraPreviewFrameLayout);
        takePhotoImageButton = findViewById(R.id.takePhotoImageButton);
        takePhotoImageButton.setOnClickListener(v -> {
            camera.takePicture(null, null, mPicture);
        });

//        camera = CameraHandler.getCameraInstance(cameraFacingFront);
//        CameraHandler.setParameters();
//        v.addView(new CameraPreview(this, camera));
//        cameraPreview.
    }

    private Camera.PictureCallback mPicture = (bytes, camera) -> {
        cameraPreviewFrameLayout.removeAllViews();
        camera.stopPreview();
        Intent intent = new Intent(this, PhotoResultActivity.class);
        intent.putExtra("bytes", bytes);
        startActivity(intent);
    };

    @Override
    protected void onPause() {
        super.onPause();
        cameraPreviewFrameLayout.removeAllViews();
        camera.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = CameraHandler.getCameraInstance(cameraFacingFront);
        CameraHandler.setParameters();
        cameraPreviewFrameLayout.addView(new CameraPreview(this, camera));
    }
}
