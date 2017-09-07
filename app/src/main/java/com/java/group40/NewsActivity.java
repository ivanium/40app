package com.java.group40;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class NewsActivity extends AppCompatActivity {
//    private static String newsTitle;
    private static final int CONNECT_SUCC = 1, CONNECT_ERROR = 2;
    private static String id;
    private String pageJson = null;
    private String news_Title = null;
    private String news_Joural = null;
    private String news_Author = null;
    private String news_Time = null;
    private String news_Content = null;

    private void getId() {
        Intent intent = getIntent();
        this.id = intent.getStringExtra("id");
    }

    private void getPage() {
        final PageGetterHandler pHandler = new PageGetterHandler(this);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String pageFullContent = null;
                try {
                    String head = "http://166.111.68.66:2042/news/action/query/detail?newsId=";
                    URL pageUrl = new URL(head + id);

                    HttpURLConnection conn = (HttpURLConnection) pageUrl.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    if (conn.getResponseCode() == 200) {
                        InputStream __netIn = conn.getInputStream();
                        InputStreamReader _netIn = new InputStreamReader(__netIn);
                        BufferedReader netIn = new BufferedReader(_netIn);
                        pageFullContent = netIn.readLine();
                        netIn.close();
                        _netIn.close();
                        __netIn.close();
                    }

                    Message msg = pHandler.obtainMessage(CONNECT_SUCC, pageFullContent);
                    pHandler.sendMessage(msg);

                } catch (SocketTimeoutException e) {
                    Message msg = pHandler.obtainMessage(CONNECT_ERROR, null);
                    pHandler.sendMessage(msg);

                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getId();

//        TextView text = (TextView) findViewById(R.id.text);
        getPage();

    }

    private final void parseJson() {
        try {
            if(pageJson != null) {
                JSONObject jPage = new JSONObject(pageJson);
                news_Author = jPage.getString("news_Author");
                news_Joural = jPage.getString("news_Journal");
                news_Title = jPage.getString("news_Title");
                news_Time = jPage.getString("news_Time");
                news_Content = jPage.getString("news_Content");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PageGetterHandler extends Handler {
        NewsActivity activity;
        PageGetterHandler(NewsActivity nActivity) {
            activity = nActivity;
        }

        @Override
        public void handleMessage(Message msg) {

            try {
                if (msg.what == CONNECT_ERROR) {
                    Toast.makeText(activity, R.string.list_connection_failed, Toast.LENGTH_SHORT).show();
                }
                if(msg.what == CONNECT_SUCC) {
                    activity.pageJson = String.valueOf(msg.obj);
                    activity.parseJson();

                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    toolbar.setTitle(activity.news_Title);

                    TextView textView = (TextView) findViewById(R.id.text);
                    textView.setText(activity.news_Author + "\n" +
                                activity.news_Joural + "\n" +
                                activity.news_Title + "\n" +
                                activity.news_Time + "\n" +
                                activity.news_Content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
