package com.java.group40;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.*;
import android.os.Handler;
import android.os.Message;
import android.widget.BaseAdapter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.json.*;

/**
 * Created by zyh_111 on 2017/9/2.
 */

public class Global{

    public static final String J_NIGHT = "night";
    public static final String J_NO_IMAGE = "noimage";
    public static final String J_VOICE = "voice";
    public static final String J_CAT = "category";

    public static final String FILE_SETTINGS = "settings.json";
    public static final String PATH_SETTINGS = "/data/data/com.java.group40/files/settings.json";

    public static final String DIR_CACHE = "/data/data/com.java.group40/databases";
    public static final String PATH_CACHE = "/data/data/com.java.group40/databases/cache.db";

    public static final String STATE_READ = "state_read";
    public static final String STATE_READ_NEWS_ID = "news_id";

    public static boolean newSettings = false;
    public static boolean night;
    public static boolean noImage;
    public static boolean voice;
    public static ArrayList<Integer> catList = new ArrayList<Integer>();

    public static SQLiteDatabase dbCache;

    public static void setCatList(int cat) {
        catList.clear();
        for (int i = 0; i < 12; i++)
            if ((cat & (1 << i)) != 0)
                catList.add(i);
    }

    public static boolean getReadState(JSONObject jNews) {
        try {
            Cursor cursor = Global.dbCache.query(Global.STATE_READ, new String[]{Global.STATE_READ_NEWS_ID},
                    Global.STATE_READ_NEWS_ID + "=?", new String[]{jNews.getString("news_ID")}, null, null, null);
            return cursor.getCount() != 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setRead(JSONObject jNews, BaseAdapter adapter) {
        try {
            if (!getReadState(jNews)) {
                ContentValues val = new ContentValues();
                val.put(Global.STATE_READ_NEWS_ID, jNews.getString("news_ID"));
                Global.dbCache.insert(Global.STATE_READ, null, val);
                adapter.notifyDataSetChanged();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadJsonFromURL(final URL url, final JSONArray jNewsList, final BaseAdapter adapter, int cacheID) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    try {
                        String s = String.valueOf(msg.obj);
                        if (s != "") {
                            JSONObject jObject = new JSONObject(s);
                            JSONArray _jNewsList = jObject.getJSONArray("list");
                            for (int i = 0; i < _jNewsList.length(); i++) {
                                JSONObject jNews = _jNewsList.getJSONObject(i);
                                jNewsList.put(jNews);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = "";
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(30000);
                    conn.setConnectTimeout(30000);
                    if (conn.getResponseCode() == 200) {
                        InputStream __netIn = conn.getInputStream();
                        InputStreamReader _netIn = new InputStreamReader(__netIn);
                        BufferedReader netIn = new BufferedReader(_netIn);
                        s = netIn.readLine();
                        netIn.close();
                        _netIn.close();
                        __netIn.close();
                    }
                    Message msg = handler.obtainMessage(1, s);
                    handler.sendMessage(msg);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

}
