package com.ivzar.vomel.dailyselfieapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "DailySelfie";
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private SelfieListAdapter mAdapter;
    public static final String FILE_NAME = "DailySelfieAppData.txt";
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: " + savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        mAdapter = new SelfieListAdapter(getApplicationContext(), this);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(mAdapter);
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
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//                }
                takePicture();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
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
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                e.printStackTrace();
                mCurrentPhotoPath = null;
            }
        }
    }

    private void readPic(String fileName) {
        Log.i(TAG, "readPic: " + fileName);
        /* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */

        /* Get the size of the ImageView */
        int targetW = (int) getResources().getDimension(R.dimen.preview_width);
        int targetH = (int) getResources().getDimension(R.dimen.preview_height);

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
        Bitmap bitmap = BitmapFactory.decodeFile(fileName, bmOptions);

        /* Associate the Bitmap to the ImageView */
        mAdapter.add(new Selfie(bitmap, new File(fileName).getName()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: " + requestCode + ", " + resultCode + ", " + data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap bitmap = (Bitmap) extras.get("data");
                mAdapter.add(new Selfie(bitmap, new Date().toString()));
            } else {
                readPic(mCurrentPhotoPath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        saveItems();
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
                readPic(picFile.getAbsolutePath());
            }
        }
    }

    static boolean isValidDir(File storageDir) {
        return storageDir != null && storageDir.exists() && storageDir.isDirectory();
    }

/*    private void saveItems() {
        PrintWriter printWriter = null;
        try {
            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Selfie item = (Selfie) mAdapter.getItem(i);
                printWriter.println(item.description);
                Log.i(TAG, "saveItems: " + item.description);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                item.preview.compress(Bitmap.CompressFormat.JPEG, 100, os);
                printWriter.println(new String(os.toByteArray()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }*/
}
