package com.htetznaing.myittarpwal.RssReader;

/**
 * Created by HtetzNaing on 1/16/2018.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Marco Gomiero on 6/17/16.
 */
public class ReadJSON extends AsyncTask<String, Void, String> implements Observer {
    private static ArrayList<FeedItem> articles = new ArrayList<>();
    private OnTaskCompleted onComplete;
    private String address;
    ArrayList<FeedItem> feedItems;
    URL url;

    public ReadJSON(String address) {
        this.address=address;
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(ArrayList<FeedItem> list);
        void onError();
    }

    public void onFinish(OnTaskCompleted onComplete) {
        this.onComplete = onComplete;
    }

    @Override
    protected String doInBackground(String... ulr) {
        try {
            ProcessJSON(Getdata());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (feedItems != null) {
            onComplete.onTaskCompleted(feedItems);
        } else
            onComplete.onError();
    }


    private void ProcessJSON(String data) {
        feedItems = new ArrayList<>();
        if (data != null) {
            try {
                JSONObject jo=new JSONObject(data);
                JSONArray ja=jo.getJSONObject("feed").getJSONArray("entry");
                JSONObject jo2;

                for(int i=0;i<ja.length();i++){
                    FeedItem item = new FeedItem();
                    jo2=ja.getJSONObject(i);
                    String label = null;
                    JSONArray cast = jo2.getJSONArray("category");
                    for (int o=0; o<cast.length(); o++) {
                        JSONObject actor = cast.getJSONObject(o);
                        label = actor.getString("term");
                    }
                    item.setTitle(jo2.getJSONObject("title").getString("$t"));
                    item.setDescription(jo2.getJSONObject("content").getString("$t"));
                    item.setCategories(label);

                    Log.d("Label",item.getCategories());
                    Log.d("Title",item.getTitle());
                    Log.d("Content",item.getDescription());
                    feedItems.add(item);
                }
            } catch (JSONException e) {
                Log.d("Start","Failed");
            }
            }
        }

    //This method will download rss feed document from specified url
    private String Getdata() throws IOException {
        String queryReturn = "";
        URL queryURL = new URL(address);
        HttpURLConnection httpURLConnection = (HttpURLConnection)queryURL.openConnection();
        if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
            InputStreamReader inputStreamReader =
                    new InputStreamReader(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader, 8192);
            String line = null;
            while((line = bufferedReader.readLine()) != null){
                queryReturn += line;
            }
            bufferedReader.close();
        }
        return queryReturn;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Observable observable, Object data) {
        articles = (ArrayList<FeedItem>) data;
        onComplete.onTaskCompleted(articles);
    }
}