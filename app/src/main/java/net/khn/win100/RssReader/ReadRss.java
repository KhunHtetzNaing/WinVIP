package net.khn.win100.RssReader;

/**
 * Created by HtetzNaing on 1/16/2018.
 */

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Marco Gomiero on 6/17/16.
 */
public class ReadRss extends AsyncTask<String, Void, String> implements Observer {
    private static ArrayList<FeedItem> articles = new ArrayList<>();
    private OnTaskCompleted onComplete;
    private String address;
    ArrayList<FeedItem> feedItems;
    URL url;

    public ReadRss(String address) {
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
        ProcessXml(Getdata());
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (feedItems != null) {
            onComplete.onTaskCompleted(feedItems);
        } else
            onComplete.onError();
    }


    private void ProcessXml(Document data) {
        if (data != null) {
            feedItems = new ArrayList<>();
            Element root = data.getDocumentElement();
            Node channel = root.getChildNodes().item(0);
            NodeList items = channel.getChildNodes();
            for (int i = 0; i < items.getLength(); i++) {
                Node cureentchild = items.item(i);
                if (cureentchild.getNodeName().equalsIgnoreCase("item")) {
                    FeedItem item = new FeedItem();
                    NodeList itemchilds = cureentchild.getChildNodes();
                    for (int j = 0; j < itemchilds.getLength(); j++) {
                        Node cureent = itemchilds.item(j);
                        if (cureent.getNodeName().equalsIgnoreCase("title")) {
                            item.setTitle(cureent.getTextContent());
                        } else if (cureent.getNodeName().equalsIgnoreCase("description")) {
                            item.setDescription(cureent.getTextContent());
                        } else if (cureent.getNodeName().equalsIgnoreCase("pubDate")) {
                            Date pubDate = new Date(cureent.getTextContent());
                            SimpleDateFormat sdf = new SimpleDateFormat();
                            sdf = new SimpleDateFormat("dd MMMM yyyy");
                            String pubDateString = sdf.format(pubDate);
                            item.setPubDate(pubDateString);
                        } else if (cureent.getNodeName().equalsIgnoreCase("link")) {
                            item.setLink(cureent.getTextContent());
                        } else if (cureent.getNodeName().equalsIgnoreCase("media:thumbnail")) {
                            //this will return us thumbnail url
                            String lol = cureent.getAttributes().item(1).getTextContent();
                            lol = lol.replace("s72-c", "s500-c");
                            item.setThumbnailUrl(lol);
                        } else if (cureent.getNodeName().equalsIgnoreCase("category")) {
                            item.setCategories(cureent.getTextContent());
                        }
                    }
                    feedItems.add(item);
                }
            }
        }
    }

    //This method will download rss feed document from specified url
    public Document Getdata() {
        try {
            url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDoc = builder.parse(inputStream);
            return xmlDoc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(Observable observable, Object data) {
        articles = (ArrayList<FeedItem>) data;
        onComplete.onTaskCompleted(articles);
    }
}