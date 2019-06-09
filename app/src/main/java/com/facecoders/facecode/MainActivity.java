package com.facecoders.facecode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
//import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facecoders.facecode.tflite.Classifier;
import com.facecoders.facecode.tflite.Classifier.Device;
import com.facecoders.facecode.tflite.Classifier.Model;
import com.facecoders.facecode.tflite.Classifier.Recognition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

//    private Camera camera;
    private boolean frontFacingCamera = true;

    private static final int PERMISSIONS_REQUEST = 108;

    private Classifier classifier;


    TextureView cameraPreviewTextureView;

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

    List<Recognition> predictions;

    android.os.Handler customHandler = new android.os.Handler();
    android.os.Handler customHandler2 = new android.os.Handler();

    boolean isCameraBusy = false;
    private Object CameraActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestMultiplePermissions();

//        cameraPreviewTextureView = findViewById(R.id.cameraPreviewTextureView);

        outputTextView = findViewById(R.id.outputTextView);
        toAnalyzeImageView = findViewById(R.id.toAnalyzeImageView);

        emotion1TextView = findViewById(R.id.emotion1TextView);
        emotion2TextView = findViewById(R.id.emotion2TextView);
        emotion3TextView = findViewById(R.id.emotion3TextView);
        emotion1ProgressBar = findViewById(R.id.emotion1ProgressBar);
        emotion2ProgressBar = findViewById(R.id.emotion2ProgressBar);
        emotion3ProgressBar = findViewById(R.id.emotion3ProgressBar);

        changeCameraImageButton = findViewById(R.id.changeCameraImageButton);


        Intent k = new Intent(this, com.facecoders.facecode.activities.CameraActivity.class);
        startActivity(k);
        //finish();


        //        changeCameraImageButton.setOnClickListener(v -> {
//            frontFacingCamera = !frontFacingCamera;
//            camera = getCameraInstance(frontFacingCamera);
//            camera.setDisplayOrientation(90);
//            Camera.Parameters cameraParameters = camera.getParameters();
//            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //It is better to use defined constraints as opposed to String, thanks to AbdelHady
//            cameraParameters.setFocusMode("continuous-picture");
//            cameraParameters.setPictureSize(1920, 1080);
//            camera.setParameters(cameraParameters);
//            CameraPreview cameraPreview = new CameraPreview(this, camera);
//            FrameLayout cameraFrameLayout = findViewById(R.id.cameraFrameLayout);
//            cameraFrameLayout.addView(cameraPreview);
//            cameraFrameLayout.getLayoutParams().height = -1;
//
//            customHandler.removeCallbacks(updateBitmap);
//            customHandler.postDelayed(updateBitmap, 1000);
//        });


//        camera = getCameraInstance(frontFacingCamera);
//        camera.setDisplayOrientation(90);
//
//
//        Camera.Parameters cameraParameters = camera.getParameters();
//
//        for (Camera.Size size : cameraParameters.getSupportedPictureSizes()) {
//            if (1080 <= size.width && size.width <= 1920) {
//                cameraParameters.setPreviewSize(size.width, size.height);
//                cameraParameters.setPictureSize(size.width, size.height);
//                break;
//            }
//        }
//        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //It is better to use defined constraints as opposed to String, thanks to AbdelHady
//        cameraParameters.setFocusMode("continuous-picture");
//        cameraParameters.setPictureSize(1920, 1080);
////        camera.setParameters(cameraParameters);
//        camera.setParameters(cameraParameters);
//
//        List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
//        for(Camera.Size c : supportedSizes)
//            Log.wtf("eadadea", c.height + "x"+c.width);

//        CameraPreview cameraPreview = new CameraPreview(this, camera);
//        cameraPreviewTextureView.addView(cameraPreview);

//        Log.wtf("camera1", cameraFrameLayout.getLayoutParams().height+"");
//        Log.wtf("camera2", cameraFrameLayout.getLayoutParams().width+"");
//        Log.wtf("camera3", cameraParameters.getPreviewSize().width+"");
//        Log.wtf("camera4", cameraParameters.getPreviewSize().height+"");
//        toAnalyzeImageView.getLayoutParams().height = -1;
//        cameraFrameLayout.getLayoutParams().height = cameraFrameLayout.getLayoutParams().width*cameraParameters.getPreviewSize().width/cameraParameters.getPreviewSize().height;
//        a.height = 100;
//        cameraFrameLayout.setLayoutParams(a);

//                .LayoutParams(
//                cameraFrameLayout.getWidth(), cameraFrameLayout.getWidth()*cameraParameters.getPreviewSize().width/cameraParameters.getPreviewSize().height));

//
//                      9                                            16
//        cameraParameters.getPreviewSize().width         cameraParameters.getPreviewSize().height
//        height????                                                          width

//        Model model = Model.valueOf("Quantized".toUpperCase());
        Model model = Model.valueOf("FLOAT");
        Device device = Device.valueOf("CPU");
        int numThreads = 4;


        try {
            classifier = Classifier.create(this, model, device, numThreads);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        customHandler.postDelayed(updateBitmap, 500);

    }





