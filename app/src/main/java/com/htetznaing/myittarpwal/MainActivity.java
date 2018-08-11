package com.htetznaing.myittarpwal;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.htetznaing.myittarpwal.Utils.CheckInternet;
import com.htetznaing.myittarpwal.RssReader.FeedItem;
import com.htetznaing.myittarpwal.RssReader.ReadRss;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    FeedsAdapter adapter;
    String rss = "https://footballwinvip.blogspot.com/feeds/posts/default?alt=rss&max-results=20";
    String currentURL;
    CheckInternet checkInternet;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar progressBar;
    AdRequest adRequest;
    AdView banner;
    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpannableString s = new SpannableString(getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "yoeyar.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
        checkInternet = new CheckInternet(MainActivity.this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        progressBar = findViewById(R.id.progressBar);
        mSwipeRefreshLayout = findViewById(R.id.container);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.canChildScrollUp();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                try {
                    adapter.clearData();
                    adapter.notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(true);
                    loadFeed(currentURL);
                }catch (Exception e){

                }

            }
        });
        //Call Read rss asyntask to fetch rss
        loadFeed(rss);

        adRequest = new AdRequest.Builder().build();
        banner = findViewById(R.id.adView);
        banner.loadAd(adRequest);
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3140510757586345/1272867699");
        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                loadAD();
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                loadAD();
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdOpened() {
                loadAD();
                super.onAdOpened();
            }
        });
    }

    public void loadAD(){
        if (!interstitialAd.isLoaded()){
            interstitialAd.loadAd(adRequest);
        }
    }

    public void showAD(){
        if (interstitialAd.isLoaded()){
            interstitialAd.show();
        }else{
            interstitialAd.loadAd(adRequest);
        }
    }

    public void loadFeed(String url){
        currentURL = url;
        if (checkInternet.isInternetOn()==true){
            loadRSS(url);
        }else{
            Toast.makeText(this, "Please Connect Internet!", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadRSS(final String url){
        ReadRss readRss = new ReadRss(url);
        readRss.execute();
        readRss.onFinish(new ReadRss.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<FeedItem> list) {
                adapter = new FeedsAdapter(MainActivity.this, list);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Unable to load data.",
                                Toast.LENGTH_LONG).show();
                        Log.i("Unable to load ", "articles");
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Attention!")
                .setMessage("Do you want to exit ?")
                .setIcon(R.drawable.icon)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showAD();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showAD();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("About App");
            builder.setMessage("Version : 1.2");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showAD();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }

        if (id == R.id.reload){
            String lol = currentURL.replaceAll("\\D+","");
            int i = Integer.parseInt(lol);
            i +=i;
            currentURL = currentURL.replace(lol,String.valueOf(i));
            loadFeed(currentURL);
            progressBar.setVisibility(View.VISIBLE);
            showAD();
        }

        return super.onOptionsItemSelected(item);
    }

    public class TypefaceSpan extends MetricAffectingSpan {
        /** An <code>LruCache</code> for previously loaded typefaces. */
        private LruCache<String, Typeface> sTypefaceCache =
                new LruCache<String, Typeface>(12);

        private Typeface mTypeface;
        public TypefaceSpan(Context context, String typefaceName) {
            mTypeface = sTypefaceCache.get(typefaceName);

            if (mTypeface == null) {
                mTypeface = Typeface.createFromAsset(context.getAssets(), typefaceName);

                // Cache the loaded Typeface
                sTypefaceCache.put(typefaceName, mTypeface);
            }
        }

        @Override
        public void updateMeasureState(TextPaint p) {
            p.setTypeface(mTypeface);

            // Note: This flag is required for proper typeface rendering
            p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            tp.setTypeface(mTypeface);

            // Note: This flag is required for proper typeface rendering
            tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
    }
}
