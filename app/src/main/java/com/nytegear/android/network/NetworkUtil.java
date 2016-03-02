package com.nytegear.android.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

/**
 * Created by Aaron Helton on 1/30/2016
 */
public final class NetworkUtil
{
    private static final String LOG_TAG = NetworkUtil.class.getSimpleName();

    public static String getURL(String urlString) {
        return getUrlImpl(urlString);
    }

    public static Bitmap getImage(String url, Context context, Bitmap defImg) {
        return defImageImpl(url, context, defImg);
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

    public static boolean isInternetAvailable(Context context) {
        final ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo current = manager.getActiveNetworkInfo();

        boolean connected = (current != null) && current.isConnected();
        if(!connected) return false;

        //Check route to make sure we aren't on a closed-off network
        boolean routeExists;
        Socket s = null;
        try {
            //Check against Google DNS Server
            InetAddress host = InetAddress.getByName("8.8.8.8");
            s = new Socket();
            s.connect(new InetSocketAddress(host, 53), 5000); //5 second timeout. Plenty of time.
            routeExists = true;
            s.close();
        } catch (IOException ex) {
            routeExists = false;
            if(s != null && !s.isClosed()) {
                try {
                    s.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "ERROR CLOSING SOCKET: " + e.getMessage(), e);
                }
            }
        }
        return routeExists;
    }
}
