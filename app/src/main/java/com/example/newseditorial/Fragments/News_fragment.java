package com.example.newseditorial.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newseditorial.Adapter.News_adapter;
import com.example.newseditorial.Data.News;
import com.example.newseditorial.DataBase.DBNewsHelper;
import com.example.newseditorial.R;
import com.example.newseditorial.add_news;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class News_fragment extends Fragment  {

    View treeView;
    public static List<News> newsList = new ArrayList<>();
    RecyclerView list;
    News_adapter adapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    int i = 0,c=5;
    //int k = 0;
    int tiken = 0;

    public  News_fragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        treeView = inflater.inflate(R.layout.fragment_news, container, false);

        final SharedPreferences prefs = getActivity().getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);

        try {
            Picasso.Builder builder = new Picasso.Builder(getContext());
            builder.downloader(new OkHttp3Downloader(getContext(),Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setIndicatorsEnabled(true);
            built.setLoggingEnabled(true);
            Picasso.setSingletonInstance(built);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }




        SharedPreferences.Editor editor;

        FloatingActionButton fab = treeView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), add_news.class);
                //String s = view.findViewById(R.id.subtitle).toString();
                //String s = (String) parent.getI;
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(myIntent, 0);
            }
        });

        DBNewsHelper mDBHelper = new DBNewsHelper(getContext());

        mShimmerViewContainer =  treeView.findViewById(R.id.shimmer_view_container);

        newsList.clear();


        SharedPreferences prefs1 =  getActivity().getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        String type = prefs1.getString("sort", "asc");
        if (type.equals("asc")) {
            tiken = 0;
        } else if (type.equals("des")) {
            tiken = 1;
        } else if (type.equals("alp")) {
            tiken = 2;
        }


        final Cursor[] cursor = {mDBHelper.getAllData12(tiken)};

        // looping through all rows and adding to list
        if (cursor[0].getCount() != 0) {
            // show message
            while (cursor[0].moveToNext()) {


                //if(i<5) {
                News word = new News();
                word.setID(Integer.parseInt(cursor[0].getString(0)));
                word.setTITLE(cursor[0].getString(1));
                word.setBODY(cursor[0].getString(2));
                word.setISREAD( cursor[0].getInt(3));
                word.setURL(cursor[0].getString(4));

                newsList.add(word);
                //  i++;
                //}else {
                //i=0;
                //}



            }

            // size = contactList.size();









        } else {

            Toasty.info(getContext(), "Nothing to show.", Toasty.LENGTH_LONG).show();
        }

        Collections.sort(newsList,Collections.reverseOrder());

        adapter = new News_adapter(getActivity(),newsList);


        list =  treeView.findViewById(R.id.news_list);

        list.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(linearLayoutManager);
        list.setAdapter(adapter);





        boolean isDark = prefs.getBoolean("isDark", false);


        RelativeLayout cardView =  treeView.findViewById(R.id.basic);
        RelativeLayout constraintLayout =  treeView.findViewById(R.id.content_newsre);
        LinearLayout linearLayout = treeView.findViewById(R.id.newslistview);
        if (isDark && newsList.size() != 0) {

            cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black));
            constraintLayout.setBackgroundColor(Color.BLACK);
            linearLayout.setBackgroundColor(Color.BLACK);
            list.setAdapter(adapter);
        } else if (!isDark && newsList.size() != 0) {
            cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

            constraintLayout.setBackgroundColor(Color.WHITE);
            linearLayout.setBackgroundColor(Color.WHITE);
            list.setAdapter(adapter);

        } else if (isDark && newsList.size() == 0) {
            cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black));


            constraintLayout.setBackgroundColor(Color.BLACK);
            linearLayout.setBackgroundColor(Color.BLACK);

        } else if (!isDark && newsList.size() == 0) {
            cardView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

            constraintLayout.setBackgroundColor(Color.WHITE);
            linearLayout.setBackgroundColor(Color.WHITE);


        }





        return treeView;

    }
}
