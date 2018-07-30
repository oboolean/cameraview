package com.google.android.cameraview;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.ImageReader;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.view.Surface;
import android.view.View;

@RequiresApi(api = VERSION_CODES.ICE_CREAM_SANDWICH)
public class DumbPreviewImpl extends PreviewImpl{

    private Context mContext;

    private Surface mSurface;

    private SurfaceTexture mSurfaceTexture;

    private View mView;

    public DumbPreviewImpl(Context context){
        mContext = context;
        mView = new View(mContext);
        mSurfaceTexture = new SurfaceTexture(0);
        mSurface = new Surface(mSurfaceTexture);
    }
    @Override
    Surface getSurface() {
        return mSurface;
    }

    @Override
    View getView() {
        return mView;
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    Class getOutputClass() {
        return ImageReader.class;
    }

    @Override
    Object getSurfaceTexture(){
        return mSurfaceTexture;
    }

    @Override
    void setDisplayOrientation(int displayOrientation) {

    }

    @Override
    boolean isReady() {
        return true;
    }
}
