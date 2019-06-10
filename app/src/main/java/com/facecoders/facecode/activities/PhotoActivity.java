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
    Bitmap takenBitmap;
    boolean cameraFacingFront = true;
    boolean detectFace = true;
    boolean showLandmarks = false;
    int imageSize = 48;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        camera = CameraHandler.getCameraInstance(cameraFacingFront);
        cameraPreviewFrameLayout = findViewById(R.id.cameraPreviewFrameLayout);
        takePhotoImageButton = findViewById(R.id.takePhotoImageButton);
        takePhotoImageButton.setOnClickListener(v -> {
            camera.takePicture(null, null, mPicture);
        });

        camera = CameraHandler.getCameraInstance(cameraFacingFront);
        camera.setDisplayOrientation(90);
        Camera.Parameters cameraParameters = camera.getParameters();
        for (Camera.Size size : cameraParameters.getSupportedPictureSizes()) {
            if (1080 <= size.width && size.height <= 1920) {
                cameraParameters.setPictureSize(size.width, size.height);
                break;
            }
        }
        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //It is better to use defined constraints as opposed to String, thanks to AbdelHady
        camera.setParameters(cameraParameters);
        cameraPreviewFrameLayout.addView(new CameraPreview(this, camera));
    }

    private Camera.PictureCallback mPicture = (data, camera) -> {
        //show animation
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Bitmap rotatedBitmap = ClassifierHandler.rotateBitmap(decodedBitmap, cameraFacingFront);

        Bitmap croppedBitmap = ClassifierHandler.getCroppedBitmap(rotatedBitmap);
        Bitmap grayscaleBitmap = ClassifierHandler.getGrayscaleBitmap(croppedBitmap);

        Bitmap faceDetectedBitmap;
        if (detectFace) faceDetectedBitmap = ClassifierHandler.getFaceBitmap(this, grayscaleBitmap, showLandmarks);
        else faceDetectedBitmap = grayscaleBitmap;

        takenBitmap = ClassifierHandler.getScaledBitmap(faceDetectedBitmap, imageSize);

        Intent intent = new Intent(this, PhotoResultActivity.class);
        intent.putExtra("takenBitmap", takenBitmap);
        startActivity(intent);

//        Intent intent = new Intent(this, PhotoResultActivity.class);
//        intent.putExtra("takenBitmap", faceDetectedBitmap);
//        startActivity(intent);
    };


}
