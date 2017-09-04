package com.java.group40;

import android.app.Activity;
import android.content.Intent;
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
    private ArrayList<JSONObject> NewsList;
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
        NewsList = new ArrayList<JSONObject>();
        adapter = new MyAdapter(activity, NewsList);
        this.list.setAdapter(adapter);
        if (cacheID != -1)
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
        list.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
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
                            if (mode != APPEND)
                                NewsList.clear();
                            for (int i = 0; i < jNewsList.length(); i++) {
                                JSONObject jNews = jNewsList.getJSONObject(i);
                                NewsList.add(jNews);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        if (jNewsList.length() == 0) {
                            if (mode == APPEND)
                                Toast.makeText(activity, R.string.no_more_result, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, R.string.no_result, Toast.LENGTH_SHORT).show();
                        }
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
                    urlGenerator = _urlGenerator;
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
                catch (SocketTimeoutException e) {
                    Message msg = handler.obtainMessage(CONNECT_ERROR, "");
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
