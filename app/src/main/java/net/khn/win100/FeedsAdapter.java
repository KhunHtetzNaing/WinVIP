package net.khn.win100;

/**
 * Created by HtetzNaing on 1/16/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import net.khn.win100.RssReader.FeedItem;

import java.util.ArrayList;

/**
 * Created by rishabh on 26-02-2016.
 */
public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.MyViewHolder> {
    ArrayList<FeedItem>feedItems;
    Context context;
    AdRequest adRequest;
    InterstitialAd interstitialAd;
    Typeface mm;
    public FeedsAdapter(Context context, ArrayList<FeedItem>feedItems){
        this.feedItems=feedItems;
        this.context=context;

        adRequest = new AdRequest.Builder().build();
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId("ca-app-pub-1325188641119577/8239119070");
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

        mm = Typeface.createFromAsset(context.getAssets(),"yoeyar.ttf");
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

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.custum_row_news_item,parent,false);
        MyViewHolder holder=new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        YoYo.with(Techniques.FadeIn).playOn(holder.cardView);
        final FeedItem current=feedItems.get(position);
        holder.Title.setText(current.getTitle());
        String date = current.getPubDate();

        holder.Title.setTypeface(mm);
        holder.date.setText(date);
        Picasso.with(context)
                .load(current.getThumbnailUrl())
                .placeholder(R.drawable.icon)
                .fit()
                .centerCrop()
                .into(holder.Thumbnail);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String LolContent = current.getDescription();
                String title = current.getTitle();

                String html = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<link rel=\"stylesheet\" href=\"file:///android_asset/mm.css\">\n" +
                        "<head>\n" +
                        "<title>" + title + "</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "  <h3>" + title + "</h3>\n" +
                        "  <br/>\n" +
                        "  \n" +
                        "" + LolContent + "\n" +
                        "<br/>\n" +
                        "<br/>" +
                        "</body>\n" +
                        "</html>";

                Intent intent = new Intent(context, Reading.class);
                intent.putExtra("content",html);
                showAD();
                context.startActivity(intent);
            }
        });
    }



    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Title,date;
        ImageView Thumbnail;
        CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            Title= (TextView) itemView.findViewById(R.id.title);
            date= (TextView) itemView.findViewById(R.id.date);
            Thumbnail= (ImageView) itemView.findViewById(R.id.image);
            cardView= (CardView) itemView.findViewById(R.id.cardView);
        }
    }

    public void clearData() {
        if (feedItems != null){
            feedItems.clear();
        }
    }
}