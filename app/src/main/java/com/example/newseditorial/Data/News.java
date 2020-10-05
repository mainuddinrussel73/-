package com.example.newseditorial.Data;

import java.util.ArrayList;

public class News extends ArrayList<News> implements Comparable<News> {
    int ID;


    String TITLE;
    String BODY;
    String URL;

    @Override
    public String toString() {
        return "News{" +
                "ID=" + ID +
                ", TITLE='" + TITLE + '\'' +
                ", URL='" + URL + '\'' +
                ", LANG='" + LANG + '\'' +
                ", ISREAD=" + ISREAD +
                '}';
    }

    public String getLANG() {
        return LANG;
    }

    public void setLANG(String LANG) {
        this.LANG = LANG;
    }

    String LANG;

    public int getISREAD() {
        return ISREAD;
    }

    public void setISREAD(int ISREAD) {
        this.ISREAD = ISREAD;
    }

    int ISREAD;

    public News(int id, String title, String body,String url,String lang) {
        this.ID = id;
        this.TITLE = title;
        this.URL = url;
        this.BODY = body;
        this.LANG = lang;

    }

    public News() {

    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTITLE() {
        return TITLE;
    }

    public void setTITLE(String TITLE) {
        this.TITLE = TITLE;
    }

    public String getBODY() {
        return BODY;
    }

    public void setBODY(String BODY) {
        this.BODY = BODY;
    }

    @Override
    public int compareTo(News o) {
        return this.getID() - (o.getID());
    }

}
