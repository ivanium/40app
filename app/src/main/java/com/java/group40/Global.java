package com.java.group40;

import android.database.Cursor;
import android.database.sqlite.*;

import java.io.*;
import java.util.ArrayList;

import org.json.*;

/**
 * Created by zyh_111 on 2017/9/2.
 */

public class Global{

    public static final String J_NIGHT = "night";
    public static final String J_NO_IMAGE = "no_image";
    public static final String J_VOICE = "voice";
    public static final String J_CAT = "category";

    public static final String FILE_SETTINGS = "settings.json";
    public static final String PATH_SETTINGS = "/data/data/com.java.group40/files/settings.json";

    public static final String DIR_CACHE = "/data/data/com.java.group40/databases";
    public static final String PATH_CACHE = "/data/data/com.java.group40/databases/cache.db";

    public static final String STATE_READ = "state_read";
    public static final String STATE_READ_NEWS_ID = "news_id";

    public static final String LIST_CACHE = "list_cache";
    public static final String LIST_CACHE_CAT = "category";
    public static final String LIST_CACHE_ID = "id";
    public static final String LIST_CACHE_J_NEWS = "j_news";

    public static final String NEWS_CACHE = "news_cache";
    public static final String NEWS_CACHE_NEWS_ID = "news_id";
    public static final String NEWS_CACHE_JSON = "json";

    public static final String LIST_FAVORITES = "list_favorites";
    public static final String LIST_FAVORITES_TIME = "time";
    public static final String LIST_FAVORITES_NEWS_ID = "news_id";
    public static final String LIST_FAVORITES_NEWS_TITLE = "news_title";

    public static final int PAGE_SIZE = 10;

    public static boolean newSettings = false;
    public static boolean night;
    public static boolean noImage;
    public static boolean voice;
    public static ArrayList<Integer> catList = new ArrayList<Integer>();

    public static boolean isLoaded[] = {false, false, false, false, false, false, false, false, false, false, false ,false};

    public static SQLiteDatabase dbCache;

    public static void setCatList(int cat) {
        catList.clear();
        for (int i = 0; i < 12; i++)
            if ((cat & (1 << i)) != 0)
                catList.add(i);
    }

    public static void initDatabase() {
        File dirDatabase = new File(DIR_CACHE);
        if (!dirDatabase.exists())
            dirDatabase.mkdirs();
        dbCache = SQLiteDatabase.openOrCreateDatabase(PATH_CACHE, null);
        dbCache.execSQL("create table if not exists " + STATE_READ + "(" + STATE_READ_NEWS_ID + " varchar(50))");
        dbCache.execSQL("create table if not exists " + LIST_CACHE + "(" + LIST_CACHE_CAT + " int, " +
                LIST_CACHE_ID + " int, " + LIST_CACHE_J_NEWS + " nvarchar(4000))");
        dbCache.execSQL("create table if not exists " + NEWS_CACHE + "(" + NEWS_CACHE_NEWS_ID + " varchar(50), " +
                NEWS_CACHE_JSON + " text)");
        dbCache.execSQL("create table if not exists " + LIST_FAVORITES + "(" + LIST_FAVORITES_TIME + " varchar(20), " +
                LIST_FAVORITES_NEWS_ID + " varchar(50), " + LIST_FAVORITES_NEWS_TITLE + " nvarchar(200))");
    }

    public static boolean getReadState(JSONObject jNews) {
        try {
            Cursor cursor = dbCache.query(STATE_READ, null, STATE_READ_NEWS_ID + " = ?",
                    new String[]{jNews.getString("news_ID")}, null, null, null);
            return cursor.getCount() != 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
