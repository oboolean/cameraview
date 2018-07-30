/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.cameraview;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

public class DumbCamera {

    /** The camera device faces the opposite direction as the device's screen. */
    public static final int FACING_BACK = Constants.FACING_BACK;

    /** The camera device faces the same direction as the device's screen. */
    public static final int FACING_FRONT = Constants.FACING_FRONT;

    /** Direction the camera faces relative to device screen. */
    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Facing {
    }

    /** Flash will not be fired. */
    public static final int FLASH_OFF = Constants.FLASH_OFF;

    /** Flash will always be fired during snapshot. */
    public static final int FLASH_ON = Constants.FLASH_ON;

    /** Constant emission of light during preview, auto-focus and snapshot. */
    public static final int FLASH_TORCH = Constants.FLASH_TORCH;

    /** Flash will be fired automatically when required. */
    public static final int FLASH_AUTO = Constants.FLASH_AUTO;

    /** Flash will be fired in red-eye reduction mode. */
    public static final int FLASH_RED_EYE = Constants.FLASH_RED_EYE;

    /** The mode for for the camera device's flash control */
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO, FLASH_RED_EYE})
    public @interface Flash {
    }


    private Context mContext;
    CameraViewImpl mImpl;

    private final CallbackBridge mCallbacks;

    private boolean mAdjustViewBounds;

    private final DisplayOrientationDetector mDisplayOrientationDetector;


    @RequiresApi(api = VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressWarnings("WrongConstant")
    public DumbCamera(Context context) {
        // Internal setup
        final PreviewImpl preview = createPreviewImpl(context);
        mCallbacks = new CallbackBridge();
        if (Build.VERSION.SDK_INT < 21) {
            mImpl = new Camera1(mCallbacks, preview);
        } else if (Build.VERSION.SDK_INT < 23) {
            mImpl = new Camera2(mCallbacks, preview, context);
        } else {
            mImpl = new Camera2Api23(mCallbacks, preview, context);
        }
        // Attributes

        // Display orientation detector
        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {
            @Override
            public void onDisplayOrientationChanged(int displayOrientation) {
                mImpl.setDisplayOrientation(displayOrientation);
            }
        };
    }

    @RequiresApi(api = VERSION_CODES.ICE_CREAM_SANDWICH)
    @NonNull
    private PreviewImpl createPreviewImpl(Context context) {
        PreviewImpl preview = new DumbPreviewImpl(context);
        return preview;
    }



    /**
     * Open a camera device and start showing camera preview. This is typically called from
     * {@link Activity#onResume()}.
     */
    @RequiresApi(api = VERSION_CODES.ICE_CREAM_SANDWICH)
    public void start() {
        if (!mImpl.start()) {
            //store the state ,and restore this state after fall back o Camera1
            // Camera2 uses legacy hardware layer; fall back to Camera1
            mImpl = new Camera1(mCallbacks, createPreviewImpl(mContext));
            mImpl.start();
        }
    }

    /**
     * Stop camera preview and close the device. This is typically called from
     * {@link Activity#onPause()}.
     */
    public void stop() {
        mImpl.stop();
    }

    /**
     * @return {@code true} if the camera is opened.
     */
    public boolean isCameraOpened() {
        return mImpl.isCameraOpened();
    }

    /**
     * Add a new callback.
     *
     * @param callback The {@link Callback} to add.
     * @see #removeCallback(Callback)
     */
    public void addCallback(@NonNull Callback callback) {
        mCallbacks.add(callback);
    }

    /**
     * Remove a callback.
     *
     * @param callback The {@link Callback} to remove.
     * @see #addCallback(Callback)
     */
    public void removeCallback(@NonNull Callback callback) {
        mCallbacks.remove(callback);
    }

    /**
     * @param adjustViewBounds {@code true} if you want the CameraView to adjust its bounds to
     *                         preserve the aspect ratio of camera.
     * @see #getAdjustViewBounds()
     */
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (mAdjustViewBounds != adjustViewBounds) {
            mAdjustViewBounds = adjustViewBounds;
        }
    }

    /**
     * @return True when this CameraView is adjusting its bounds to preserve the aspect ratio of
     * camera.
     * @see #setAdjustViewBounds(boolean)
     */
    public boolean getAdjustViewBounds() {
        return mAdjustViewBounds;
    }

    /**
     * Chooses camera by the direction it faces.
     *
     * @param facing The camera facing. Must be either {@link #FACING_BACK} or
     *               {@link #FACING_FRONT}.
     */
    public void setFacing(@Facing int facing) {
        mImpl.setFacing(facing);
    }

    /**
     * Gets the direction that the current camera faces.
     *
     * @return The camera facing.
     */
    @Facing
    public int getFacing() {
        //noinspection WrongConstant
        return mImpl.getFacing();
    }

    /**
     * Gets all the aspect ratios supported by the current camera.
     */
    public Set<AspectRatio> getSupportedAspectRatios() {
        return mImpl.getSupportedAspectRatios();
    }

    /**
     * Sets the aspect ratio of camera.
     *
     * @param ratio The {@link AspectRatio} to be set.
     */
    public void setAspectRatio(@NonNull AspectRatio ratio) {
        if (mImpl.setAspectRatio(ratio)) {
        }
    }

    /**
     * Gets the current aspect ratio of camera.
     *
     * @return The current {@link AspectRatio}. Can be {@code null} if no camera is opened yet.
     */
    @Nullable
    public AspectRatio getAspectRatio() {
        return mImpl.getAspectRatio();
    }

    /**
     * Enables or disables the continuous auto-focus mode. When the current camera doesn't support
     * auto-focus, calling this method will be ignored.
     *
     * @param autoFocus {@code true} to enable continuous auto-focus mode. {@code false} to
     *                  disable it.
     */
    public void setAutoFocus(boolean autoFocus) {
        mImpl.setAutoFocus(autoFocus);
    }

    /**
     * Returns whether the continuous auto-focus mode is enabled.
     *
     * @return {@code true} if the continuous auto-focus mode is enabled. {@code false} if it is
     * disabled, or if it is not supported by the current camera.
     */
    public boolean getAutoFocus() {
        return mImpl.getAutoFocus();
    }

    /**
     * Sets the flash mode.
     *
     * @param flash The desired flash mode.
     */
    public void setFlash(@Flash int flash) {
        mImpl.setFlash(flash);
    }

    /**
     * Gets the current flash mode.
     *
     * @return The current flash mode.
     */
    @Flash
    public int getFlash() {
        //noinspection WrongConstant
        return mImpl.getFlash();
    }

    /**
     * Take a picture. The result will be returned to
     * {@link Callback#onPictureTaken(DumbCamera, byte[])}.
     */
    public void takePicture() {
        mImpl.takePicture();
    }

    private class CallbackBridge implements CameraViewImpl.Callback {

        private final ArrayList<Callback> mCallbacks = new ArrayList<>();

        private boolean mRequestLayoutOnOpen;

        CallbackBridge() {
        }

        public void add(Callback callback) {
            mCallbacks.add(callback);
        }

        public void remove(Callback callback) {
            mCallbacks.remove(callback);
        }

        @Override
        public void onCameraOpened() {
            if (mRequestLayoutOnOpen) {
                mRequestLayoutOnOpen = false;
            }
            for (Callback callback : mCallbacks) {
                callback.onCameraOpened(DumbCamera.this);
            }
        }

        @Override
        public void onCameraClosed() {
            for (Callback callback : mCallbacks) {
                callback.onCameraClosed(DumbCamera.this);
            }
        }

        @Override
        public void onPictureTaken(byte[] data) {
            for (Callback callback : mCallbacks) {
                callback.onPictureTaken(DumbCamera.this, data);
            }
        }

        @Override
        public void onPreviewFrame(byte[] bytes) {
            for (Callback callback : mCallbacks) {
                callback.onPreviewFrame(bytes);
            }
        }

        @Override
        public void onPreviewFrame(byte[][] bytesYUV, int[] yuv, int w, int h) {
            for (Callback callback : mCallbacks) {
                callback.onPreviewFrame(bytesYUV,yuv,w,h);
            }
        }

        public void reserveRequestLayoutOnOpen() {
            mRequestLayoutOnOpen = true;
        }
    }



    /**
     * Callback for monitoring events about {@link DumbCamera}.
     */
    @SuppressWarnings("UnusedParameters")
    public abstract static class Callback {

        /**
         * Called when camera is opened.
         *
         * @param cameraView The associated {@link DumbCamera}.
         */
        public void onCameraOpened(DumbCamera cameraView) {
        }

        /**
         * Called when camera is closed.
         *
         * @param cameraView The associated {@link DumbCamera}.
         */
        public void onCameraClosed(DumbCamera cameraView) {
        }

        /**
         * Called when a picture is taken.
         *
         * @param cameraView The associated {@link DumbCamera}.
         * @param data       JPEG data.
         */
        public void onPictureTaken(DumbCamera cameraView, byte[] data) {
        }

        public void onPreviewFrame(byte[] bytes) {

        }

        public void onPreviewFrame(byte[][] bytesYUV,int[] yuv,int w,int h){

        }
    }

}
