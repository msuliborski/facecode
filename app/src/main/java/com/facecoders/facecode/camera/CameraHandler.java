package com.facecoders.facecode.camera;

import android.hardware.Camera;

public abstract class CameraHandler {

    private static Camera camera = null;

    private CameraHandler(){

    }

    public static Camera getCameraInstance(boolean frontFacingCamera) {
        if (camera != null) camera.release();

        Camera c = null;

        try {
            if (frontFacingCamera)
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
            else
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            e.getStackTrace();
        }
        camera = c;
        return c;
    }

    public static void setParameters(){
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
    }


}
