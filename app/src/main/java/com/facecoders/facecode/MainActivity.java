package com.facecoders.facecode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facecoders.facecode.tflite.Classifier;
import com.facecoders.facecode.tflite.Classifier.Device;
import com.facecoders.facecode.tflite.Classifier.Model;
import com.facecoders.facecode.tflite.Classifier.Recognition;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity{

    private Camera camera;
    private boolean frontFacingCamera = false;

    private static final int PERMISSIONS_REQUEST = 108;

    private Classifier classifier;

    Bitmap bitmapToAnalyze;
    Bitmap bitmapToDisplay;

    TextView outputTextView;
    TextView emotionText1;
    TextView emotionText2;
    TextView emotionText3;
    ProgressBar emotionPercent1;
    ProgressBar emotionPercent2;
    ProgressBar emotionPercent3;

    ImageButton changeCameraButton;

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

        emotionText1 = findViewById(R.id.emotionText1);
        emotionText2 = findViewById(R.id.emotionText2);
        emotionText3 = findViewById(R.id.emotionText3);
        emotionPercent1 = findViewById(R.id.emotionPercent1);
        emotionPercent2 = findViewById(R.id.emotionPercent2);
        emotionPercent3 = findViewById(R.id.emotionPercent3);

        changeCameraButton = findViewById(R.id.changeCameraButton);
        changeCameraButton.setOnClickListener(v -> {
            frontFacingCamera = !frontFacingCamera;
            camera = getCameraInstance(frontFacingCamera);
            camera.setDisplayOrientation(90);
            Camera.Parameters cameraParameters = camera.getParameters();
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //It is better to use defined constraints as opposed to String, thanks to AbdelHady
            cameraParameters.setFocusMode("continuous-picture");
            cameraParameters.setPictureSize(1920, 1080);
            camera.setParameters(cameraParameters);
            CameraPreview cameraPreview = new CameraPreview(this, camera);
            FrameLayout cameraFrameLayout = findViewById(R.id.cameraFrameLayout);
            cameraFrameLayout.addView(cameraPreview);
            cameraFrameLayout.getLayoutParams().height = -1;

            customHandler.removeCallbacks(updateBitmap);
            customHandler.postDelayed(updateBitmap, 1000);
        });

        img = findViewById(R.id.img);

        camera = getCameraInstance(frontFacingCamera);
        camera.setDisplayOrientation(90);

        Camera.Parameters cameraParameters = camera.getParameters();

//        for (Camera.Size size : cameraParameters.getSupportedPictureSizes()) {
//            if (1080 <= size.width && size.width <= 1920) {
//                cameraParameters.setPreviewSize(size.width, size.height);
//                cameraParameters.setPictureSize(size.width, size.height);
//                break;
//            }
//        }
        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //It is better to use defined constraints as opposed to String, thanks to AbdelHady
        cameraParameters.setFocusMode("continuous-picture");
        cameraParameters.setPictureSize(1920, 1080);
        camera.setParameters(cameraParameters);
//        params.setFocusMode("continuous-picture");
//        camera.setParameters(params);
//
//        List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
//        for(Camera.Size c : supportedSizes)
//            Log.wtf("eadadea", c.height + "x"+c.width);

        CameraPreview cameraPreview = new CameraPreview(this, camera);
        FrameLayout cameraFrameLayout = findViewById(R.id.cameraFrameLayout);
        cameraFrameLayout.addView(cameraPreview);
        Log.wtf("camera1", cameraFrameLayout.getLayoutParams().height+"");
        Log.wtf("camera2", cameraFrameLayout.getLayoutParams().width+"");
        Log.wtf("camera3", cameraParameters.getPreviewSize().width+"");
        Log.wtf("camera4", cameraParameters.getPreviewSize().height+"");
        cameraFrameLayout.getLayoutParams().height = -1;
//        cameraFrameLayout.getLayoutParams().height = cameraFrameLayout.getLayoutParams().width*cameraParameters.getPreviewSize().width/cameraParameters.getPreviewSize().height;
//        a.height = 100;
//        cameraFrameLayout.setLayoutParams(a);

//                .LayoutParams(
//                cameraFrameLayout.getWidth(), cameraFrameLayout.getWidth()*cameraParameters.getPreviewSize().width/cameraParameters.getPreviewSize().height));

//
//                      9                                            16
//        cameraParameters.getPreviewSize().width         cameraParameters.getPreviewSize().height
//        height????                                                          width

        Model model = Model.valueOf("Float".toUpperCase());
        Device device = Device.valueOf("CPU");
        int numThreads = 4;


        try {
            classifier = Classifier.create(this, model, device, numThreads);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        customHandler.postDelayed(updateBitmap, 500);
//        customHandler2.postDelayed(analyzeBitmap, 2000);

        customHandler.postDelayed(updateBitmap, 1000);

    }

    private Runnable updateBitmap = new Runnable() {
        public void run() {
//            if (!isCameraBusy) {
//                isCameraBusy = true;
                camera.takePicture(null, null, mPicture);
//            }
            customHandler.postDelayed(this, 500);
//            customHandler.postDelayed(this, 1/60);
        }
    };

//    private Runnable analyzeBitmap = new Runnable() {
//        public void run() {
//            predictions = classifier.recognizeImage(bitmapToAnalyze);
//            StringBuilder ca = new StringBuilder();
//            for (int i = 0; i < predictions.size(); i++){
//                ca.append(predictions.get(i).getTitle()).append("(").append(String.format("%.2f", predictions.get(i).getConfidence())).append(")\n");
//            }
//
//            String finalCa = ca.toString();
//            runOnUiThread(() ->
//                    outputTextView.setText(finalCa));
//            customHandler.postDelayed(this, 1000);
//        }
//    };

    private void analyzeBitmap() {
        predictions = classifier.recognizeImage(bitmapToAnalyze);
        StringBuilder ca = new StringBuilder();
        for (int i = 0; i < predictions.size(); i++) {
            switch (i) {
                case 1:
                    emotionText3.setText(predictions.get(i).getTitle());
                    emotionPercent3.setProgress((int)(predictions.get(i).getConfidence() * 100));
                    break;
                case 2:
                    emotionText2.setText(predictions.get(i).getTitle());
                    emotionPercent2.setProgress((int)(predictions.get(i).getConfidence() * 100));
                    break;
                default:
                    emotionText1.setText(predictions.get(i).getTitle());
                    emotionPercent1.setProgress((int)(predictions.get(i).getConfidence() * 100));
                    break;
            }
            ca.append(predictions.get(i).getTitle()).append("(").append(String.format("%.2f", predictions.get(i).getConfidence())).append(")\n");
        }

        String finalCa = ca.toString();
        runOnUiThread(() ->
                outputTextView.setText(finalCa));
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(90);
//        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        bitmapToAnalyze = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), matrix, true);
//        bitmapToAnalyze = getScaleBitmap(bitmapToAnalyze, 224);
        img.setImageBitmap(bitmapToAnalyze);
//        isCameraBusy = false;

        Log.wtf("mPicture", "wbijam");

//        // cale foto
        Matrix matrix = new Matrix();
        if (frontFacingCamera)
            matrix.postRotate(-90);
        else
            matrix.postRotate(90);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap rotatedBitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), matrix, true);

        Log.wtf("mPicture", "mam foto");
        // z tego wykrywamy twarz JEDNA
        bitmapToDisplay = getCroppedBitmap(rotatedBitmap);
        bitmapToDisplay = getScaledBitmap(bitmapToDisplay, 224);
        Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(3);
        Canvas canvas = new Canvas(bitmapToDisplay);
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setProminentFaceOnly(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
        Frame frame = new Frame.Builder().setBitmap(bitmapToDisplay).build();


        SparseArray<Face> faces = detector.detect(frame);
        ArrayList<Bitmap> facesBitmaps = new ArrayList<>();
        for (int i = 0; i < faces.size(); ++i) {

            Log.wtf("mPicture", "mam twarz");
            Face face = faces.valueAt(i);
            for (Landmark landmark : face.getLandmarks()) {
                int cx = (int) (landmark.getPosition().x * 1);
                int cy = (int) (landmark.getPosition().y * 1);
                canvas.drawCircle(cx, cy, 1, myPaint);
            }

            int x = (int)(face.getPosition().x*0.8f);
            int y = (int)(face.getPosition().y*0.8f);
            int w = (int)(face.getWidth()*1.2f);
            int h = (int)(face.getHeight()*1.2f);

            boolean faceIsFine = true;
            while (x < 0 || y < 0 || x + w > bitmapToDisplay.getWidth() || y + h > bitmapToDisplay.getHeight()){
                Log.wtf("mPicture", "trochę skaluję");
                x++;
                y++;
                w--;
                h--;
                if (w < 10 || h < 10) {
                    faceIsFine = false;
                    break;
                }
            }

            if (faceIsFine) facesBitmaps.add(Bitmap.createBitmap(bitmapToDisplay, x, y, w, h));
        }
        if(facesBitmaps.size() > 0)
            img.setImageBitmap(facesBitmaps.get(0));
        else
            img.setImageBitmap(bitmapToDisplay);
        bitmapToAnalyze = bitmapToDisplay;

        // nowa bitmapa tylko z mordą (kwadratowa i zeskalowana)

        // analiza

        // output

        // preview
        // --- nowa bitmapa, cropped tylko i z zaznaczona mordą

        analyzeBitmap();

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

    private static Bitmap getScaledAndCroppedBitmap(Bitmap bitmap, int size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap b;
        float scale;
        Matrix matrix = new Matrix();
        if (width < height) {
            b = Bitmap.createBitmap(bitmap, 0, height / 2 - width / 2, width, width);
            scale = ((float) size) / width;
            matrix.postScale(scale, scale);
            return Bitmap.createBitmap(b, 0, 0, width, width, matrix, true);
        } else {
            b = Bitmap.createBitmap(bitmap, width / 2 - height / 2, 0, height, height);
            scale = ((float) size) / height;
            matrix.postScale(scale, scale);
            return Bitmap.createBitmap(b, 0, 0, height, height, matrix, true);
        }
    }


    private static Bitmap getScaledBitmap(Bitmap bitmap, int size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale;
        Matrix matrix = new Matrix();
        scale = ((float) size) / width;
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

    }

    private static Bitmap getCroppedBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap b;
        if (width < height) {
            b = Bitmap.createBitmap(bitmap, 0, height / 2 - width / 2, width, width);
            return Bitmap.createBitmap(b, 0, 0, width, width);
        } else {
            b = Bitmap.createBitmap(bitmap, width / 2 - height / 2, 0, height, height);
            return Bitmap.createBitmap(b, 0, 0, height, height);
        }
    }
    public Camera getCameraInstance(boolean frontFacingCamera) {
        if (camera != null) camera.release();

        Camera c = null;
        try {
            if (frontFacingCamera)
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
            else
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }

//        camera.startPreview();
//        camera.setPreviewDisplay(previewHolder);

//        CameraPreview cameraPreview = new CameraPreview(this, camera);
//        FrameLayout cameraFrameLayout = findViewById(R.id.cameraFrameLayout);
//        cameraFrameLayout.addView(cameraPreview);
//        cameraFrameLayout.getLayoutParams().height = -1;
        return c; // returns null if camera is unavailable
    }

}
