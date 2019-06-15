package com.facecoders.facecode.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facecoders.facecode.R;
import com.facecoders.facecode.tflite.ClassifierHandler;
import com.facecoders.facecode.tflite.Recognition;

import java.util.List;


public class PhotoResultActivity extends AppCompatActivity {

    FrameLayout cameraPreviewFrameLayout;

    ImageView  viewImageView;
    ImageView  analyzedImageView;

    TextView emotion1TextView;
    TextView emotion2TextView;
    TextView emotion3TextView;
    ProgressBar emotion1ProgressBar;
    ProgressBar emotion2ProgressBar;
    ProgressBar emotion3ProgressBar;

    Bitmap takenBitmap;
    boolean cameraFacingFront = true;
    boolean detectFace = true;
    boolean showLandmarks = false;
    int imageSize = 48;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_result);


        cameraPreviewFrameLayout = findViewById(R.id.cameraPreviewFrameLayout);

        viewImageView = findViewById(R.id.viewImageView);
        analyzedImageView = findViewById(R.id.analyzedImageView);

        emotion1TextView = findViewById(R.id.emotion1TextView);
        emotion2TextView = findViewById(R.id.emotion2TextView);
        emotion3TextView = findViewById(R.id.emotion3TextView);
        emotion1ProgressBar = findViewById(R.id.emotion1ProgressBar);
        emotion2ProgressBar = findViewById(R.id.emotion2ProgressBar);
        emotion3ProgressBar = findViewById(R.id.emotion3ProgressBar);

        Intent intent = this.getIntent();
        byte[] bytes = intent.getByteArrayExtra("bytes");
        cameraFacingFront = intent.getBooleanExtra("cameraFacingFront", true);


        Bitmap rotatedBitmap = ClassifierHandler.rotateBitmap(
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length), cameraFacingFront);
        viewImageView.setImageBitmap(rotatedBitmap);

        Bitmap grayscaleBitmap = ClassifierHandler.getGrayscaleBitmap(
                ClassifierHandler.getCroppedBitmap(rotatedBitmap));

        Bitmap faceDetectedBitmap;
        analyzedImageView.setImageBitmap(grayscaleBitmap);
        if (detectFace) faceDetectedBitmap = ClassifierHandler.getFaceBitmap(grayscaleBitmap, showLandmarks);
        else faceDetectedBitmap = grayscaleBitmap;

        Bitmap toAnalyzeBitmap = ClassifierHandler.getScaledBitmap(faceDetectedBitmap, imageSize);

        analyzedImageView.setImageBitmap(toAnalyzeBitmap);

        analyzeBitmap(toAnalyzeBitmap);
    }



    private void analyzeBitmap(Bitmap bitmap) {
        List<Recognition> predictions = ClassifierHandler.analyzeBitmap(bitmap);

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
}
