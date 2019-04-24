package com.facecoders.facecode;

import android.app.AlertDialog;
import android.app.usage.NetworkStats;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.api.gax.paging.Page;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.google.cloud.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView faceImageView;
    TextView analysisOutputTextView;
    Button detectFaceButton;
    Button analyzeFaceButton;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        faceImageView = findViewById(R.id.faceImageView);
        analysisOutputTextView = findViewById(R.id.analysisOutputTextView);
        detectFaceButton = findViewById(R.id.detectFaceButton);
        analyzeFaceButton = findViewById(R.id.analyzeFaceButton);

        detectFaceButton.setOnClickListener(v -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap myBitmap = BitmapFactory.decodeResource(
                    getApplicationContext().getResources(),
                    R.drawable.model,
                    options);

            Paint myRectPaint = new Paint();
            myRectPaint.setStrokeWidth(5);
            myRectPaint.setColor(Color.RED);
            myRectPaint.setStyle(Paint.Style.STROKE);

            Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(tempBitmap);
            tempCanvas.drawBitmap(myBitmap, 0, 0, null);

            FaceDetector faceDetector = new
                    FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                    .build();
            if (!faceDetector.isOperational()) {
                new AlertDialog.Builder(v.getContext()).setMessage("Could not set up the face detector!").show();
                return;
            }


            Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
            SparseArray<Face> faces = faceDetector.detect(frame);


            for (int i = 0; i < faces.size(); i++) {
                Face thisFace = faces.valueAt(i);
                float x1 = thisFace.getPosition().x;
                float y1 = thisFace.getPosition().y;
                float x2 = x1 + thisFace.getWidth();
                float y2 = y1 + thisFace.getHeight();
                tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
            }
            faceImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
        });


        analyzeFaceButton.setOnClickListener(d -> {
            try {
                GoogleCredentials credential =
                        GoogleCredentials.getApplicationDefault();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Instantiates a client
            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

                // The path to the image file to annotate
                String fileName = "./drawable-v24/model.png";

                // Reads the image file into memory
                Path path = Paths.get(fileName);
                byte[] data = Files.readAllBytes(path);
                ByteString imgBytes = ByteString.copyFrom(data);

                // Builds the image annotation request
                List<AnnotateImageRequest> requests = new ArrayList<>();
                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(feat)
                        .setImage(img)
                        .build();
                requests.add(request);

                // Performs label detection on the image file
                BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        System.out.printf("Error: %s\n", res.getError().getMessage());
                        analysisOutputTextView.setText(analysisOutputTextView.getText() + "Error: %s\n" + res.getError().getMessage());
                        return;
                    }

                    for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                        annotation.getAllFields().forEach((k, v) -> {
                            System.out.printf("%s : %s\n", k, v.toString());

                            analysisOutputTextView.setText(analysisOutputTextView.getText() + "%s : %s\n" + k + v.toString());
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
