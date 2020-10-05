package com.example.newseditorial.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.newseditorial.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBNewsHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "news.db";
    public static final String TABLE_NAME = "news_table";
    public static final String ID = "ID";
    public static final String TITLE = "TITLE";
    public static final String BODY = "BODY";
    public static final String URL = "URL";
    public static final String ISREAD = "ISREAD";
    public static final String LANG = "LANG";
    static String DB_PATH = "";
    private SQLiteDatabase mDataBase;
    private Context mContext;
    private boolean mNeedUpdate = false;


    public DBNewsHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        copyDataBase();

        this.getReadableDatabase();
    }

    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DATABASE_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DATABASE_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {

    }

    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DATABASE_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + TITLE + " TEXT,"
                + BODY + " TEXT,"
                + ISREAD + " INTEGER,"
                + LANG + " TEXT,"
                + URL + " URL" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String title, String body, String url,String lang) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, title);
        contentValues.put(BODY, body);
        contentValues.put(LANG, lang);
        contentValues.put(URL, url);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor getAllData12(int i) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        if(i==0){
            res = db.rawQuery("select * from " + TABLE_NAME , null);
        }
        else if(i==1){
            res = db.rawQuery("select * from " + TABLE_NAME + " ORDER BY " + ID + " DESC", null);
        }
        else if(i==2){
            res = db.rawQuery("select * from " + TABLE_NAME + " ORDER BY " + TITLE + " DESC", null);
        }
        return res;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;

        res = db.rawQuery("select * from " + TABLE_NAME , null);


        return res;
    }


    public boolean updateData(String id, String old_title, String title, String body, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db1 = this.getReadableDatabase();
        try {
            Cursor re = db1.rawQuery("SELECT * FROM news_table WHERE TITLE = ?; ", new String[]{old_title});
            if (re.moveToFirst()) {
                do {
                    System.out.println(re.getString(0));
                    id = re.getString(0);
                } while (re.moveToNext());
            }

            re.close();
            // System.out.println(re.getString(0));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(TITLE, title);
        contentValues.put(BODY, body);
        contentValues.put(URL, url);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
        return true;
    }

    public boolean updateDatau(String id, String old_title, String title, String body) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db1 = this.getReadableDatabase();
        try {
            Cursor re = db1.rawQuery("SELECT * FROM news_table WHERE TITLE = ?; ", new String[]{old_title});
            if (re.moveToFirst()) {
                do {
                    System.out.println(re.getString(0));
                    id = re.getString(0);
                } while (re.moveToNext());
            }

            re.close();
            // System.out.println(re.getString(0));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(TITLE, title);
        contentValues.put(BODY, body);
        //contentValues.put(URL,url);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
        return true;
    }

    public boolean updateDataR(String id, String old_title, String title, int read) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db1 = this.getReadableDatabase();
        try {
            Cursor re = db1.rawQuery("SELECT * FROM news_table WHERE TITLE = ?; ", new String[]{old_title});
            if (re.moveToFirst()) {
                do {
                    System.out.println(re.getString(0));
                    id = re.getString(0);
                } while (re.moveToNext());
            }

            re.close();
            // System.out.println(re.getString(0));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(TITLE, title);
        // contentValues.put(BODY, body);
        contentValues.put(ISREAD,read);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
        return true;
    }

    public Integer deleteData(String id, String old_title, String title, String body, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db1 = this.getReadableDatabase();
        try {
            Cursor re = db1.rawQuery("SELECT * FROM news_table WHERE TITLE = ?; ", new String[]{old_title});
            if (re.moveToFirst()) {
                do {
                    System.out.println(re.getString(0));
                    id = re.getString(0);
                } while (re.moveToNext());
            }

            re.close();
            // System.out.println(re.getString(0));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(TITLE, title);
        contentValues.put(BODY, body);
        contentValues.put(URL, url);
        return db.delete(TABLE_NAME, "ID = ?", new String[]{id});
    }

    public Integer deleteDatau(String id, String old_title, String title, String body) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db1 = this.getReadableDatabase();
        try {
            Cursor re = db1.rawQuery("SELECT * FROM news_table WHERE TITLE = ?; ", new String[]{old_title});
            if (re.moveToFirst()) {
                do {
                    System.out.println(re.getString(0));
                    id = re.getString(0);
                } while (re.moveToNext());
            }

            re.close();
            // System.out.println(re.getString(0));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(TITLE, title);
        contentValues.put(BODY, body);
        return db.delete(TABLE_NAME, "ID = ?", new String[]{id});
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME);
    }


}
