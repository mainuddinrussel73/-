package com.example.newseditorial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.newseditorial.Adapter.online_adapter;
import com.example.newseditorial.Data.News;
import com.example.newseditorial.DataBase.DBNewsHelper;
import com.example.newseditorial.Utill.PopUpClass;
import com.example.newseditorial.Utill.PopUpIn;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class news_online extends AppCompatActivity {

    public static List<News> newsList = new ArrayList<>();
    ListView list;
    online_adapter adapter;
    private DBNewsHelper mDBHelper;
    private ShimmerFrameLayout mShimmerViewContainer;
    Context context;

    public static List<String> extractUrls(String text)
    {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_news_online);

        try {
            Picasso.Builder builder = new Picasso.Builder(this);
            builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setIndicatorsEnabled(true);
            built.setLoggingEnabled(true);
            Picasso.setSingletonInstance(built);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        mDBHelper = new DBNewsHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        context = getBaseContext();
        if(newsList.size()>0){

            adapter = new online_adapter(news_online.this);

            list = findViewById(R.id.list);
            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    int pos = parent.getPositionForView(view);
                    // Toast.makeText(getContext(),pos+"",Toast.LENGTH_SHORT).show();

                    Context wrapper = new ContextThemeWrapper(news_online.this, R.style.YOURSTYLE1);
                    SharedPreferences prefs = news_online.this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
                    boolean isDark = prefs.getBoolean("isDark", false);
                    if (isDark) {
                        wrapper = new ContextThemeWrapper(news_online.this, R.style.YOURSTYLE);

                    } else {
                        wrapper = new ContextThemeWrapper(news_online.this, R.style.YOURSTYLE1);
                    }



                    PopupMenu popup = new PopupMenu(wrapper, view);
                    popup.getMenuInflater().inflate(R.menu.pop_up_editorial, popup.getMenu());
                    //
                    popup.show();


                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getTitle().equals("Add")){
                                Display display = getWindowManager().getDefaultDisplay();
                                int width = display.getWidth();

                                String dataq = "<html><head><meta name=\"viewport\"\"content=\"width="+width+" height="+width+ ", initial-scale=1 \" /></head>";
                                dataq = dataq + "<body>"+ newsList.get(position).getBODY() +"</body></html>";

                                String stringToAdd = "width=\"100%\" ";

                                // Create a StringBuilder to insert string in the middle of content.
                                StringBuilder sb = new StringBuilder(dataq);

                                int i = 0;
                                int cont = 0;

                                // Check for the "src" substring, if it exists, take the index where
                                // it appears and insert the stringToAdd there, then increment a counter
                                // because the string gets altered and you should sum the length of the inserted substring
                                while(i != -1){
                                    i = dataq.indexOf("src", i + 1);
                                    if(i != -1) sb.insert(i + (cont * stringToAdd.length()), stringToAdd );
                                    ++cont;
                                }




                                boolean b;
                                final Intent intent = getIntent();
                                String type = intent.getStringExtra("lang");

                                if (newsList.get(position).getURL().isEmpty()) {
                                    b = mDBHelper.insertData(newsList.get(position).getTITLE(), dataq, "empty",type);
                                } else
                                    b = mDBHelper.insertData(newsList.get(position).getTITLE(), dataq, newsList.get(position).getURL(),type);
                                if (b == true) {
                                    Toasty.success(getApplicationContext(), "Done.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toasty.error(getApplicationContext(), "opps.", Toast.LENGTH_SHORT).show();
                                }
                            }else if(item.getTitle().equals("Detail")){
                                //float density=news_online.this.getResources().getDisplayMetrics().density;
                                //LayoutInflater inflater = (LayoutInflater) news_online.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                //View layout = inflater.inflate(R.layout.popupdetail,null);
                                // final PopupWindow pw = new PopupWindow(layout, (int)density*370, (int)density*685, true);

                               /* ((TextView)layout.findViewById(R.id.goldName)).setText(newsList.get(position).BODY);
                                ((TextView)layout.findViewById(R.id.goldName)).setMovementMethod(new ScrollingMovementMethod());
                                ((TextView)layout.findViewById(R.id.goldNamet)).setText(newsList.get(position).TITLE);
                                ((TextView)layout.findViewById(R.id.goldNamet)).setMovementMethod(new ScrollingMovementMethod());
                                pw.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                pw.setTouchInterceptor(new View.OnTouchListener() {
                                    public boolean onTouch(View v, MotionEvent event) {
                                        if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                                            pw.dismiss();
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                                pw.setOutsideTouchable(true);
                                pw.showAtLocation(layout, Gravity.CENTER, 0, 0);*/

                            }
                            return true;
                        }
                    });

                }
            });
        }else {
            Toasty.info(news_online.this,"Swip to load",Toast.LENGTH_LONG).show();
        }


        final Intent intent = getIntent();
        String type = intent.getStringExtra("tag");
        System.out.println(type);

        ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo != null) {
            if (netInfo.isConnected()) {
                new RetrieveFeedTask().execute(type);
            }
        }else{
            Toasty.error(news_online.this,"No internet connection.", Toast.LENGTH_LONG).show();
        }

        SharedPreferences prefs = getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("isDark", false);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        if(isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.black));
            toolbar.setTitleTextColor(Color.WHITE);
        }else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            toolbar.setTitleTextColor(Color.WHITE);
        }
        if (isDark && newsList.size() != 0) {

            ConstraintLayout constraintLayout = findViewById(R.id.content_editsre);
            LinearLayout linearLayout = findViewById(R.id.editlistview);
            constraintLayout.setBackgroundColor(Color.BLACK);
            linearLayout.setBackgroundColor(Color.BLACK);
            list.setBackgroundColor(Color.BLACK);
            list.setAdapter(adapter);
        } else if (!isDark && newsList.size() != 0) {

            ConstraintLayout constraintLayout = findViewById(R.id.content_editsre);
            LinearLayout linearLayout = findViewById(R.id.editlistview);
            constraintLayout.setBackgroundColor(Color.WHITE);
            linearLayout.setBackgroundColor(Color.WHITE);
            list.setAdapter(adapter);

            list.setBackgroundColor(Color.WHITE);
        } else if (isDark && newsList.size() == 0) {

            ConstraintLayout constraintLayout = findViewById(R.id.content_editsre);
            LinearLayout linearLayout = findViewById(R.id.editlistview);

            constraintLayout.setBackgroundColor(Color.BLACK);
            linearLayout.setBackgroundColor(Color.BLACK);

        } else if (!isDark && newsList.size() == 0) {
            ConstraintLayout constraintLayout = findViewById(R.id.content_editsre);
            LinearLayout linearLayout = findViewById(R.id.editlistview);

            constraintLayout.setBackgroundColor(Color.WHITE);
            linearLayout.setBackgroundColor(Color.WHITE);


        }



    }

    class RetrieveFeedTask extends AsyncTask<String, Void, Boolean> {

        private Exception exception;

        @Override
        public void onPreExecute(){
            mShimmerViewContainer.startShimmer();
            mShimmerViewContainer.setVisibility(View.VISIBLE);
        }


        public Boolean doInBackground(String... urls) {
            if(urls[0].equals("p")){
                try {
                    newsList.clear();


                    Document doc = Jsoup.connect("https://www.prothomalo.com/opinion/article/").get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    for (Element link : links) {
                        if(!elements.contains(link) && link.attr("href").contains("/opinion/article/") && !link.attr("href").contains("?page")){
                            elements.add(link);
                        }else{
                            elements.remove(link);
                        }
                    }

                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());

                        //docs.outputSettings(new Document.OutputSettings().prettyPrint(false));


                        Elements _ContentRegion =  docs.select("div[itemprop=articleBody]");
                        Elements image = docs.select("div.col_in").select("img");
                        String url;
                        if(image.size()>0){
                            url = image.first().absUrl("src");
                        }else{
                            url = "https://is4-ssl.mzstatic.com/image/thumb/Purple118/v4/bf/c4/1b/bfc41bb3-3e16-89f8-8ae8-f8207cb41e92/source/512x512bb.jpg";
                        }

                        news.setURL(url);
                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                        }
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);
                        //   System.out.println(news.TITLE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("i")){

                newsList.clear();
                try {

                    Document doc1 = Jsoup.connect("https://www.ittefaq.com.bd/opinion").get();
                    Elements links1 = doc1.select("a[href]");
                    List<Element> elements1 = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links1) {
                        if(!elements1.contains(link) && link.attr("href").contains("/opinion/") && !link.attr("href").contains("?page")){
                            elements1.add(link);
                            //System.out.println(link.attr("abs:href"));
                        }else{
                            elements1.remove(link);
                        }
                    }

                    for (Element link:
                            elements1) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());


                        String url;
                        Element image = docs.select("div[class=dtl_img_block relative]").select("img").first();
                        if(image==null){
                            url = "https://www.ittefaq.com.bd/templates/desktop-v1/images/news-logo.jpg";
                        }
                        else{
                            url = image.absUrl("src");
                        }

                        news.setURL(url);

                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        //select all <br> tags and append \n after that
                        docs.select("br").after("\\n");
                        //select all <p> tags and prepend \n before that
                        docs.select("p").before("\\n");
                        Elements _ContentRegion =  docs.getElementById("dtl_content_block").children();


                        news.setURL(url);
                        StringBuilder ss = new StringBuilder();
                        for (int i = 2; i<_ContentRegion.size(); i++){
                            ss.append(_ContentRegion.get(i).wholeText());
                            ss.append("\n");
                        }
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);
                    }

                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("dailystar")){
                newsList.clear();

                try {

                    Document doc = Jsoup.connect("https://www.thedailystar.net/opinion").userAgent("Mozila/5.0").timeout(3000).get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {
                        if(!elements.contains(link) &&
                                (link.attr("href").contains("/opinion/politics/")
                                        ||link.attr("href").contains("/opinion/human-rights/")
                                        ||link.attr("href").contains("/opinion/perspective/")
                                        ||link.attr("href").contains("/opinion/project-syndicate/")
                                        ||link.attr("href").contains("/opinion/economics/")
                                )
                                && !link.attr("href").contains("?page")){
                            elements.add(link);
                            //System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    Document doc1 = Jsoup.connect("https://www.thedailystar.net/editorial").userAgent("Mozila/5.0").timeout(3000).get();
                    Elements links1 = doc1.select("a[href]");
                    //List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links1) {
                        if(!elements.contains(link) && link.attr("href").contains("/editorial/") && !link.attr("href").contains("?page")){
                            elements.add(link);
                            //System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());

                        Element image = docs.getElementsByClass("panel-pane pane-image no-title block").select("img").first();
                        String url = image.absUrl("src");
                        news.setURL(url);

                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        //select all <br> tags and append \n after that
                        docs.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.select("div.node-content");

                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                        }
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);
                    }

                    // In case of any IO errors, we want the messages written to the console
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            else if(urls[0].equals("jugantor")){

                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://www.jugantor.com/todays-paper").get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {
                        if(link.attr("href").contains("/todays-paper/editorial/") || link.attr("href").contains("/todays-paper/sub-editorial/")){
                            elements.add(link);
                            // System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());
                        //Elements _ContentRegions =  docs.select("img.image-style-very-big-1");

                        Element image = docs.getElementsByClass("dtl_img_section post_template-0").select("img").first();
                        String url = image.absUrl("src");
                        news.setURL(url);

                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        //select all <br> tags and append \n after that
                        docs.select("br").after("\n");
                        //select all <p> tags and prepend \n before that
                        docs.select("p").before("\n");
                        docs.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.select("div#myText");


                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                            ss.append("\n");
                        }
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);
                    }



                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("janakantho")){

                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://www.dailyjanakantha.com/editorial/").get();
                    Elements links = doc.select("div[class=col-12 col-md-9]").select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {
                        if(link.attr("href").contains("/details/article/")){
                            elements.add(link);
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        News news = new News();
                        news.setTITLE(docs.title());
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        //Elements _ContentRegions =  docs.select("img.image-style-very-big-1");

                        news.setURL("https://www.dailyjanakantha.com/cloud-uploads/default/header-top.png");
                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        docs.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.select("div[class=article-details mb-5] p");


                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                            ss.append("\n\n");
                        }
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);

                    }



                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("aljazeera")){
                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://www.aljazeera.com/indepth/opinion/").get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {
                        if(link.attr("href").contains("/indepth/opinion/") && link.attr("href").contains(".html") && !link.attr("href").contains("/indepth/opinion/profile/") ){
                            elements.add(link);
                            System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).userAgent("Mozila").get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());
                        //Elements _ContentRegions =  docs.select("img.image-style-very-big-1");

                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        //select all <br> tags and append \n after that
                        docs.select("br").after("\n");
                        //select all <p> tags and prepend \n before that
                        docs.select("p").before("\n");
                        docs.outputSettings().prettyPrint(true);
                        docs.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.select("div[class=article-p-wrapper]");

                        Element image =  docs.select("div.main-article-body img").first();

                        news.setURL(image.absUrl("src"));

                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                            ss.append("\n");
                        }
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);

                    }

                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("bdpratidin")){
                try {

                    Document doc = Jsoup.connect("https://www.bd-pratidin.com/editorial").get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    String reg = "editorial/([0-9]{4})/([0-9]{2})/([0-9]{2})/(.*)";
                    for (Element link : links) {

                        if(Pattern.matches(reg,link.attr("href")) ){
                            elements.add(link);
                            System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).userAgent("Mozila").get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());
                        // System.out.println(docs.title());
                        //Elements _ContentRegions =  docs.select("img.image-style-very-big-1");

                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        //select all <br> tags and append \n after that
                        docs.select("br").after("\n");
                        //select all <p> tags and prepend \n before that
                        docs.select("p").before("\n");
                        docs.outputSettings().prettyPrint(true);
                        docs.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.getElementsByTag("article");

                        Element image =  docs.select("div[class=main-image] img").first();



                        if(image==null){
                            // System.out.println("https://www.bd-pratidin.com/assets/importent_images/og-logo.jpg");
                            news.setURL("https://www.bd-pratidin.com/assets/importent_images/og-logo.jpg");
                        }
                        else {
                            //System.out.println(image.absUrl("src"));
                            news.setURL(image.absUrl("src"));
                        }

                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                            ss.append("\n");
                        }
                        news.setBODY(ss.toString().trim());
                        System.out.println(ss.toString().trim());
                        newsList.add(news);

                    }

                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("washpost")){
                try {

                    Document doc = Jsoup.connect("https://www.washingtonpost.com/global-opinions/").get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    String reg = "(.*)/opinions/([0-9]{4})/([0-9]{2})/([0-9]{2})/(.*)";
                    for (Element link : links) {

                        if(Pattern.matches(reg,link.attr("href")) ){
                            elements.add(link);
                            //System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).userAgent("Mozila").get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());
                        //System.out.println(docs.title());
                        //Elements _ContentRegions =  docs.select("img.image-style-very-big-1");

                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        //select all <br> tags and append \n after that
                        docs.select("br").after("\n");
                        //select all <p> tags and prepend \n before that
                        docs.select("p").before("\n");
                        docs.outputSettings().prettyPrint(true);
                        docs.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.select("div[class=article-body]");

                        Element image =  docs.select("figure[class=center mb-md ml-neg-gutter mr-neg-gutter ml-auto-ns mr-auto-ns  hide-for-print] img").first();

                        try{
                            news.setURL(extractUrls(image.absUrl("src")).get(0).replace("=150","=1023"));
                        }catch (Exception e){

                        }
                        // System.out.println(extractUrls(image.absUrl("src")).get(0));
                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                            ss.append("\n");
                        }
                        news.setBODY(ss.toString().trim());
                        //System.out.println(ss.toString().trim());
                        newsList.add(news);

                    }

                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("foreignpolicy")){
                try {

                    Document doc = Jsoup.connect("https://foreignpolicy.com/channel/analysis/").get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    String reg = "(.*)/([0-9]{4})/([0-9]{2})/([0-9]{2})/(.*)";
                    for (Element link : links) {

                        if(Pattern.matches(reg,link.attr("href")) ){
                            elements.add(link);
                            // System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).userAgent("Mozila").get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());
                        // System.out.println(doc.title());
                        //Elements _ContentRegions =  docs.select("img.image-style-very-big-1");

                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        //select all <br> tags and append \n after that
                        docs.select("br").after("\n");
                        //select all <p> tags and prepend \n before that
                        docs.select("p").before("\n");
                        docs.outputSettings().prettyPrint(true);
                        docs.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.select("div[class=post-content-main initial-drop-cap shares-position]");

                        Element image =  docs.select("figure[class=figure-image] img").first();

                        news.setURL(extractUrls(image.absUrl("data-srcset")).get(0));
                        //System.out.println(extractUrls(image.absUrl("data-srcset")).get(0));
                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                            ss.append("\n");
                        }
                        news.setBODY(ss.toString().trim());
                        // System.out.println(ss.toString().trim());
                        newsList.add(news);

                    }

                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("nayadiganta")){

                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://www.dailynayadiganta.com/editorial/9").get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {
                        if(!link.attr("abs:href").contains("https://www.dailynayadiganta.com/editorial/9") && link.attr("href").contains("/sub-editorial/")){
                            elements.add(link);
                            //System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    Document doc1 = Jsoup.connect("https://www.dailynayadiganta.com/opinion/10").get();
                    Elements links1 = doc1.select("a[href]");
                    // List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links1) {
                        if(!link.attr("abs:href").contains("https://www.dailynayadiganta.com/opinion/10") && link.attr("href").contains("/opinion/")){
                            elements.add(link);
                            //System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }


                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());

                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));

                        docs.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.select("div[class=news-content]");

                        Element image =  docs.select("div[class=image-holder margin-bottom-10]").first();

                        if(image==null){
                            news.setURL("https://www.dailynayadiganta.com/resources/img/sitesetup/1_1.png");
                        }else{
                            news.setURL(image.getElementsByTag("img").attr("abs:src"));
                        }




                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                        }
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);
                    }

                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("dailysun")){
                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://www.daily-sun.com/printversion/type/editorial").userAgent("Mozila").get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {
                        if(!elements.contains(link) && link.attr("href").contains("/printversion/details/") && !link.attr("href").contains("/Quote-of-the-day")){
                            elements.add(link);
                            //System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        News news = new News();
                        news.setTITLE(docs.title());

                        Element image = docs.select("img[class=main_img]").first();
                        String url;
                        if(image==null){
                            url = "https://devo-tech.com/wp-content/uploads/2018/05/Daily_Sun_Bd.png";
                        }else{
                            url = image.absUrl("src");
                        }
                        news.setURL(url);

                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        //select all <br> tags and append \n after that
                        docs.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.getElementsByTag("article");

                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                        }
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);

                        // newsList.add(news);
                    }

                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("inquilab")){
                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://www.dailyinqilab.com/newscategory/editors").get();
                    Elements links = doc.select("div[class=row news_list]").select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {
                        if(!elements.contains(link) && link.attr("href").contains("/article/") ){
                            elements.add(link);
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();

                        News news = new News();
                        news.setTITLE(docs.title());

                        Element image = docs.select("div[class=image_block]").select("img").first();

                        String url;
                        if(image==null){
                            url="https://www.dailyinqilab.com/news_original/1574777349_editorial-inq.jpg";
                        }else {
                            url = image.absUrl("src");
                        }
                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        //select all <br> tags and append \n after that
                        docs.select("br").after("\\n");
                        //select all <p> tags and prepend \n before that
                        docs.select("p").before("\\n");
                        doc.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.getElementById("ar_news_content").children();


                        news.setURL(url);

                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                        }
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);

                        //newsList.add(news);
                    }



                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("kalerkantho")){
                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://www.kalerkantho.com/print-edition/editorial").get();
                    Elements links = doc.select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {
                        if(!elements.contains(link) && link.attr("abs:href").contains("https://www.kalerkantho.com/print-edition/editorial/") ){
                            elements.add(link);
                            // System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    Document doc1 = Jsoup.connect("https://www.kalerkantho.com/print-edition/sub-editorial").get();
                    Elements links1 = doc1.select("a[href]");
                    //Iterate links and print link attributes.
                    for (Element link : links1) {
                        if(!elements.contains(link) && link.attr("abs:href").contains("https://www.kalerkantho.com/print-edition/sub-editorial/") ){
                            elements.add(link);
                            //System.out.println(link.attr("abs:href"));
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();

                        News news = new News();
                        news.setTITLE(docs.title());
                        //System.out.println("Title : "+docs.title());

                        Element image = docs.select("div[class=img-popup]").select("img").first();

                        String url;
                        if(image==null){
                            url="https://www.bkash.com/sites/default/files/Kaler%20Kontho%20logo.jpg";
                        }else {
                            url = image.absUrl("src");
                        }
                        docs.outputSettings(new Document.OutputSettings().prettyPrint(false));
                        doc.outputSettings().prettyPrint(true);
                        Elements _ContentRegion =  docs.getElementsByClass("some-class-name2");


                        //System.out.println("Url : "+url);
                        news.setURL(url);

                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                        }
                        //System.out.println("Body : "+ ss.toString());
                        news.setBODY(ss.toString().trim());

                        newsList.add(news);

                        //newsList.add(news);
                    }



                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("songbad")){
                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://thesangbad.net/opinion/open-discussion/").get();
                    Elements links = doc.select("div[class=left-page-contents]").select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {

                        if(link.attr("href").contains("/opinion/open-discussion/")){
                            elements.add(link);
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        News news = new News();
                         news.setTITLE(docs.title());
                        // System.out.println(docs.html());
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        Element image =  docs.select("div[class=news-details] img").first();

                        if(image==null){
                             news.setURL("https://m.thesangbad.net/images/logo.png");
                        }
                        else {
                            news.setURL(image.absUrl("src"));
                        }

                        Elements _ContentRegion =  docs.select("div[class=news-details] p");


                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                            ss.append("\n\n");
                        }
                         news.setBODY(ss.toString().trim());

                        newsList.add(news);

                    }



                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("songram")){
                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://dailysangram.com/section/opinion").get();
                    Elements links = doc.select("div[class=homeContent]").select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {

                        if(link.attr("href").contains("/post/")){
                            elements.add(link);
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        News news = new News();
                        news.setTITLE(docs.title());
                        // System.out.println(docs.html());
                        System.out.println(docs.title());
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        Element image =  docs.select("div[class=news-details] img").first();

                        if(image==null){
                            // System.out.println("https://www.bd-pratidin.com/assets/importent_images/og-logo.jpg");
                             news.setURL("https://allbanglanewspaper.net/upload/2019/03/639661-the-daily-sangram.jpg");
                        }
                        else {
                            news.setURL(image.absUrl("src"));
                        }

                        // news.setURL("https://www.dailyjanakantha.com/files/201701/1485886687_JK-LOGO.jpg");

                        Elements _ContentRegion =  docs.select("div[class=postDetails] p");


                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                            ss.append("\n\n");
                        }
                        //System.out.println(ss.toString().trim());
                         news.setBODY(ss.toString().trim());

                        newsList.add(news);

                    }



                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(urls[0].equals("nyt")){
                newsList.clear();
                try {

                    Document doc = Jsoup.connect("https://www.nytimes.com/section/opinion/editorials").get();
                    Elements links = doc.select("div[class=css-13mho3u]").select("a[href]");
                    List<Element> elements = new ArrayList<>();
                    //Iterate links and print link attributes.
                    for (Element link : links) {

                        if(link.attr("href").contains("/opinion/")){
                            elements.add(link);
                        }else{
                            elements.remove(link);
                        }
                    }
                    for (Element link:
                            elements) {
                        Document docs = Jsoup.connect(link.attr("abs:href")).get();
                        News news = new News();
                         news.setTITLE(docs.title());
                        // System.out.println(docs.html());
                        System.out.println(docs.title());
                        // With the document fetched, we use JSoup's title() method to fetch the title
                        Element image =  docs.select("div[class=css-1a48zt4 ehw59r15] img").first();

                        if(image==null){
                            // System.out.println("https://www.bd-pratidin.com/assets/importent_images/og-logo.jpg");
                             news.setURL("https://www.bd-pratidin.com/assets/importent_images/og-logo.jpg");
                        }
                        else {
                            news.setURL(image.absUrl("src"));
                        }

                        // news.setURL("https://www.dailyjanakantha.com/files/201701/1485886687_JK-LOGO.jpg");

                        Elements _ContentRegion =  docs.select("section[class=meteredContent css-1r7ky0e] p");


                        StringBuilder ss = new StringBuilder();
                        for (Element ee : _ContentRegion){
                            ss.append(ee.wholeText());
                            ss.append("\n\n");
                        }
                          news.setBODY(ss.toString().trim());

                        newsList.add(news);

                    }



                    // In case of any IO errors, we want the messages written to the console
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void onPostExecute(Boolean b) {
            // TODO: check this.exception
            // TODO: do something with the feed



            HashSet<Object> seen=new HashSet<>();
            newsList.removeIf(e->!seen.add(e.getTITLE()));

            adapter = new online_adapter(news_online.this);

            list = findViewById(R.id.list);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                    int pos = parent.getPositionForView(view);
                    // Toast.makeText(getContext(),pos+"",Toast.LENGTH_SHORT).show();

                    Context wrapper = new ContextThemeWrapper(news_online.this, R.style.YOURSTYLE1);
                    SharedPreferences prefs = news_online.this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
                    boolean isDark = prefs.getBoolean("isDark", false);
                    if (isDark) {
                        wrapper = new ContextThemeWrapper(news_online.this, R.style.YOURSTYLE);

                    } else {
                        wrapper = new ContextThemeWrapper(news_online.this, R.style.YOURSTYLE1);
                    }

                    PopupMenu popup = new PopupMenu(wrapper, view);
                    popup.getMenuInflater().inflate(R.menu.pop_up_editorial, popup.getMenu());
                    //
                    popup.show();


                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getTitle().equals("Add")){
                                Display display = getWindowManager().getDefaultDisplay();
                                int width = display.getWidth();

                                String dataq = "<html><head><meta name=\"viewport\"\"content=\"width=" + width + " height=" + width + ", initial-scale=1 \" />" +
                                        "</head>";
                                dataq = dataq + "<body>"+ newsList.get(position).getBODY() +"</body></html>";

                                String stringToAdd = "width=\"100%\" ";

                                // Create a StringBuilder to insert string in the middle of content.
                                StringBuilder sb = new StringBuilder(dataq);

                                int i = 0;
                                int cont = 0;

                                // Check for the "src" substring, if it exists, take the index where
                                // it appears and insert the stringToAdd there, then increment a counter
                                // because the string gets altered and you should sum the length of the inserted substring
                                while(i != -1){
                                    i = dataq.indexOf("src", i + 1);
                                    if(i != -1) sb.insert(i + (cont * stringToAdd.length()), stringToAdd );
                                    ++cont;
                                }




                                boolean b;
                                final Intent intent = getIntent();
                                String type = intent.getStringExtra("lang");
                                if (newsList.get(position).getURL().isEmpty()) {
                                    b = mDBHelper.insertData(newsList.get(position).getTITLE(), dataq, "empty",type);
                                } else
                                    b = mDBHelper.insertData(newsList.get(position).getTITLE(), dataq, newsList.get(position).getURL(),type);
                                if (b == true) {
                                    Toasty.success(getApplicationContext(), "Done.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toasty.error(getApplicationContext(), "opps.", Toast.LENGTH_SHORT).show();
                                }

                                //list.setAdapter(adapter);
                            }else if(item.getTitle().equals("Detail")){


                                ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
                                PopUpClass popUpClass = new PopUpClass(root);
                                popUpClass.showPopupWindow(context,view,newsList.get(position).getTITLE(),newsList.get(position).getBODY());



                            }
                            return true;
                        }
                    });

                }
            });


            mShimmerViewContainer.stopShimmer();
            mShimmerViewContainer.setVisibility(View.GONE);

        }
    }
}
