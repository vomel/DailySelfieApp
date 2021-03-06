package com.ivzar.vomel.dailyselfieapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "DailySelfie";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final long INITIAL_ALARM_DELAY = 1000 * 60 * 2;
    public static final int INTERVAL_TWO_MINUTES = 1000 * 60 * 2;
    private AlarmManager mAlarmManager;
    private SelfieListAdapter mAdapter;
    private String mCurrentPhotoPath;
    private PendingIntent mNotificationReceiverPendingIntent;
    private Intent mNotificationReceiverIntent;
    private int targetW;
    private int targetH;
    int screenWidth;
    int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: " + savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getString("currentPhotoFileName");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the AlarmManager Service
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        targetW = (int) getResources().getDimension(R.dimen.preview_width);
        targetH = (int) getResources().getDimension(R.dimen.preview_height);
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        mAdapter = new SelfieListAdapter(getApplicationContext(), this);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        // Create an Intent to broadcast to the AlarmNotificationReceiver
        mNotificationReceiverIntent = new Intent(MainActivity.this, AlarmNotificationReceiver.class);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, mNotificationReceiverIntent, 0);
        // Set repeating alarm
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY,
                INTERVAL_TWO_MINUTES,
                mNotificationReceiverPendingIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu: " + menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected: " + item + ":" + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_camera:
                takePicture();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getStorageDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    File getStorageDir() {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    private void takePicture() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                mCurrentPhotoPath = photoFile.getAbsolutePath();
                Uri uri = FileProvider.getUriForFile(this, "com.ivzar.vomel.dailyselfieapp", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> resInfoList = getApplicationContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getApplicationContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                e.printStackTrace();
                mCurrentPhotoPath = null;
            }
        }
    }

    private void readPicToAdapter(String fileName) {
        Log.i(TAG, "readPicToAdapter: " + fileName);
        /* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */
        Bitmap bitmap = getScaledBitmap(fileName, targetW, targetH);

        /* Associate the Bitmap to the ImageView */
        mAdapter.add(new Selfie(bitmap, new File(fileName).getName()));
    }

    static Bitmap getScaledBitmap(String fileName, int targetW, int targetH) {
    /* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        /* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        /* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        /* Decode the JPEG file into a Bitmap */
        return BitmapFactory.decodeFile(fileName, bmOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: " + requestCode + ", " + resultCode + ", " + data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                mAdapter.add(new Selfie(bitmap, new Date().toString()));
            } else {
                readPicToAdapter(mCurrentPhotoPath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentPhotoFileName", mCurrentPhotoPath);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter.getCount() == 0) loadItems();
    }

    private void loadItems() {
        File storageDir = getStorageDir();
        if (isValidDir(storageDir)) {
            for (File picFile : storageDir.listFiles()) {
                readPicToAdapter(picFile.getAbsolutePath());
            }
        }
    }

    static boolean isValidDir(File storageDir) {
        return storageDir != null && storageDir.exists() && storageDir.isDirectory();
    }
}
