package com.estiplate.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    public static final String IP = "192.168.0.102";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = getSharedPreferences("com.estiplate.app.prefs", Context.MODE_PRIVATE);
        String token = pref.getString("token","");
        if ( token.length() > 0 ) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginTask loginTask = new LoginTask();
                loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    private class LoginTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            doLogin();
            return null;
        }
    }

    private void doLogin() {
        EditText usernameEdit = (EditText) findViewById(R.id.username);
        EditText passwordEdit = (EditText) findViewById(R.id.password);
        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        if ( username == null || username.length() == 0 ) {
            alertBuilder.setMessage("Username can't be empty");
            alertBuilder.show();
        } else if ( password == null || password.length() == 0 ) {
            alertBuilder.setMessage("Password can't be empty");
            alertBuilder.show();
        }
        JSONObject loginObject = new JSONObject();
        try {
            loginObject.put("command", "login");
            loginObject.put("username", username);
            loginObject.put("password", password);
        } catch (JSONException e ) {
            Log.e(MainActivity.class.toString(), "Why would this fail?");
        }

        URL loginUrl = null;
        try {
            loginUrl = new URL("http://" + IP + ":8080/helpings/user");
        } catch ( MalformedURLException e) {
            // not happening
            return;
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) loginUrl.openConnection();
        } catch (IOException e) {
            return;
        }

        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(loginObject.toString().getBytes());
            out.flush();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String result = convertStreamToString(in);
            try {
                JSONObject res = new JSONObject(result);
                String token = res.getString("token");
                SharedPreferences pref = getSharedPreferences("com.estiplate.app.prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("token", token);
                editor.putString("username", username);
                editor.commit();
                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
            } catch (JSONException e ) {

            }
        } catch ( IOException e ) {
            Log.e(MainActivity.class.toString(), e.toString());
        }
        finally{
            urlConnection.disconnect();
        }
    }

    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
