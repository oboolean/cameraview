package com.google.android.cameraview.demo;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import com.google.android.cameraview.CameraNoView;

@RequiresApi(api = VERSION_CODES.ICE_CREAM_SANDWICH)
public class NoCameraActivity extends AppCompatActivity {

    CameraNoView cameraNoView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no);
        cameraNoView = new CameraNoView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraNoView.start();;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraNoView.stop();
    }
}