//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && toAnalyzeImageView.getVisibility() == View.VISIBLE) {
//            cameraPreviewTextureView.setVisibility(View.VISIBLE);
//            toAnalyzeImageView.setVisibility(View.INVISIBLE);
//            return false;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//    public void onClickShutter(View view) {
//        mCamera2.takePicture(reader -> {
//            final Image image = reader.acquireLatestImage();
//            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//            byte[] bytes = new byte[buffer.remaining()];
//            buffer.get(bytes);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            image.close();
//
//            toAnalyzeImageView.setImageBitmap(bitmap);
//            toAnalyzeImageView.setVisibility(View.VISIBLE);
//            cameraPreviewTextureView.setVisibility(View.INVISIBLE);
//        });
//    }








//
//    private Runnable updateBitmap = new Runnable() {
//        public void run() {
////            if (!) {
//            camera.startFaceDetection();
//                isCameraBusy = true;
//                camera.takePicture(null, null, mPicture);
////            }
//            customHandler.postDelayed(this, 1000);
////            customHandler.postDelayed(this, 1/60);
//        }
//    };
//
//
//    private Camera.PictureCallback mPicture = (data, camera) -> {
//        isCameraBusy = true;
//
//        Matrix matrix = new Matrix();
//        if (frontFacingCamera)
//            matrix.postRotate(-90);
//        else
//            matrix.postRotate(90);
//
//        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        Bitmap rotatedBitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), matrix, true);
//
//        bitmapToDisplay = getGrayscaleBitmap(getCroppedBitmap(rotatedBitmap));
//
////        Paint myPaint = new Paint();
////        myPaint.setColor(Color.GREEN);
////        myPaint.setStyle(Paint.Style.STROKE);
////        myPaint.setStrokeWidth(1);
////        Canvas canvas = new Canvas(bitmapToDisplay);
//        FaceDetector detector = new FaceDetector.Builder(this)
//                .setTrackingEnabled(false)
//                .setProminentFaceOnly(true)
//                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
//                .build();
//        Frame frame = new Frame.Builder().setBitmap(bitmapToDisplay).build();
//
//
//        SparseArray<Face> faces = detector.detect(frame);
//        ArrayList<Bitmap> facesBitmaps = new ArrayList<>();
//        for (int i = 0; i < faces.size(); ++i) {
//            Face face = faces.valueAt(i);
////            for (Landmark landmark : face.getLandmarks()) {
////                int cx = (int) (landmark.getPosition().x);
////                int cy = (int) (landmark.getPosition().y);
////                canvas.drawCircle(cx, cy, 1, myPaint);
////            }
//
//            int x = (int) (face.getPosition().x);
//            int y = (int) (face.getPosition().y);
//            int w = (int) (face.getWidth());
//            int h = (int) (face.getHeight());
//
//            boolean faceIsFine = true;
//            while (x < 0 || y < 0 || x + w > bitmapToDisplay.getWidth() || y + h > bitmapToDisplay.getHeight()) {
//                x++;
//                y++;
//                w--;
//                h--;
//                if (w <= 48 || h <= 48) {
//                    faceIsFine = false;
//                    break;
//                }
//            }
//
//            if (faceIsFine) bitmapToDisplay = Bitmap.createBitmap(bitmapToDisplay, x, y, w, h);
//        }
//
//        bitmapToDisplay = Bitmap.createScaledBitmap(bitmapToDisplay, 48, 48, true);
//
//        toAnalyzeImageView.setImageBitmap(bitmapToDisplay);
//
//        bitmapToAnalyze = bitmapToDisplay;
//
//        analyzeBitmap();
//
//        isCameraBusy = false;
//    };
//

    private void analyzeBitmap() {
        predictions = classifier.recognizeImage(bitmapToAnalyze);
        StringBuilder ca = new StringBuilder();
        for (int i = 0; i < predictions.size(); i++) {
            switch (i) {
                case 1:
                    emotion1TextView.setText(predictions.get(i).getTitle());
                    emotion3ProgressBar.setProgress((int) (predictions.get(i).getConfidence() * 100));
                    break;
                case 2:
                    emotion2TextView.setText(predictions.get(i).getTitle());
                    emotion2ProgressBar.setProgress((int) (predictions.get(i).getConfidence() * 100));
                    break;
                default:
                    emotion3TextView.setText(predictions.get(i).getTitle());
                    emotion1ProgressBar.setProgress((int) (predictions.get(i).getConfidence() * 100));
                    break;
            }
            ca.append(predictions.get(i).getTitle()).append("(").append(String.format("%.2f", predictions.get(i).getConfidence())).append(")\n");
        }

        String finalCa = ca.toString();
        runOnUiThread(() ->
                outputTextView.setText(finalCa));
    }


    public Bitmap getGrayscaleBitmap(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }



    private static Bitmap getScaledBitmap(Bitmap bitmap, int size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale;
        Matrix matrix = new Matrix();
        scale = (float) (size/width);
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

//
//    public Camera getCameraInstance(boolean frontFacingCamera) {
////        if (camera != null) camera.release();
//
//        Camera c = null;
//        try {
////            if (frontFacingCamera)
//            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
////            else
////                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
//        } catch (Exception e) {
//            // Camera is not available (in use or does not exist)
//        }
//
////        camera.startPreview();
////        camera.setPreviewDisplay(previewHolder);
//
////        CameraPreview cameraPreview = new CameraPreview(this, camera);
////        FrameLayout cameraFrameLayout = findViewById(R.id.cameraFrameLayout);
////        cameraFrameLayout.addView(cameraPreview);
////        cameraFrameLayout.getLayoutParams().height = -1;
//        return c; // returns null if camera is unavailable
//    }

}
