package com.java.group40;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import cn.sharesdk.onekeyshare.OnekeyShare;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsActivity extends AppCompatActivity {
    private static final String TAG = "nadebug";

    private static final int OFFLINE = 0, CONNECT_SUCC = 1, CONNECT_ERROR = 2;
    private String news_id;
    private String pageJson = null;
    private String news_Title = null;
    private String news_Journal = null;
    private String news_Author = null;
    private String news_Time = null;
    private String share_page;
    private String share_news_Author,share_news_Journal,share_news_Title,share_news_Time,share_news_Content,share_news_Url;
    private String news_raw_text = null;
    private ArrayList<String> news_people_and_location = null;
    private ArrayList<String> news_Keywords = null;
    private ArrayList<String> news_Content = null;
    private ArrayList<String> news_pic_urls = null ;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");


    private Thread getPageThread = null;

    private SpeechSynthesizer newsSpeechSynthesizer = null;

    private Cursor offlineCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        this.news_id = intent.getStringExtra("id");
        Log.e("news_id:", news_id);

        offlineCursor = Global.dbCache.rawQuery("select * from "+ Global.NEWS_CACHE +" where "+ Global.NEWS_CACHE_NEWS_ID +"=?", new String[] {news_id});

        if(Global.voice)
            initSpeechSynthesizer();
        getPage();
        linkDecorate();
    }

    @Override
    public void onBackPressed() {
        if(newsSpeechSynthesizer != null) {
            newsSpeechSynthesizer.stopSpeaking();
        }
        super.onBackPressed();
    }

    private void initSpeechSynthesizer() {
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=59b3bff8"); //
        newsSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(this, new InitListener() {
            @Override
            public void onInit(int code) {
                Log.d("newsSynthesizer:", "InitListener init() code = " + code);
            }
        });
        newsSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME,"xiaoyan");
        newsSpeechSynthesizer.setParameter(SpeechConstant.PITCH,"50");
        newsSpeechSynthesizer.setParameter(SpeechConstant.VOLUME,"50");
        newsSpeechSynthesizer.stopSpeaking();
