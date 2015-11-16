package com.estiplate.app;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by stephenpreer on 11/10/15.
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity {

    Camera mCamera;
    CameraCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_main);
        mCamera = null;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
        }
        mCallback = new CameraCallback();
        CameraPreview preview = new CameraPreview(this, mCamera);
        FrameLayout previewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        previewFrame.addView(preview);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.capture);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, mCallback);
            }
        });
    }

    class CameraCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
            Intent intent = new Intent(CameraActivity.this, UploadActivity.class);
            intent.putExtra("filename", pictureFile.getAbsolutePath());
            startActivity(intent);
            finish();
        }
    };

    private File getOutputMediaFile() {
        File mediaStorageDir = getExternalFilesDir(null);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }
}