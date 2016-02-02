package com.example.android.popularmovies;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Aaron Helton on 1/30/2016
 */
public final class NetworkUtil
{
    private static final String LOG_TAG = NetworkUtil.class.getSimpleName();

    public static String getURL(String urlString)
    {
        if(urlString == null || urlString.isEmpty())
            return null;
        URL url;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder builder = null;
        try {
            url = new URL(urlString);
            connection = (HttpURLConnection)url.openConnection();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            builder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "Malformed URL: " + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            if(connection != null)
            {
                try {
                    Log.e(LOG_TAG, "ERROR CODE: " + connection.getResponseCode(), ex);
                    Log.e(LOG_TAG, "ERROR MSG: " + connection.getResponseMessage(), ex);
                } catch (IOException e) {
                    Log.e(LOG_TAG,
                            "Unable to retrive code/message from response: " + e.getMessage(), e);
                }
            }
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Log.e(LOG_TAG, "Error Closing Stream: " + ex.getMessage(), ex);
                }
            }
        }
        if(builder != null)
            return builder.toString();
        else
            return null;
    }
}
