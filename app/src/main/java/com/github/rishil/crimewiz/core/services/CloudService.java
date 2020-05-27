package com.github.rishil.crimewiz.core.services;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class CloudService {
    private Double latitude, longitude;

    public static final int HTTP_REQUEST_CODE = 1;
    public static final String HTTP_KEY_RESPONSE = "HTTP_KEY_RESPONSE";
    private static final String REQUEST_METHOD_GET = "GET";

    private Handler httpMessageHandler;

    public CloudService(Handler newHandler){
        httpMessageHandler = newHandler;
    }


    public void getData(double lat, double lon){
        this.latitude = lat;
        this.longitude = lon;
        processRequest();
    }
    private void processRequest(){
        String requestUrl = "https://data.police.uk/api/crimes-street/all-crime?";
        //requestUrl = requestUrl + "lat=" + latitude + "&lng=" + longitude;
        // Only query circular region around the marker
        String poly = "poly=" + (latitude - 0.005) + "," + (longitude + 0.005) + ":"
                + (latitude + 0.005) + "," + (longitude + 0.005) + ":"
                + (latitude + 0.005) + "," + (longitude - 0.005) + ":"
                + (latitude - 0.005) + "," + (longitude - 0.005);

        requestUrl = requestUrl + poly; // add this polygon to the request

        int startYear = 2017;
        Calendar now = Calendar.getInstance();
        for (int i = startYear; i <= now.get(Calendar.YEAR); i++) {
            searchForYear(i, requestUrl);
        }

    }

    private void searchForYear(int year, String requestUrl){
        for (int i = 1; i <= 12; i++){
            if (i < 10){
                sendHTTPRequest(REQUEST_METHOD_GET, requestUrl + "&date="+year+"-0" + i);
            } else {
                sendHTTPRequest(REQUEST_METHOD_GET, requestUrl + "&date="+year+"-" + i);
            }
        }
    }

    private void sendHTTPRequest(final String method, final String urlParams){
        Thread sendHttpRequestThread = new Thread()
        {
            @Override
            public void run() {
                HttpURLConnection httpConn = null;
                InputStreamReader isReader = null;
                BufferedReader bufReader = null;

                // Declare input stream
                InputStream inputStream = null;
                StringBuffer readTextBuf = new StringBuffer();


                try {
                    // https://data.police.uk/api/crimes-street/all-crime?poly=52.268,0.543:52.794,0.238:52.130,0.478&date=2017-01
                    URL url = new URL(urlParams);

                    //URL url = new URL("https://data.police.uk/api/crimes-street/all-crime?poly=52.268,0.543:52.794,0.238:52.130,0.478&date=2017-01");

                    httpConn = (HttpURLConnection)url.openConnection();
                    httpConn.setRequestMethod(method);
                    httpConn.setConnectTimeout(10000);
                    httpConn.setReadTimeout(10000);
                    inputStream = httpConn.getInputStream();
                    isReader = new InputStreamReader(inputStream);
                    bufReader = new BufferedReader(isReader);

                    String line = bufReader.readLine();

                    while(line != null) {
                        readTextBuf.append(line);
                        line = bufReader.readLine();
                    }

                    Message message = new Message();
                    message.what = HTTP_REQUEST_CODE;

                    int code = httpConn.getResponseCode(); // used for debugging

                    // Put the Json in a bundle as a string
                    Bundle bundle = new Bundle();
                    bundle.putString(HTTP_KEY_RESPONSE, readTextBuf.toString());
                    // Set bundle data in message.
                    message.setData(bundle);
                    // Send message to main thread Handler to process.
                    httpMessageHandler.sendMessage(message);

                } catch(IOException e) {
                    Log.e("UrlException", e.getMessage(), e);
                } finally {
                    try {
                        if (bufReader != null) {
                            bufReader.close();
                        }
                        if (isReader != null) {
                            isReader.close();
                        }
                        if (httpConn != null) {
                            httpConn.disconnect();
                        }
                        if (inputStream != null){
                            inputStream.close();
                        }
                    }catch (IOException e) {
                        Log.e("UrlException", e.getMessage(), e);
                    }
                }
            }
        };
        // send the request on a new thread
        sendHttpRequestThread.start();
    }
}