//        newsSpeechSynthesizer.startSpeaking("农夫山泉维他命水为您朗读：膜拜航爷！", newsTtsListener);
    }

    private SynthesizerListener newsTtsListener = new SynthesizerListener() {
        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
        @Override
        public void onSpeakBegin() {}
        @Override
        public void onSpeakPaused() {}
        @Override
        public void onSpeakResumed() {}
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {}
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {}

        @Override
        public void onCompleted(SpeechError error) {
            if(error!=null)
            {
                Log.d("Synthesizer err:", error.getErrorCode()+"");
            }
            else
            {
                Log.d("Synthesizer complete:", "0");
            }
        }
    };

    private void getPage() {
        final PageGetterHandler pHandler = new PageGetterHandler(this);
        if(offlineCursor.getCount() == 0) {// load content from web
            getPageThread = new Thread(new Runnable() {
                @Override
                public void run() {
//                    String pageFullContent = null;
                    try {
                        String head = "http://166.111.68.66:2042/news/action/query/detail?newsId=";
                        URL pageUrl = new URL(head + news_id);

                        HttpURLConnection conn = (HttpURLConnection) pageUrl.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);

                        if (conn.getResponseCode() == 200) {
                            InputStream __netIn = conn.getInputStream();
                            InputStreamReader _netIn = new InputStreamReader(__netIn);
                            BufferedReader netIn = new BufferedReader(_netIn);
//                            pageFullContent = netIn.readLine();
                            pageJson = netIn.readLine();
                            netIn.close();
                            _netIn.close();
                            __netIn.close();
                        }

                        parseJson();

//                        Message msg = pHandler.obtainMessage(CONNECT_SUCC, pageFullContent);
                        Message msg = pHandler.obtainMessage(CONNECT_SUCC, null);
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
        }
        else {//load content from local database
            getPageThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        offlineCursor.moveToNext();
//                        String newsJson = offlineCursor.getString(offlineCursor.getColumnIndex(Global.NEWS_CACHE_JSON));
                        pageJson = offlineCursor.getString(offlineCursor.getColumnIndex(Global.NEWS_CACHE_JSON));
                        parseJson();

//                        Message msg = pHandler.obtainMessage(OFFLINE, newsJson);
                        Message msg = pHandler.obtainMessage(OFFLINE, null);
                        pHandler.sendMessage(msg);
                        offlineCursor.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        getPageThread.start();
    }

    private void linkDecorate() {
        final refreshHandler rhandler = new refreshHandler(this);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getPageThread.join();
                    String raw_content = news_raw_text;
                    Collections.sort(news_people_and_location, new Comparator<String>() {
                        @Override
                        public int compare(String o1, String o2) {
                            return (o1.length() > o2.length() ? 1 : -1);
                        }
                    });
//                    Log.e(TAG, "baike is running!"+news_people_and_location);
                    for (String key : news_people_and_location) {
//                        Log.e(TAG, "key="+key);
                        if (testKeyValid(key)) {
                            raw_content = raw_content.replaceAll(key, "<a href=\"https://baike.baidu.com/item/" + key + "\">" + key + "</a>");
                        }
                    }

                    String[] contents = raw_content.split("\n");
                    news_Content = new ArrayList<>(Arrays.asList(contents));
                    Message msg = rhandler.obtainMessage(CONNECT_SUCC, "");
                    rhandler.sendMessage(msg);
                } catch (Exception e) {
//                    Log.e(TAG, "baike is fucked");
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news, menu);

        Cursor favoriteCursor = Global.dbCache.rawQuery("select * from "+ Global.LIST_FAVORITES +" where "+ Global.LIST_FAVORITES_NEWS_ID +"=?",
        new String[] {news_id});
        boolean isfavorate = (favoriteCursor.getCount() != 0);
        menu.getItem(0).setChecked(isfavorate);
        menu.getItem(0).setIcon(isfavorate ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_border_white_24dp);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            if(pageJson == null) {
                Toast.makeText(this, R.string.void_page, Toast.LENGTH_SHORT).show();
                return false;
            }
            share_page=pageJson;
            showShare();/*
            Intent intent = new Intent(this, ShareActivity.class);
            intent.putExtra("page", pageJson);
            startActivity(intent);*/
            return true;
        }
        else if (id == R.id.action_favorites) {
            boolean isChecked = item.isChecked();
            if(isChecked) {
                Global.dbCache.delete(Global.LIST_FAVORITES, Global.LIST_FAVORITES_NEWS_ID+"=?", new String[] {this.news_id});
                item.setIcon(R.drawable.ic_star_border_white_24dp);
                Toast.makeText(this, R.string.delete_favorite_item, Toast.LENGTH_SHORT).show();
            }
            else {
                ContentValues cv = new ContentValues();
                cv.put(Global.LIST_FAVORITES_NEWS_ID, this.news_id);
                cv.put(Global.LIST_FAVORITES_NEWS_TITLE, this.news_Title);
                Date curDate = new Date(System.currentTimeMillis());
                cv.put(Global.LIST_FAVORITES_TIME, sdf.format(curDate));
                Global.dbCache.insert(Global.LIST_FAVORITES, null, cv);

                item.setIcon(R.drawable.ic_star_yellow_24dp);
                Toast.makeText(this, R.string.add_favorite_item, Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(this, FavoritesActivity.class);
//                intent.putExtra("page", pageJson);
//                startActivity(intent);
            }
            item.setChecked(!isChecked);
            return true;
        }
        else if (id == R.id.action_tts) {
            if(Global.voice == false) {
                Toast.makeText(this, R.string.voice_is_off, Toast.LENGTH_SHORT).show();
                return true;
            }
            boolean isChecked = item.isChecked();
            if(isChecked) {
                item.setIcon(R.drawable.ic_record_voice_over_white_24dp);
                newsSpeechSynthesizer.stopSpeaking();
            }
            else {
                item.setIcon(R.drawable.ic_record_voice_over_yellow_24dp);
                newsSpeechSynthesizer.startSpeaking(news_raw_text, newsTtsListener);
            }
            item.setChecked(!isChecked);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void parseJson() {
        try {
            if(pageJson != null) {
                JSONObject jPage = new JSONObject(pageJson);
                news_Author = jPage.getString("news_Author");
                news_Journal = jPage.getString("news_Journal");
                news_Title = jPage.getString("news_Title");
                String rawTime = jPage.getString("news_Time");
                news_Time = rawTime.substring(0, 4) + "年" + rawTime.substring(4, 6) + "月" + rawTime.substring(6, 8) + "日";
                String[] people_and_location = (jPage.getString("persons")
                        + jPage.getString("locations")).replaceAll("[^\\u4e00-\\u9fa5]+", " ").trim().split(" ");
                news_people_and_location = new ArrayList<>(Arrays.asList(people_and_location));

                String[] raw_keywords = jPage.getString("Keywords").replaceAll("[^\\u4e00-\\u9fa5]+", " ").trim().split(" ");
                news_Keywords = new ArrayList<>(Arrays.asList(raw_keywords));

                news_raw_text = jPage.getString("news_Content").replaceAll("[ 　]+([ 　]{2,}|\\t)", "\n　　");
//                analyseContent();
                news_Content = new ArrayList<>(Arrays.asList(news_raw_text.split("\n")));

                String[] pic_urls = jPage.getString("news_Pictures").trim().split("[ ;,]");
//                if(pic_urls.length == 1 && pic_urls[0].equals("")) {
//                    news_pic_urls = new ArrayList<>();
//                }
//                else {
                    news_pic_urls = new ArrayList<>(Arrays.asList(pic_urls));
//                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean testKeyValid(String key) {
        boolean valid = false;
        try {
            URL baikeUrl = new URL("https://baike.baidu.com/item/" + key);
            HttpURLConnection conn = (HttpURLConnection) baikeUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream __netIn = conn.getInputStream();
                InputStreamReader _netIn = new InputStreamReader(__netIn);
                BufferedReader netIn = new BufferedReader(_netIn);
                String pageFullContent = null;
                for (int i = 0; i < 5; i++) {
                    pageFullContent = netIn.readLine();
                }
                netIn.close();
                _netIn.close();
                __netIn.close();
                valid = !(pageFullContent.trim().equals("<title>百度百科——全球最大中文百科全书</title>")
                        || pageFullContent.trim().equals("<title>百度百科_全球最大中文百科全书</title>"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valid;
    }

    private class PageGetterHandler extends Handler {
        NewsActivity activity;
        int textColor;

        PageGetterHandler(NewsActivity nActivity) {
            activity = nActivity;
        }

        @Override
        public void handleMessage(Message msg) {

            try {
                if (msg.what == CONNECT_ERROR) {
                    Toast.makeText(activity, R.string.list_connection_failed, Toast.LENGTH_SHORT).show();
                }
                else if(msg.what == CONNECT_SUCC) {
//                    activity.pageJson = String.valueOf(msg.obj);
//                    activity.parseJson();

                    setView((LinearLayout) findViewById(R.id.newsActivityLinearLayout));

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Global.NEWS_CACHE_NEWS_ID, news_id);
                    contentValues.put(Global.NEWS_CACHE_JSON, activity.pageJson);
                    Global.dbCache.insert(Global.NEWS_CACHE, null, contentValues);
                }
                else if(msg.what == OFFLINE) {
//                    activity.pageJson = String.valueOf(msg.obj);
//                    activity.parseJson();

                    setView((LinearLayout) findViewById(R.id.newsActivityLinearLayout));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setView(final LinearLayout linearLayout) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("新闻正文");

            TextView titleTextView = (TextView) findViewById(R.id.newsActivityTitle);
            titleTextView.setText(activity.news_Title);
            textColor = titleTextView.getCurrentTextColor();

            TextView infoTextView = (TextView) findViewById(R.id.newsActivityInfo);
            infoTextView.setText(activity.news_Author + "\t\t" + activity.news_Journal + "\t\t" + activity.news_Time);


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            int pic_num = news_pic_urls.size();
            if(Global.noImage) { // when the no image mode is on or there is just
                for (String text : news_Content) {
                    linearLayout.addView(createTextView(text, params));
                }
            }
            else if(pic_num == 0) {
//                if(autoImage()) {
//                    linearLayout.addView(createImageView(news_pic_urls.get(0), params));
//                }
                for (String text : news_Content) {
                    linearLayout.addView(createTextView(text, params));
                }
            }
            else {
                int text_num = news_Content.size();
                int text_per_pic = text_num / pic_num;
                int current_text = 0;

                for (String html : news_pic_urls) {
                    linearLayout.addView(createImageView(html, params));
                    for (int i = 0; i < text_per_pic; i++) {
                        linearLayout.addView(createTextView(news_Content.get(current_text+i), params));
                    }
                    current_text += text_per_pic;
                }
                for (; current_text < text_num; current_text++) {
                    linearLayout.addView(createTextView(news_Content.get(current_text), params));
                }
            }
        }

        private TextView createTextView(String text, ViewGroup.LayoutParams params) {
            TextView tv = new TextView(activity);
            tv.setLayoutParams(params);

            tv.setCompoundDrawablePadding(10);
            tv.setText(Html.fromHtml(text));

            tv.setTextColor(textColor);
            tv.setTextSize(16);
            tv.setPadding(20, 0, 20, 15);
            tv.setCompoundDrawablePadding(5);
            tv.setLineSpacing(0, 1.2f);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            return tv;
        }
        private ImageView createImageView(String img_url, ViewGroup.LayoutParams params) {
            ImageView iv = new ImageView(activity);
            iv.setLayoutParams(params);
//            iv.setPadding(10, 10, 10, 10);
//            iv.setPadding(0, 0, 0, 5);
            Glide.with(activity).load(img_url).into(iv);
            return iv;
        }

        private boolean autoImage() {
            try {
                if(news_Keywords.size() == 0) {
                    return false;
                }
                String key = (news_Keywords.size() == 1 ? news_Keywords.get(0) : news_Keywords.get(0) + "+"+news_Keywords.get(1));
                Log.e(TAG, "autoImage: key = " + key);
                URL pageUrl = new URL("https://api.cognitive.microsoft.com/bing/v5.0/search?q=" + URLEncoder.encode(key, "UTF-8")+"&responseFilter=images&mkt=zn-ch");
                HttpURLConnection conn = (HttpURLConnection) pageUrl.openConnection();
                conn.addRequestProperty("Ocp-Apim-Subscription-Key", "33bd2c69ff8a48f29a9e98e7ef8af5b7");
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    InputStream __netIn = conn.getInputStream();
                    InputStreamReader _netIn = new InputStreamReader(__netIn);
                    BufferedReader netIn = new BufferedReader(_netIn);
                    String pageFullContent = netIn.readLine();
                    JSONObject autoImageJson = new JSONObject(pageFullContent);
                    String pic_json = autoImageJson.getString("image");
                    JSONObject picJson = new JSONObject(pic_json);
                    String pics_urls = picJson.getString("value");
                    Pattern p = Pattern.compile("\"contentUrl\":\"(.*)\"");
                    Matcher m = p.matcher(pics_urls);
                    String pic_url = null;
                    if(m.find()) {
                        pic_url = m.group(1);
                    }

                    if(pic_url != null) {
                        news_pic_urls.add(pic_url);
                    }
                    netIn.close();
                    _netIn.close();
                    __netIn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private class refreshHandler extends Handler {
        NewsActivity activity;
        refreshHandler(NewsActivity nActivity) {
            activity = nActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                refreshView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void refreshView() {
            LinearLayout llyt = (LinearLayout) findViewById(R.id.newsActivityLinearLayout);

            int pic_num = news_pic_urls.size();
            if(pic_num == 0) {
                int i = 0;
                for (String para : news_Content) {
                    ((TextView) llyt.getChildAt(i++)).setText(Html.fromHtml(para));
                }
            }
            else {
                int para_num = news_Content.size();
                int para_per_pic = para_num / pic_num;
                int currentPara = 0;
                for (int i = 0; i < pic_num; i++) {
                    for (int j = 0; j < para_per_pic; j++) {
                        ((TextView) llyt.getChildAt(3 + i*(1+para_per_pic) + j)).setText(Html.fromHtml(news_Content.get(currentPara++)));
                    }
                }
                for (int bias = 2 + pic_num*(1+para_per_pic); currentPara < para_num; currentPara++) {
                    ((TextView) llyt.getChildAt(bias++)).setText(Html.fromHtml(news_Content.get(currentPara)));
                }
            }
        }
    }
    private void showShare() {
        try {
            if(share_page != null) {
                JSONObject jPage = new JSONObject(share_page);
                share_news_Author = jPage.getString("news_Author");
                share_news_Journal = jPage.getString("news_Journal");
                share_news_Title = jPage.getString("news_Title");
                share_news_Time = jPage.getString("news_Time");
                share_news_Content = jPage.getString("news_Content");
                share_news_Url = jPage.getString("news_URL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        使用开源第三方库sharesdk，参考其文档代码，参考网址为：http://wiki.mob.com/sdk-share-android-3-0-0/
         */
        //ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
        oks.setTitle(share_news_Title);
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用
        oks.setTitleUrl(share_news_Url);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(share_news_Title+" 网页链接："+share_news_Url);
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        Log.e("debug",news_pic_urls.get(0));
        oks.setImageUrl(news_pic_urls.get(0));
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(share_news_Url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment(share_news_Title);
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("G_word");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(share_news_Url);

// 启动分享GUI
        oks.show(this);
    }
}
