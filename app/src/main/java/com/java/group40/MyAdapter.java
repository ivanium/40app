/*
Note: This source file provides an adapter for MyList.
The tutorial of the implementation of adapters is from http://blog.csdn.net/tianshuguang/article/details/7344315
 */

package com.java.group40;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import org.json.*;

/**
 * Created by zyh_111 on 2017/9/3.
 */

public class MyAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater mInflater;
    private ArrayList<JSONObject> NewsList;

    public MyAdapter(Context context, ArrayList<JSONObject> Array) {
        this.context = context;
        mInflater = LayoutInflater.from(this.context);
        NewsList = Array;
    }

    @Override
    public int getCount() {
        return NewsList.size();
    }

    @Override
    public Object getItem(int position) {
        return NewsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_adapter, null);
            }
            TextView newsID = (TextView) convertView.findViewById(R.id.newsID);
            TextView newsTitle = (TextView) convertView.findViewById(R.id.newsTitle);
            TextView newsTime = (TextView) convertView.findViewById(R.id.newsTime);
            TextView newsSource = (TextView) convertView.findViewById(R.id.newsSource);

            JSONObject jNews = NewsList.get(position);
            String _time = jNews.getString("news_Time");
            String time = "";
            if (_time.length() >= 14) {
                String year = _time.substring(0, 4);
                String month = _time.substring(4, 6);
                String day = _time.substring(6, 8);
                String hour = _time.substring(8, 10);
                String minute = _time.substring(10, 12);
                String second = _time.substring(12, 14);
                time = year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
            }

            if (Global.getReadState(jNews))
                newsTitle.setTextColor(newsTime.getTextColors().getDefaultColor());
            else
                newsTitle.setTextColor(newsID.getTextColors().getDefaultColor());

            newsID.setText(jNews.getString("news_ID"));
            newsTitle.setText(jNews.getString("news_Title"));
            newsTime.setText(time);
            newsSource.setText(jNews.getString("news_Source"));

            ImageView newsImage = (ImageView) convertView.findViewById(R.id.newsImage);
            String pics = jNews.getString("news_Pictures").trim();
            if ((pics.length() > 0) && (!Global.noImage)) {
                String pic[] = pics.split("[ ;,]");

                /*
                Note: The pictures are loaded using the library Glide.
                The tutorial is from http://blog.csdn.net/hshshshshs1/article/details/50786203
                 */
                Glide.with(context).load(pic[0]).placeholder(R.drawable.ic_image_gray_24dp).
                        error(R.drawable.ic_broken_image_gray_24dp).fitCenter().into(newsImage);
                newsImage.setVisibility(View.VISIBLE);
            }
            else
                newsImage.setVisibility(View.GONE);

            return convertView;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
