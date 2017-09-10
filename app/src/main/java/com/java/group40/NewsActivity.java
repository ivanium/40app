package com.java.group40;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.Arrays;

public class NewsActivity extends AppCompatActivity {
    private static final int CONNECT_SUCC = 1, CONNECT_ERROR = 2;
    private String id;
    private String pageJson = null;
    private String news_Title = null;
    private String news_Journal = null;
    private String news_Author = null;
    private String news_Time = null;
    private String news_raw_text = null;
    private ArrayList<String> news_people_and_location = null;
    private ArrayList<String> news_Content = null;
    private ArrayList<String> news_pic_urls = null ;

    private SpeechSynthesizer newsSpeechSynthesizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        this.id = intent.getStringExtra("id");
        Log.e("news_id:", id);
        initSpeechSynthesizer();
        getPage();
//        setButton();
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
        newsSpeechSynthesizer.startSpeaking("农夫山泉维他命水为您朗读：膜拜航爷！", newsTtsListener);
    }
//    private void setButton() {
//        Button button = (Button) findViewById(R.id.voiceButton);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for (String text: news_Content) {
//                    newsSpeechSynthesizer.startSpeaking(text, newsTtsListener);
//                }
//            }
//        });
//    }

    private SynthesizerListener newsTtsListener = new SynthesizerListener() {
        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            // TODO Auto-generated method stub

        }
        @Override
        public void onSpeakBegin() {
        }
        @Override
        public void onSpeakPaused() {
        }
        @Override
        public void onSpeakResumed() {
        }
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
        }
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            if(pageJson == null) {
                // TODO: 2017/9/7 pop a warning toast.
                return false;
            }
            Intent intent = new Intent(this, ShareActivity.class);
            intent.putExtra("page", pageJson);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            intent.putExtra("page", pageJson);
            startActivity(intent);
            // TODO: 2017/9/7 is favorite flag
            return true;
        }
        else if (id == R.id.action_tts) {
            boolean isChecked = item.isChecked();
            if(isChecked) {
                newsSpeechSynthesizer.stopSpeaking();
            }
            else {
                newsSpeechSynthesizer.startSpeaking(news_raw_text, newsTtsListener);
            }
            item.setChecked(!isChecked);
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
                news_raw_text = jPage.getString("news_Content").replaceAll("[ 　]+([ 　]{2,}|\\t)", "\n　　");
                analyseContent();
                
                String[] pic_urls = jPage.getString("news_Pictures").trim().split("[ ;,]");
                news_pic_urls = new ArrayList<>(Arrays.asList(pic_urls));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void analyseContent() {
        String raw_content = news_raw_text;
        for (String key : news_people_and_location) {
            raw_content = raw_content.replaceAll(key, "<a herf=\"https://baike.baidu.com/item/" + key + "\">" + key + "</a>");
        }
        String[] contents = raw_content.split("\n");
        news_Content = new ArrayList<>(Arrays.asList(contents));
    }

    private class PageGetterHandler extends Handler {
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
                    toolbar.setTitle("新闻正文");
//                    setContentView(R.layout.activity_news);
                    setView((LinearLayout) findViewById(R.id.newsActivityLinearLayout));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setView(final LinearLayout linearLayout) {

            TextView titleTextView = (TextView) findViewById(R.id.newsActivityTitle);
            titleTextView.setText(activity.news_Title);
            TextView infoTextView = (TextView) findViewById(R.id.newsActivityInfo);
            infoTextView.setText(activity.news_Author + "\t\t" + activity.news_Journal + "\t\t" + activity.news_Time);


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            ImageView imageView = (ImageView) findViewById(R.id.newsActivityImage);
//            for (String html: news_pic_urls) {
//                Glide.with(activity).load(html).into(imageView);
//                break;
//            }
//            TextView contentTextView = (TextView) findViewById(R.id.newsActivityContent);
//            contentTextView.setText(activity.news_Content.indexOf(0));
            int pic_num = news_pic_urls.size();
            if(Global.noImage) { // when the no image mode is on or there is just
                for (String text : news_Content) {
                    linearLayout.addView(createTextView(text, params));
                }
            }
            else if(pic_num == 0) {
                // TODO: 2017/9/9 add auto-find picture
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
//            tv.setTextSize(R.dimen.news_content_font_size);
//            tv.setTextScaleX(1.2f);
            tv.setCompoundDrawablePadding(10);
            tv.setText(Html.fromHtml(text));
            tv.setPadding(20, 0, 20, 0);
            return tv;
        }
        private ImageView createImageView(String img_url, ViewGroup.LayoutParams params) {
            ImageView iv = new ImageView(activity);
            iv.setLayoutParams(params);
            Glide.with(activity).load(img_url).into(iv);
            return iv;
        }
    }
}
