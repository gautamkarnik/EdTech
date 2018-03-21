package com.edtech.utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceHandler {

    private final String CLASS_NAME = getClass().getSimpleName();

    public final static int GET = 1;
    public final static int POST = 2;
    public static int STATUS_CODE = 0;

    public ServiceHandler() {

    }

    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * */
    public String makeServiceCall(String url, int method) {
        //return this.makeServiceCall(url, method, null);
        return this.makeServiceCall2(url, method, null, true);
    }

    // Source: http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
    /**
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public String makeServiceCall(String url, int method,
                                  List<NameValuePair> params) {
        String response = null;
        try {
            // http client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                // adding post params
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }

                httpResponse = httpClient.execute(httpPost);

            } else if (method == GET) {
                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url);

                httpResponse = httpClient.execute(httpGet);

            }
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;

    }

    // Source: https://gist.github.com/mypapit/642de0968a01bf13a936b6f62e874a48
    public String makeServiceCall2(String stringUrl, int method, String urlParameters, boolean isJsonRequest) {

        StringBuilder sbResponse = null;
        HttpURLConnection httpURLConnection = null;
        try {
            if (method == GET) {
                if (urlParameters != null) {
                    stringUrl = stringUrl + "?" + urlParameters;
                }
                URL url = new URL(stringUrl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setAllowUserInteraction(false);
                httpURLConnection.setConnectTimeout(0);
                httpURLConnection.setReadTimeout(0);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(false);
                httpURLConnection.connect();

            } else if (method == POST) {
                URL url = new URL(stringUrl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setAllowUserInteraction(false);
                httpURLConnection.setConnectTimeout(0);
                httpURLConnection.setReadTimeout(0);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                if (urlParameters != null) {
                    if (isJsonRequest) {
                        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    } else {
                        httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                    }
                    httpURLConnection.connect();
                    OutputStream os = httpURLConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(urlParameters);
                    writer.flush();
                    writer.close();
                    os.close();
                }
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            sbResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sbResponse.append(line + "\n");
            }
            br.close();
            STATUS_CODE = httpURLConnection.getResponseCode();
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                STATUS_CODE = httpURLConnection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        httpURLConnection.disconnect();
        return sbResponse.toString();
    }
}