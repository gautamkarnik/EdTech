package com.edtech.utilities;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;

// Based on source http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
public class LessonAsyncHttpReader extends AsyncTask<Void, Void, String> {

    private final String CLASS_NAME = getClass().getSimpleName();

    public String jsonStrData;

    // URL to get lesson model in JSON format
    private static String url = "https://lit-forest-48979.herokuapp.com/curriculums/58894ec4f36d2836bdc6bd8d";
    //private static String url = "https://lit-forest-48979.herokuapp.com/curriculums/5892b3814aed510011ff28a8";

    /**
     * Async task class to get json by making HTTP call
     * */
    public static int pingLessons(){
        HttpURLConnection connection = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setConnectTimeout(1000);
            connection.setRequestMethod("HEAD");
            int code = connection.getResponseCode();
            Log.d("LessonsAsyncHttpReader", "Ping: " + code);
            // You can determine on HTTP return code received. 200 is success.
            return code;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return -1;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        // Creating service handler class instance
        ServiceHandler sh = new ServiceHandler();

        // Making a request to url and getting response
        jsonStrData = sh.makeServiceCall(url, ServiceHandler.GET);
        return jsonStrData;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

}
