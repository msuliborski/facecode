package com.facecoders.facecode.activities;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facecoders.facecode.R;
import com.facecoders.facecode.camera.Camera2Fragment;

import java.nio.ByteBuffer;


public class PhotoActivity extends AppCompatActivity {


    boolean cameraFacingFront = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        Camera2Fragment camera2BasicFragment = Camera2Fragment.newInstance(cameraFacingFront, false);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.cameraPreviewFrameLayout, camera2BasicFragment)
                    .commit();
        }


        Intent intent = new Intent(this, PhotoResultActivity.class);
        camera2BasicFragment.mOnImageAvailableListener = reader -> {
            Image mImage = reader.acquireNextImage();
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            mImage.close();

            intent.putExtra("bytes", bytes);
            intent.putExtra("cameraFacingFront", cameraFacingFront);
            startActivity(intent);
        };

        findViewById(R.id.pickFromGalleryImageButton).setOnClickListener(v -> {
        });

        findViewById(R.id.takePhotoImageButton).setOnClickListener(v -> {
            camera2BasicFragment.takePicture();
        });

        findViewById(R.id.switchCameraImageButton).setOnClickListener(v -> {
            cameraFacingFront = !cameraFacingFront;
            camera2BasicFragment.switchCamera(cameraFacingFront, false);
        });
    }
}
