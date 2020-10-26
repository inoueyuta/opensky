package io.github.inoueyuta.opensky.model;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class requestAPITask extends AsyncTask<String,Void,String> {
    @Override
    protected String doInBackground(String... urls) {
        return requestAPI(urls[0]);
    }

    public String requestAPI(String urlString) {
        Log.d("opensky", urlString);
        Log.d("opensky", "doInBackground start");
        try {
            StringBuilder sb = new StringBuilder();

            HttpURLConnection urlConnection = null;
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();
            while (data != -1) {
                char current = (char) data;
                //result += current;
                sb.append(current);
                data = reader.read();
            }
            Log.i("opensky", "doInBackground end");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if ( s==null ) {
            return;
        }
    }
}
