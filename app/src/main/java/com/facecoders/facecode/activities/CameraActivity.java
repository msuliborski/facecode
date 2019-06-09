/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facecoders.facecode.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facecoders.facecode.camera2.Camera2BasicFragment;
import com.facecoders.facecode.R;


public class CameraActivity extends AppCompatActivity {


    TextView outputTextView;
    ImageView toAnalyzeImageView;

    TextView emotion1TextView;
    TextView emotion2TextView;
    TextView emotion3TextView;
    ProgressBar emotion1ProgressBar;
    ProgressBar emotion2ProgressBar;
    ProgressBar emotion3ProgressBar;

    ImageButton changeCameraImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        outputTextView = findViewById(R.id.outputTextView);
        toAnalyzeImageView = findViewById(R.id.toAnalyzeImageView);

        emotion1TextView = findViewById(R.id.emotion1TextView);
        emotion2TextView = findViewById(R.id.emotion2TextView);
        emotion3TextView = findViewById(R.id.emotion3TextView);
        emotion1ProgressBar = findViewById(R.id.emotion1ProgressBar);
        emotion2ProgressBar = findViewById(R.id.emotion2ProgressBar);
        emotion3ProgressBar = findViewById(R.id.emotion3ProgressBar);

        changeCameraImageButton = findViewById(R.id.changeCameraImageButton);


        Bitmap croppedBitmap;
        Bitmap bitmapToAnalyze;
        Bitmap bitmapToDisplay;

        Camera2BasicFragment camera2BasicFragment = Camera2BasicFragment.newInstance();
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.cameraPreviewFrameLayout, camera2BasicFragment)
                    .commit();
        }

        changeCameraImageButton.setOnClickListener(v -> {
            camera2BasicFragment.takePicture();
//            bitmapToDisplay = ;
            if (camera2BasicFragment.getCapturedBitmap() != null)
                toAnalyzeImageView.setImageBitmap(camera2BasicFragment.getCapturedBitmap());
        });


//        iv = findViewById(R.id.imageView);
//
//        iv.setOnClickListener(v -> {
//            iv.setImageBitmap(Camera2BasicFragment.getCapturedBitmap());
//        });
    }

}
