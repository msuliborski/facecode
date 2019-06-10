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
        return c;
    }


}
