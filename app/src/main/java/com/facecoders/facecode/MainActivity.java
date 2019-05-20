package com.facecoders.facecode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facecoders.facecode.tflite.Classifier;
import com.facecoders.facecode.tflite.Classifier.Device;
import com.facecoders.facecode.tflite.Classifier.Model;
import com.facecoders.facecode.tflite.Classifier.Recognition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity{

    private Camera camera;

    private static final int PERMISSIONS_REQUEST = 108;

    private Classifier classifier;

    Bitmap bitmapToAnalyze;

    TextView outputTextView;
    Button button;
    Button button2;
    ImageView img;
    Bitmap croppedBitmap;
    List<Recognition> predictions;


    android.os.Handler customHandler = new android.os.Handler();
    android.os.Handler customHandler2 = new android.os.Handler();


    boolean isCameraBusy = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestMultiplePermissions();

        outputTextView = findViewById(R.id.outputTextView);
        img = findViewById(R.id.img);

        camera = getCameraInstance();
        camera.setDisplayOrientation(90);

        Camera.Parameters params = camera.getParameters();
//*EDIT*//
//It is better to use defined constraints as opposed to String, thanks to AbdelHady
//        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setFocusMode("continuous-picture");
        camera.setParameters(params);

        CameraPreview mPreview = new CameraPreview(this, camera);
        FrameLayout cameraFrameLayout = findViewById(R.id.cameraFrameLayout);
        cameraFrameLayout.addView(mPreview);

        Model model = Model.valueOf("Quantized".toUpperCase());
        Device device = Device.valueOf("CPU");
        int numThreads = 1;




        try {
            classifier = Classifier.create(this, model, device, numThreads);
        } catch (IOException e) {
            e.printStackTrace();
        }


        customHandler.postDelayed(updateBitmap, 500);
        customHandler2.postDelayed(analyzeBitmap, 2000);


    }

    private Runnable updateBitmap = new Runnable() {
        public void run() {
            if(!isCameraBusy) {
                isCameraBusy = true;
                camera.takePicture(null, null, mPicture);
            }
            customHandler.postDelayed(this, 1000);
        }
    };

    private Runnable analyzeBitmap = new Runnable() {
        public void run() {
            predictions = classifier.recognizeImage(bitmapToAnalyze);
            StringBuilder ca = new StringBuilder();
            for (int i = 0; i < predictions.size(); i++){
                ca.append(predictions.get(i).getTitle()).append("(").append(String.format("%.2f", predictions.get(i).getConfidence())).append(")\n");
            }

            String finalCa = ca.toString();
            runOnUiThread(() ->
                    outputTextView.setText(finalCa));
            customHandler.postDelayed(this, 1000);
        }
    };


    private Camera.PictureCallback mPicture = (data, camera) -> {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        bitmapToAnalyze = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), matrix, true);
        bitmapToAnalyze = getScaleBitmap(bitmapToAnalyze, 224);
        img.setImageBitmap(bitmapToAnalyze);
        isCameraBusy = false;
    };

    private static Bitmap getScaleBitmap(Bitmap bitmap, int size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) size) / width;
        float scaleHeight = ((float) size) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }


    private void requestMultiplePermissions() {

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
            ActivityCompat.requestPermissions(this, params, PERMISSIONS_REQUEST);
        }
    }


    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


}
