package com.estiplate.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephenpreer on 11/11/15.
 */
public class UploadActivity extends Activity {

    private String mBoundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection mHttpConnection;
    DataOutputStream mRequest;
    private static final String CHARSET = "UTF-8";
    static final String CRLF = "\r\n";
    static final String TWO_HYPHENS = "--";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        final String filename = intent.getStringExtra("filename");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f);

        Bitmap resizedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        ImageView imageView = (ImageView) findViewById(R.id.upload_image);
        imageView.setImageBitmap(resizedBitmap);

        Button loginButton = (Button) findViewById(R.id.button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadTask uploadTask = new UploadTask();
                uploadTask.init(filename, ((EditText) findViewById(R.id.title)).getText().toString());
                uploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    private class UploadTask extends AsyncTask<Void, Void, Void> {

        String mFilename;
        String mTitle;

        public void init(String filename, String title) {
            mFilename = filename;
            mTitle = title;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SharedPreferences pref = getSharedPreferences("com.estiplate.app.prefs", Context.MODE_PRIVATE);
                String token = pref.getString("token", "");
                String username = pref.getString("username", "");

                URL url = new URL("http://" + MainActivity.IP + ":8080/helpings/upload");
                setupConnection(url.toString());
                writeFormData("username", username);
                writeFormData("token", token);
                writeFormData("title", mTitle);
                writeFile(mFilename,"image/jpeg");
                finishConnection();

            } catch (Exception e) {

            }
            return null;
        }
    }

    public void setupConnection(String requestURL)
            throws IOException {

        mBoundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        mHttpConnection = (HttpURLConnection) url.openConnection();
        mHttpConnection.setUseCaches(false);
        mHttpConnection.setDoOutput(true);
        mHttpConnection.setDoInput(true);

        mHttpConnection.setRequestMethod("POST");
        mHttpConnection.setRequestProperty("Connection", "Keep-Alive");
        mHttpConnection.setRequestProperty("Cache-Control", "no-cache");
        mHttpConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + mBoundary);
        mRequest = new DataOutputStream(
                mHttpConnection.getOutputStream());
    }

    public void writeFormData(String key, String value) throws IOException {

        mRequest.writeBytes(TWO_HYPHENS + mBoundary + CRLF);
        mRequest.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + CRLF);
        mRequest.writeBytes(CRLF);
        mRequest.writeBytes(value + CRLF);
    }

    public void writeFile(String attachment, String contentType) throws IOException {

        String filename = attachment.substring(attachment.lastIndexOf('/') + 1, attachment.length());

        mRequest.writeBytes(TWO_HYPHENS + mBoundary + CRLF);
        mRequest.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" +
                filename + "\"" + CRLF);
        mRequest.writeBytes("Content-Type: " + contentType + CRLF);
        mRequest.writeBytes(CRLF);
        File file = new File(attachment);
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            mRequest.write(buffer, 0, len);
        }
        in.close();
        mRequest.writeBytes(CRLF);
    }

    public List<String> finishConnection() throws IOException {
        List<String> response = new ArrayList<>();

        mRequest.writeBytes(LINE_FEED);
        mRequest.writeBytes("--" + mBoundary + "--" + LINE_FEED);
        mRequest.flush();
        mRequest.close();

        int status = mHttpConnection.getResponseCode();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    mHttpConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
        } catch (Exception e) {

        }
        if (status == HttpURLConnection.HTTP_OK) {
            mHttpConnection.disconnect();
        } else {
            for (String line : response) {
                System.out.println(line);
            }
        }

        return response;
    }
}
