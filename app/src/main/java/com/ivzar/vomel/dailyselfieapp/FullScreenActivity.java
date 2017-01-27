package com.ivzar.vomel.dailyselfieapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;

public class FullScreenActivity extends AppCompatActivity {
    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(MainActivity.TAG, "FullScreenActivity. onCreate: " + savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        String path = getIntent().getStringExtra("path");
        if (path != null) {
            Log.i(MainActivity.TAG, "onCreate: path: " + path);
            screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
            Bitmap scaledBitmap = MainActivity.getScaledBitmap(new File(getStorageDir(), path).getAbsolutePath(), screenWidth, screenHeight);
            ImageView imageView = (ImageView) findViewById(R.id.fullscreen);
            imageView.setImageBitmap(scaledBitmap);
        }
    }

    private File getStorageDir() {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

}
