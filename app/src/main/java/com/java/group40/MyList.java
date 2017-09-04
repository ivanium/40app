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
    private ArrayList<JSONObject> NewsList;
    private MyAdapter adapter;
    private URLGenerator urlGenerator;

    public static final int NEW = 0;
    public static final int REFRESH = 1;
    public static final int APPEND = 2;


    /*private void getToast(String text) {
        Toast toast = new Toast(activity);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setText(text);
        toast.show();
    }*/

    public MyList(PullToRefreshListView _list, Activity _activity, int _cacheID) {
        list = _list;
        activity = _activity;
        cacheID = _cacheID;
        NewsList = new ArrayList<JSONObject>();
        adapter = new MyAdapter(activity, NewsList);
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

        list.setOnPullEventListener(new PullToRefreshBase.OnPullEventListener<ListView>() {
            @Override
            public void onPullEvent(PullToRefreshBase<ListView> refreshView, PullToRefreshBase.State state, PullToRefreshBase.Mode direction) {
                if (direction == PullToRefreshBase.Mode.PULL_FROM_START)
                    initFromURLGenerator(urlGenerator, REFRESH);
                else if (direction == PullToRefreshBase.Mode.PULL_FROM_END)
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
                if (msg.what == 1) {
                    try {
                        String s = String.valueOf(msg.obj);
                        if (s != "") {
                            list.setMode(PullToRefreshBase.Mode.BOTH);
                            JSONObject jObject = new JSONObject(s);
                            JSONArray jNewsList = jObject.getJSONArray("list");
                            if (mode != APPEND)
                                NewsList.clear();
                            for (int i = 0; i < jNewsList.length(); i++) {
                                JSONObject jNews = jNewsList.getJSONObject(i);
                                NewsList.add(jNews);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        else if (mode != NEW)
                            Toast.makeText(activity, R.string.list_connection_failed, Toast.LENGTH_SHORT).show();
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
                    Message msg = handler.obtainMessage(1, s);
                    handler.sendMessage(msg);
                }
                catch (SocketTimeoutException e) {
                    Message msg = handler.obtainMessage(1, "");
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
