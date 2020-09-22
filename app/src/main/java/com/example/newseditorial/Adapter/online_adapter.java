package com.example.newseditorial.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.newseditorial.Data.News;
import com.example.newseditorial.R;
import com.example.newseditorial.news_online;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class online_adapter extends BaseAdapter {

    private final Activity context;
    List<News> newsList;
    TextView titleText, body;

    // private final Integer[] imgid;

    public online_adapter(Activity context) {

        this.context = context;
        this.newsList = new ArrayList<News>();
        this.newsList.addAll(news_online.newsList);


    }

    @Override
    public int getCount() {
        return news_online.newsList.size();
    }

    @Override
    public News getItem(int i) {
        return news_online.newsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public View getView(final int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.news_list_item, null, true);

        titleText = rowView.findViewById(R.id.news_title);
        body = rowView.findViewById(R.id.news_detail);

        LinearLayout listitm = rowView.findViewById(R.id.list_item);
        final ImageView imageView = rowView.findViewById(R.id.topnews);

        //System.out.println("klkl"+MainActivity.contactList.size());
        titleText.setText(newsList.get(position).getTITLE());


        online_adapter.RetrieveFeedTask asyncTask = new online_adapter.RetrieveFeedTask();
        String s = newsList.get(position).getBODY();
        asyncTask.execute(s);






        Picasso.with(context)
                .load(newsList.get(position).getURL())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(newsList.get(position).getURL())
                                .error(R.drawable.news)
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso","Could not fetch image");
                                    }
                                });
                    }
                });

        //LinearLayout listitem = rowView.findViewById(R.id.list_item);

        SharedPreferences prefs = context.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("isDark", false);
        FrameLayout linearLayout = rowView.findViewById(R.id.baselayout);
        if (isDark) {

            //System.out.println("klklkl");
            linearLayout.setBackgroundColor(Color.BLACK);


            titleText.setTextColor(Color.WHITE);
            body.setTextColor(Color.WHITE);
            listitm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.background_card_dark));



        } else {
            linearLayout.setBackgroundColor(Color.WHITE);
            titleText.setTextColor(Color.BLACK);
            body.setTextColor(Color.BLACK);
            listitm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.background_card));

        }


        return rowView;

    }




    class RetrieveFeedTask extends AsyncTask<String, Void, Spanned> {

        private Exception exception;

        protected Spanned doInBackground(String... data) {

            //System.out.println(data);
            return Html.fromHtml(data[0].replace("\n", "<br>"));

        }

        protected void onPostExecute(Spanned text) {
            body.setText(text);

        }
    }
}
