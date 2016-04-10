package com.estiplate.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
 * Created by stephenpreer on 11/16/15.
 */
public class EstiplateService extends Service {

    public static String UPLOAD_ACTION = "com.estiplate.app.action.upload";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int id){
        if ( intent.getAction().equals(UPLOAD_ACTION)) {
            UploadTask uploadTask = new UploadTask();
            uploadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, intent);
        }
        return START_NOT_STICKY;
    }

    private String mBoundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection mHttpConnection;
    DataOutputStream mRequest;
    static final String CRLF = "\r\n";
    static final String TWO_HYPHENS = "--";

    private class UploadTask extends AsyncTask<Intent, Void, Void> {

        String mFilename;
        String mTitle;

        @Override
        protected Void doInBackground(Intent... intents) {
            Intent uploadIntent = intents[0];
            mFilename = uploadIntent.getStringExtra("filename");
            mTitle = uploadIntent.getStringExtra("title");

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

    private void UploadComplete(){

    }
}

