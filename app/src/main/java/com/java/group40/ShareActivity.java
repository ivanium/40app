package com.java.group40;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import cn.sharesdk.*;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.*;

/**
 * Created by ivanium on 2017/9/7.
 */
public class ShareActivity extends AppCompatActivity{
    private String page;
    private String news_Author,news_Journal,news_Title,news_Time,news_Content,news_Url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        page = intent.getStringExtra("page");
        System.out.println(page);
        TextView tv = (TextView) findViewById(R.id.text);
        tv.setText(page);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showShare();
            }
        });
/*
        Intent intent2=new Intent(Intent.ACTION_SEND);
        intent2.setType("image/*");
        intent2.putExtra(Intent.EXTRA_SUBJECT, "Share");
        intent2.putExtra(Intent.EXTRA_TEXT, "I have successfully share my message through my app");
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent2, getTitle()));*/
    }

    /* 创建菜单 */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "分享");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                // intent.setType("text/plain"); //纯文本
            /*
             * 图片分享 it.setType("image/png"); 　//添加图片 File f = new
             * File(Environment.getExternalStorageDirectory()+"/name.png");
             *
             * Uri uri = Uri.fromFile(f); intent.putExtra(Intent.EXTRA_STREAM,
             * uri); 　
             */
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
                intent.putExtra(Intent.EXTRA_TEXT, "I have successfully share my message through my app");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getTitle()));
                return true;
        }
        return false;
    }
    private void showShare() {
        try {
            if(page != null) {
                JSONObject jPage = new JSONObject(page);
                news_Author = jPage.getString("news_Author");
                news_Journal = jPage.getString("news_Journal");
                news_Title = jPage.getString("news_Title");
                news_Time = jPage.getString("news_Time");
                news_Content = jPage.getString("news_Content");
                news_Url = jPage.getString("news_URL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
        oks.setTitle(news_Title);
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用
        oks.setTitleUrl(news_Url);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(news_Content);
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(news_Url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment(news_Title);
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("G_word");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(news_Url);

// 启动分享GUI
        oks.show(this);
    }
}
