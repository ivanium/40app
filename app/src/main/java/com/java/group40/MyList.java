package com.java.group40;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.*;
import java.net.*;

import org.json.*;

/**
 * Created by zyh_111 on 2017/9/4.
 */

class URLGenerator {

    private String head;
    private String tail;
    private int page;

    public URLGenerator(String head, String tail) {
        this.head = head;
        this.tail = tail;
    }

    private URL getURL() {
        try {
            URL url = new URL(head + page + tail);
            return url;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public URL firstPage() {
        page = 1;
        return getURL();
    }

    public URL nextPage() {
        page++;
        return getURL();
    }

}

public class MyList {

    private PullToRefreshListView list;
    private Activity activity;
    private int cacheID;
    private JSONArray jNewsList;
    private MyAdapter adapter;
    private URLGenerator urlGenerator;

    public MyList(PullToRefreshListView _list, Activity _activity, int _cacheID) {
        list = _list;
        activity = _activity;
        cacheID = _cacheID;
        jNewsList = new JSONArray();
        adapter = new MyAdapter(activity, jNewsList);
        this.list.setAdapter(adapter);
        if (_cacheID != -1)
            loadFromCache();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    JSONObject jNews = (JSONObject) adapter.getItem((int) id);
                    Global.setRead(jNews, adapter);
                    Intent intent = new Intent(activity, NewsActivity.class);
                    intent.putExtra("id", jNews.getString("news_ID"));
                    activity.startActivity(intent);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadFromCache() {
        list.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
    }

    public void initFromURLGenerator(final URLGenerator _urlGenerator) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    try {
                        String s = String.valueOf(msg.obj);
                        if (s != "") {
                            list.setMode(PullToRefreshBase.Mode.BOTH);
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
                    urlGenerator = _urlGenerator;
                    URL url = urlGenerator.firstPage();
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
