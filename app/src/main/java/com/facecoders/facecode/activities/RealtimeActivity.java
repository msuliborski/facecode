package com.facecoders.facecode.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facecoders.facecode.R;
import com.facecoders.facecode.camera.CameraHandler;
import com.facecoders.facecode.camera.CameraPreview;
import com.facecoders.facecode.tflite.Classifier.Recognition;
import com.facecoders.facecode.tflite.ClassifierHandler;

import java.util.List;


public class RealtimeActivity extends AppCompatActivity {

    private Camera camera;

    private int imageSize = 48;

    FrameLayout cameraPreviewFrameLayout;

    ImageView toAnalyzeImageView;

    TextView emotion1TextView;
    TextView emotion2TextView;
    TextView emotion3TextView;
    ProgressBar emotion1ProgressBar;
    ProgressBar emotion2ProgressBar;
    ProgressBar emotion3ProgressBar;


    ImageButton changeCameraImageButton;

    Bitmap croppedBitmap;
    Bitmap bitmapToAnalyze;
    Bitmap bitmapToDisplay;


    android.os.Handler customHandler = new android.os.Handler();
    android.os.Handler customHandler2 = new android.os.Handler();

    private boolean cameraFacingFront = false;
    private boolean isCameraBusy = false;
    private boolean showLandmarks = false;
    private boolean detectFace = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreviewFrameLayout = findViewById(R.id.cameraPreviewFrameLayout);

        toAnalyzeImageView = findViewById(R.id.viewImageView);

        emotion1TextView = findViewById(R.id.emotion1TextView);
        emotion2TextView = findViewById(R.id.emotion2TextView);
        emotion3TextView = findViewById(R.id.emotion3TextView);
        emotion1ProgressBar = findViewById(R.id.emotion1ProgressBar);
        emotion2ProgressBar = findViewById(R.id.emotion2ProgressBar);
        emotion3ProgressBar = findViewById(R.id.emotion3ProgressBar);

        changeCameraImageButton = findViewById(R.id.changeCameraImageButton);
        changeCameraImageButton.setOnClickListener(v -> {

        });

        camera = CameraHandler.getCameraInstance(cameraFacingFront);

        changeCameraImageButton.setOnClickListener(v -> {
            customHandler.removeCallbacks(updateBitmap);

            cameraFacingFront = !cameraFacingFront;

            camera = CameraHandler.getCameraInstance(cameraFacingFront);
            CameraHandler.setParameters();
            cameraPreviewFrameLayout.addView(new CameraPreview(this, camera));

            customHandler.postDelayed(updateBitmap, 500);
        });

        customHandler.postDelayed(updateBitmap, 500);
    }

    private Runnable updateBitmap = new Runnable() {
        public void run() {
            if (!isCameraBusy) {
                isCameraBusy = true;
                camera.takePicture(null, null, mPicture);
            }
//            customHandler.postDelayed(this, 1/60);
            customHandler.postDelayed(this, 500);
        }
    };


    private Camera.PictureCallback mPicture = (bytes, camera) -> {
        isCameraBusy = true;

        System.out.println("mam foto");
        Bitmap grayscaleBitmap = ClassifierHandler.getGrayscaleBitmap(
                ClassifierHandler.getCroppedBitmap(
                        ClassifierHandler.rotateBitmap(
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.length), cameraFacingFront)));

        Bitmap faceDetectedBitmap = grayscaleBitmap;
        if (detectFace)
            faceDetectedBitmap = ClassifierHandler.getFaceBitmap(grayscaleBitmap, showLandmarks);

        bitmapToAnalyze = ClassifierHandler.getScaledBitmap(faceDetectedBitmap, imageSize);

        toAnalyzeImageView.setImageBitmap(bitmapToAnalyze);

        analyzeBitmap();

        isCameraBusy = false;
    };


    private void analyzeBitmap() {
        List<Recognition> predictions = ClassifierHandler.analyzeBitmap(bitmapToAnalyze);
        for (int i = 0; i < predictions.size(); i++) {
            switch (i) {
                case 0:
                    emotion1TextView.setText(predictions.get(i).getTitle() + "(" + String.format("%.2f", predictions.get(i).getConfidence()) + ")");
                    emotion1ProgressBar.setProgress((int) (predictions.get(i).getConfidence() * 100));
                    break;
                case 1:
                    emotion2TextView.setText(predictions.get(i).getTitle() + "(" + String.format("%.2f", predictions.get(i).getConfidence()) + ")");
                    emotion2ProgressBar.setProgress((int) (predictions.get(i).getConfidence() * 100));
                    break;
                default:
                    emotion3TextView.setText(predictions.get(i).getTitle() + "(" + String.format("%.2f", predictions.get(i).getConfidence()) + ")");
                    emotion3ProgressBar.setProgress((int) (predictions.get(i).getConfidence() * 100));
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        customHandler.removeCallbacks(updateBitmap);
        cameraPreviewFrameLayout.removeAllViews();
        isCameraBusy = true;
        camera.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = CameraHandler.getCameraInstance(cameraFacingFront);
        CameraHandler.setParameters();
        cameraPreviewFrameLayout.addView(new CameraPreview(this, camera));
        isCameraBusy = false;
        emotion1TextView.setText("Detecting...");
        emotion2TextView.setText("Detecting...");
        emotion3TextView.setText("Detecting...");
        emotion1ProgressBar.setProgress(100);
        emotion2ProgressBar.setProgress(100);
        emotion3ProgressBar.setProgress(100);

        customHandler.postDelayed(updateBitmap, 500);
    }

}
