package com.example.android.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Aaron Helton on 1/30/2016
 */
public final class NetworkUtil
{
    private static final String LOG_TAG = NetworkUtil.class.getSimpleName();
    private static final ArrayList<Long> requestTimes = new ArrayList<>();
    private static final ArrayList<Long> imgRequestTimes = new ArrayList<>();

    public static String getURL(String urlString) {
        long nano = System.nanoTime();
        String res = getUrlImpl(urlString);

        //Statistics
        long time = (System.nanoTime()-nano)/1000000;
        requestTimes.add(time);
        long avg = 0;
        long min = 10000;
        long max = -1;
        Log.d(LOG_TAG, "REQUEST: " + time + "ms");
        for(Long num : requestTimes)
        {
            if(num < min) min = num;
            if(num > max) max = num;
            avg += num;
        }
        Log.d(LOG_TAG, "AVERAGE TIME: " + ((double)avg/requestTimes.size()) + "ms");
        Log.d(LOG_TAG, "MAX TIME: " + max + "ms");
        Log.d(LOG_TAG, "MIN TIME: " + min + "ms");
        return res;
    }

    public static Bitmap getImage(String url, Context context, Bitmap defImg) {
        long nano = System.nanoTime();

        //Tests on target device show that the straight HTTPUrlConnection version actually performs
        //faster on average than Picasso, and images being stored as an ImageView later means we
        //don't quite benefit as much from the image cache to make it worth using.
        Bitmap bm = defImageImpl2(url, context, defImg);

        long time = (System.nanoTime()-nano)/1000000;
        imgRequestTimes.add(time);
        long avg = 0;
        long min = 10000;
        long max = -1;
        Log.d(LOG_TAG, "IMAGE REQUEST: " + time + "ms");
        for(Long num : imgRequestTimes)
        {
            if(num < min) min = num;
            if(num > max) max = num;
            avg += num;
        }
        Log.d(LOG_TAG, "AVERAGE TIME: " + ((double)avg/imgRequestTimes.size()) + "ms");
        Log.d(LOG_TAG, "MAX TIME: " + max + "ms");
        Log.d(LOG_TAG, "MIN TIME: " + min + "ms");
        return bm;
    }

    private static Bitmap defImageImpl(String url, Context context, Bitmap defImg)
    {
        try {
            return Picasso.with(context).load(url).get();
        } catch(Downloader.ResponseException ex) {
            return defImg;
        } catch (IOException ex) {
            return defImg;
        }
    }

    private static Bitmap defImageImpl2(String urlString, Context context, Bitmap defImg)
    {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "Malformed URL: " + urlString + ";\n" + ex.getMessage(), ex);
            return defImg;
        } catch (IOException ex) {
            Log.e(LOG_TAG, "I/O ERROR: " + ex.getMessage(), ex);
            return defImg;
        }
    }

    private static String getUrlImpl(String urlString)
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
