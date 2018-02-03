package gr.ictpro.jsalatas.sensordata;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by john on 1/20/18.
 */

public class SendDataTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(strings[0]);
            connection = (HttpURLConnection) url.openConnection();

            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);
            connection.setRequestMethod("GET");

            connection.connect();
            connection.getInputStream();
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
