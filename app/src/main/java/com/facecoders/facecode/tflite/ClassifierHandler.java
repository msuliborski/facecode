package com.facecoders.facecode.tflite;

import android.app.Activity;
import android.content.Context;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.SparseArray;

import com.facecoders.facecode.R;
import com.facecoders.facecode.tflite.ClassifierHandler;
import com.facecoders.facecode.tflite.Classifier;
import com.facecoders.facecode.tflite.Classifier.Recognition;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ClassifierHandler {

    private static Classifier classifier;
    private static List<Recognition> predictions = new ArrayList<>();


    public static void initialize(Activity activity, String model, String device, int numThreads, int imageSize) {
        try {
            classifier = Classifier.create(activity, Classifier.Model.valueOf(model), Classifier.Device.valueOf(device), numThreads, imageSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Recognition> analyzeBitmap(Bitmap bitmap){
        predictions.clear();
        predictions = classifier.recognizeImage(bitmap);
        return  predictions;
    }


    public static Bitmap rotateBitmap(Bitmap bitmap, boolean cameraFacingFront){
        Matrix matrix = new Matrix();
        if (cameraFacingFront)
            matrix.postRotate(-90);
        else
            matrix.postRotate(90);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap getFaceBitmap(Context context, Bitmap bitmap, boolean showLandmarks) {
        Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(1);
        Canvas canvas = new Canvas(bitmap);

        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setProminentFaceOnly(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();


        SparseArray<Face> faces = detector.detect(frame);
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.valueAt(i);
            if (showLandmarks) {
                for (Landmark landmark : face.getLandmarks()) {
                    int x = (int) (landmark.getPosition().x);
                    int y = (int) (landmark.getPosition().y);
                    canvas.drawPoint(x, y,  myPaint);
                }
            }
            int x = (int) (face.getPosition().x);
            int y = (int) (face.getPosition().y);
            int w = (int) (face.getWidth());
            int h = (int) (face.getHeight());

            boolean faceIsFine = true;
            while (x < 0 || y < 0 || x + w > bitmap.getWidth() || y + h > bitmap.getHeight()) {
                x++;
                y++;
                w--;
                h--;
                if (w <= 48 || h <= 48) {
                    faceIsFine = false;
                    break;
                }
            }

            if (faceIsFine) return Bitmap.createBitmap(bitmap, x, y, w, h);
        }
        return bitmap;
    }


    public static Bitmap getGrayscaleBitmap(Bitmap bmpOriginal) {
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


    public static Bitmap getScaledBitmap(Bitmap bitmap, int size) {
        return Bitmap.createScaledBitmap(bitmap, size, size, true);
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
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
}
