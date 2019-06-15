package com.facecoders.facecode.tflite;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.os.Trace;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public abstract class Classifier {
    public enum Model {
        FLOAT,
        QUANTIZED,
    }

    public enum Device {
        CPU,
        NNAPI,
        GPU
    }

    private static final int MAX_RESULTS = 3;

    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;

    private static int IMAGE_SIZE;
    private static Model MODEL;
    private static Device DEVICE;
    private static int NUMBER_OF_THREADS;

    private static String MODEL_PATH;
    private static String LABELS_PATH;

    private final int[] intValues = new int[getImageSizeX() * getImageSizeY()];


    private final Interpreter.Options tfliteOptions = new Interpreter.Options();

    private MappedByteBuffer tfliteModel;


    private List<String> labels;


    private GpuDelegate gpuDelegate = null;

    protected Interpreter tflite;

    protected ByteBuffer imgData = null;


    public static Classifier getInstance(Activity activity, Model model, Device device, int numThreads, int imageSize, String modelPath, String labelsPath) throws IOException {
        IMAGE_SIZE = imageSize;
        MODEL = model;
        DEVICE = device;
        NUMBER_OF_THREADS = numThreads;

        MODEL_PATH = modelPath;
        LABELS_PATH = labelsPath;

        if (MODEL == Model.QUANTIZED) {
            return new ClassifierQuantized(activity, DEVICE, numThreads);
        } else {
            return new ClassifierFloat(activity, DEVICE, numThreads);
        }
    }


    protected Classifier(Activity activity, Device device, int numThreads) throws IOException {
        tfliteModel = loadModelFile(activity);
        switch (device) {
            case NNAPI:
                tfliteOptions.setUseNNAPI(true);
                break;
            case GPU:
                gpuDelegate = new GpuDelegate();
                tfliteOptions.addDelegate(gpuDelegate);
                break;
            case CPU:
                break;
        }
        tfliteOptions.setNumThreads(numThreads);
        tflite = new Interpreter(tfliteModel, tfliteOptions);
        labels = loadLabelList(activity);
        imgData =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE
                                * getImageSizeX()
                                * getImageSizeY()
                                * DIM_PIXEL_SIZE
                                * getNumBytesPerChannel());
        imgData.order(ByteOrder.nativeOrder());
    }

    private List<String> loadLabelList(Activity activity) throws IOException {
        List<String> labels = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(getLabelPath())));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < getImageSizeX(); ++i) {
            for (int j = 0; j < getImageSizeY(); ++j) {
                final int val = intValues[pixel++];
                addPixelValue(val);
            }
        }
        long endTime = SystemClock.uptimeMillis();
    }

    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        convertBitmapToByteBuffer(bitmap);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("runInference");
        long startTime = SystemClock.uptimeMillis();
        runInference();
        long endTime = SystemClock.uptimeMillis();
        Trace.endSection();

        // Find the best classifications.
        PriorityQueue<Recognition> pq =
                new PriorityQueue<Recognition>(
                        3,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });
        for (int i = 0; i < labels.size(); ++i) {
            pq.add(
                    new Recognition(
                            "" + i,
                            labels.size() > i ? labels.get(i) : "unknown",
                            getNormalizedProbability(i),
                            null));
        }
        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        Trace.endSection();
        return recognitions;
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        }
        tfliteModel = null;
    }


    public int getImageSizeX() {
        return IMAGE_SIZE;
    }


    public int getImageSizeY() {
        return IMAGE_SIZE;
    }

    protected String getModelPath(){
        return MODEL_PATH;
    }


    protected String getLabelPath(){
        return LABELS_PATH;
    }

    protected abstract int getNumBytesPerChannel();


    protected abstract void addPixelValue(int pixelValue);


    protected abstract float getProbability(int labelIndex);


    protected abstract void setProbability(int labelIndex, Number value);


    protected abstract float getNormalizedProbability(int labelIndex);

    protected abstract void runInference();

    protected int getNumLabels() {
        return labels.size();
    }
}
