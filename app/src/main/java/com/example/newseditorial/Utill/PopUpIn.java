package com.example.newseditorial.Utill;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.view.ActionMode;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.newseditorial.Data.News;
import com.example.newseditorial.DataBase.DBNewsHelper;
import com.example.newseditorial.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import es.dmoral.toasty.Toasty;

import static android.content.Context.CLIPBOARD_SERVICE;

public class  PopUpIn {

    //PopupWindow display method

    ViewGroup root;
    DBNewsHelper mDBHelper;
    int width;
    Context Coontext;

    public PopUpIn(ViewGroup r) {
        this.root = r;
    }

    public static void applyDim(@NonNull ViewGroup parent, float dimAmount) {
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    public static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    public void showPopupWindow(Context context, final View view) {


        //Create a View object yourself through inflater
        mDBHelper = new DBNewsHelper(context);
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up_take, null);

        //Specify the length and width through constants

        applyDim(root, 0.5f);

        width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;
        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


        //Initialize the elements of our window, install the handler
        Coontext = context;

        EditText test2 = popupView.findViewById(R.id.goldNamet);
        test2.setTextIsSelectable(true);
        test2.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }


            public void onDestroyActionMode(ActionMode mode) {
            }


            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        test2.setMovementMethod(new ScrollingMovementMethod());

        Button button = popupView.findViewById(R.id.done);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //As an example, display the message
                clearDim(root);
                popupWindow.dismiss();

                ClipboardManager manager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData pasteData = manager.getPrimaryClip();
                ClipData.Item item = pasteData.getItemAt(0);
                String paste = item.getText().toString();
                test2.setText(paste);
                new RetrieveFeedTask().execute(test2.getText().toString().trim());



            }
        });
        ImageButton imageButton = popupView.findViewById(R.id.messageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                clearDim(root);

            }
        });


        LinearLayout relativeLayout = popupView.findViewById(R.id.nnmnm);
        SharedPreferences prefs = context.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("isDark", false);
        if (isDark) {
            relativeLayout.setBackgroundColor(Color.BLACK);
            test2.setTextColor(Color.WHITE);
        } else {
            relativeLayout.setBackgroundColor(Color.WHITE);
            test2.setTextColor(Color.BLACK);
        }


        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Close the window when clicked
                //popupWindow.dismiss();

                return true;
            }
        });
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, Boolean> {

        private Exception exception;
        News news = new News();


        @Override
        public void onPreExecute() {

        }


        public Boolean doInBackground(String... urls) {
            Document docs = null;
            try {
                docs = Jsoup.connect(urls[0].trim()).get();
            } catch (IOException e) {
                e.printStackTrace();
            }



            news.setTITLE(docs.title());

            // System.out.println(docs.body());
            Elements _ContentRegion = docs.select("div[class=story-element story-element-text]");
            Elements image = docs.select("div[class=left-align  bn-story-element]").select("img");
            String url;
            if (image.size() > 0) {
                url = image.first().absUrl("src");
            } else {
                url = "https://is4-ssl.mzstatic.com/image/thumb/Purple118/v4/bf/c4/1b/bfc41bb3-3e16-89f8-8ae8-f8207cb41e92/source/512x512bb.jpg";
            }


            news.setURL(url);
            StringBuilder ss = new StringBuilder();
            for (Element ee : _ContentRegion) {
                ss.append(ee.wholeText());

            }
            news.setBODY(ss.toString().trim());


            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void onPostExecute(Boolean b) {
            // TODO: check this.exception
            // TODO: do something with the feed


            String dataq = "<html><head><meta name=\"viewport\"\"content=\"width=" + width + " height=" + width + ", initial-scale=1 \" />" +
                    "</head>";
            dataq = dataq + "<body>" + news.getBODY() + "</body></html>";

            String stringToAdd = "width=\"100%\" ";

            // Create a StringBuilder to insert string in the middle of content.
            StringBuilder sb = new StringBuilder(dataq);

            int i = 0;
            int cont = 0;

            // Check for the "src" substring, if it exists, take the index where
            // it appears and insert the stringToAdd there, then increment a counter
            // because the string gets altered and you should sum the length of the inserted substring
            while (i != -1) {
                i = dataq.indexOf("src", i + 1);
                if (i != -1) sb.insert(i + (cont * stringToAdd.length()), stringToAdd);
                ++cont;
            }

            boolean b1;
            if (news.getURL().isEmpty()) {
                b1 = mDBHelper.insertData(news.getTITLE(), dataq, "empty",news.getLANG());
            } else
                b1 = mDBHelper.insertData(news.getTITLE(), dataq, news.getURL(),news.getLANG());
            if (b1 == true) {
                Toasty.success(Coontext, "Done.", Toast.LENGTH_SHORT).show();
            } else {
                Toasty.error(Coontext, "opps.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}