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
import com.facecoders.facecode.tflite.Classifier;
import com.facecoders.facecode.tflite.ClassifierHandler;

import java.util.List;


public class CameraActivity extends AppCompatActivity {

    private Camera camera;

    private int imageSize = 48;

    FrameLayout cameraPreviewFrameLayout;

    TextView outputTextView;
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

        outputTextView = findViewById(R.id.outputTextView);
        toAnalyzeImageView = findViewById(R.id.viewImageView);

        emotion1TextView = findViewById(R.id.emotion1TextView);
        emotion2TextView = findViewById(R.id.emotion2TextView);
        emotion3TextView = findViewById(R.id.emotion3TextView);
        emotion1ProgressBar = findViewById(R.id.emotion1ProgressBar);
        emotion2ProgressBar = findViewById(R.id.emotion2ProgressBar);
        emotion3ProgressBar = findViewById(R.id.emotion3ProgressBar);

        changeCameraImageButton = findViewById(R.id.changeCameraImageButton);
        changeCameraImageButton.setOnClickListener(v -> {

            customHandler.removeCallbacks(updateBitmap);

            cameraFacingFront = !cameraFacingFront;
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
            customHandler.postDelayed(updateBitmap, 5000);
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
        customHandler.postDelayed(updateBitmap, 100);





    }

    private Runnable updateBitmap = new Runnable() {
        public void run() {
            if (!isCameraBusy) {
                isCameraBusy = true;
                camera.takePicture(null, null, mPicture);
            }
//            customHandler.postDelayed(this, 1/60);
            customHandler.postDelayed(this, 800);
        }
    };
//

    private Camera.PictureCallback mPicture = (data, camera) -> {
        isCameraBusy = true;
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Bitmap rotatedBitmap = ClassifierHandler.rotateBitmap(decodedBitmap, cameraFacingFront);

        Bitmap croppedBitmap = ClassifierHandler.getCroppedBitmap(rotatedBitmap);
        Bitmap grayscaleBitmap = ClassifierHandler.getGrayscaleBitmap(croppedBitmap);

        Bitmap faceDetectedBitmap;
        if (detectFace) faceDetectedBitmap = ClassifierHandler.getFaceBitmap(this, grayscaleBitmap, showLandmarks);
        else faceDetectedBitmap = grayscaleBitmap;

        bitmapToAnalyze = ClassifierHandler.getScaledBitmap(faceDetectedBitmap, imageSize);

        toAnalyzeImageView.setImageBitmap(bitmapToAnalyze);

        analyzeBitmap();

        isCameraBusy = false;
    };


    private void analyzeBitmap() {
        ClassifierHandler.initialize(this, "FLOAT", "CPU", 4, imageSize);
        List<Classifier.Recognition> predictions = ClassifierHandler.analyzeBitmap(bitmapToAnalyze);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < predictions.size(); i++) {
            switch (i) {
                case 0:
                    emotion1TextView.setText(predictions.get(i).getTitle());
                    emotion1ProgressBar.setProgress((int) (predictions.get(i).getConfidence() * 100));
                    break;
                case 1:
                    emotion2TextView.setText(predictions.get(i).getTitle());
                    emotion2ProgressBar.setProgress((int) (predictions.get(i).getConfidence() * 100));
                    break;
                default:
                    emotion3TextView.setText(predictions.get(i).getTitle());
                    emotion3ProgressBar.setProgress((int) (predictions.get(i).getConfidence() * 100));
                    break;
            }
            stringBuilder.append(predictions.get(i).getTitle()).append("(").append(String.format("%.2f", predictions.get(i).getConfidence())).append(")\n");
        }

        String finalCa = stringBuilder.toString();
        runOnUiThread(() ->
                outputTextView.setText(finalCa));
    }


}
