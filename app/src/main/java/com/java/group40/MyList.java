/*
Note: The implementation of the list is based on an open-source library named pulltorefresh.
The source code of pulltorefresh can be downloaded from https://github.com/chrisbanes/Android-PullToRefresh
The tutorial is from http://www.cnblogs.com/tianzhijiexian/p/4023802.html
 */

package com.java.group40;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.json.*;

/**
 * Created by zyh_111 on 2017/9/4.
 */

public class MyList {

    private PullToRefreshListView list;
    private Activity activity;
    private int cacheID;
    private ArrayList<JSONObject> newsList;
    private MyAdapter adapter;
    private URLGenerator urlGenerator;

    public static final int NEW = 0;
    public static final int REFRESH = 1;
    public static final int APPEND = 2;

    public static final int CONNECTED = 1;
    public static final int CONNECT_ERROR = 2;

    public MyList(PullToRefreshListView _list, Activity _activity, int _cacheID) {
        list = _list;
        activity = _activity;
        cacheID = _cacheID;
        newsList = new ArrayList<>();
        if (cacheID != -1)
            loadFromCache();
        adapter = new MyAdapter(activity, newsList);
        this.list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    JSONObject jNews = (JSONObject) adapter.getItem((int) id);
                    if (!Global.getReadState(jNews)) {
                        ContentValues val = new ContentValues();
                        val.put(Global.STATE_READ_NEWS_ID, jNews.getString("news_ID"));
                        Global.dbCache.insert(Global.STATE_READ, null, val);
                        adapter.notifyDataSetChanged();
                    }
                    Intent intent = new Intent(activity, NewsActivity.class);
                    intent.putExtra("id", jNews.getString("news_ID"));
                    activity.startActivity(intent);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        list.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                initFromURLGenerator(urlGenerator, REFRESH);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                initFromURLGenerator(urlGenerator, APPEND);
            }
        });
    }

    private void loadFromCache() {
        try {
            newsList.clear();
            Cursor cursor = Global.dbCache.query(Global.LIST_CACHE, null, Global.LIST_CACHE_CAT + " = ?",
                    new String[]{String.valueOf(cacheID)}, null, null, Global.LIST_CACHE_ID);
            int cnt = cursor.getCount();
            cursor.moveToFirst();
            for (int i = 0; i < cnt; i++) {
                String s = cursor.getString(2);
                JSONObject jNews = new JSONObject(s);
                newsList.add(jNews);
                cursor.move(1);
            }
            list.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initFromURLGenerator(final URLGenerator _urlGenerator, final int mode) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try {
                    if ((msg.what == CONNECT_ERROR) && (mode != NEW)) {
                        Toast.makeText(activity, R.string.list_connection_failed, Toast.LENGTH_SHORT).show();
                        if (mode == APPEND)
                            _urlGenerator.prevPage();
                    }
                    if (msg.what == CONNECTED) {
                        String s = String.valueOf(msg.obj);
                        JSONArray jNewsList = new JSONArray();
                        if (s != "") {
                            list.setMode(PullToRefreshBase.Mode.BOTH);
                            JSONObject jObject = new JSONObject(s);
                            jNewsList = jObject.getJSONArray("list");
                            if (mode != APPEND) {
                                newsList.clear();
                                if (cacheID != -1)
                                    Global.dbCache.delete(Global.LIST_CACHE, Global.LIST_CACHE_CAT + " = ?", new String[]{String.valueOf(cacheID)});
                            }
                            for (int i = 0; i < jNewsList.length(); i++) {
                                JSONObject jNews = jNewsList.getJSONObject(i);
                                newsList.add(jNews);
                                if (cacheID != -1) {
                                    ContentValues val = new ContentValues();
                                    val.put(Global.LIST_CACHE_CAT, cacheID);
                                    val.put(Global.LIST_CACHE_ID, newsList.size() - 1);
                                    val.put(Global.LIST_CACHE_J_NEWS, jNews.toString());
                                    Global.dbCache.insert(Global.LIST_CACHE, null, val);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                        if (jNewsList.length() == 0) {
                            if (mode == APPEND)
                                Toast.makeText(activity, R.string.no_more_result, Toast.LENGTH_SHORT).show();
                            else {
                                clear();
                            }
                        }
                        if (cacheID != -1)
                            Global.isLoaded[cacheID] = true;
                    }
                    if (mode != NEW)
                        list.onRefreshComplete();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = "";
                    URL url;
                    if (mode == APPEND)
                        url = urlGenerator.nextPage();
                    else
                        url = urlGenerator.firstPage();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    if (conn.getResponseCode() == 200) {
                        InputStream __netIn = conn.getInputStream();
                        InputStreamReader _netIn = new InputStreamReader(__netIn);
                        BufferedReader netIn = new BufferedReader(_netIn);
                        s = netIn.readLine();
                        netIn.close();
                        _netIn.close();
                        __netIn.close();
                    }
                    Message msg = handler.obtainMessage(CONNECTED, s);
                    handler.sendMessage(msg);
                }
                catch (Exception e) {
                    Message msg = handler.obtainMessage(CONNECT_ERROR, "");
                    handler.sendMessage(msg);
                }
            }
        });

        urlGenerator = _urlGenerator;
        if ((cacheID != -1) && Global.isLoaded[cacheID] && (mode == NEW)) {
            loadFromCache();
            list.setMode(PullToRefreshBase.Mode.BOTH);
        }
        else thread.start();
    }

    public void clear() {
        if (cacheID == -1) {
            newsList.clear();
            adapter.notifyDataSetChanged();
        }
    }

}
