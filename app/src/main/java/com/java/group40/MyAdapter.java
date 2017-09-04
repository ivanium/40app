package com.java.group40;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import org.json.*;

/**
 * Created by zyh_111 on 2017/9/3.
 */

public class MyAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<JSONObject> NewsList;

    public MyAdapter(Context context, ArrayList<JSONObject> Array) {
        mInflater = LayoutInflater.from(context);
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
            TextView newsTitle = (TextView) convertView.findViewById(R.id.newsTitle);
            TextView newsTime = (TextView) convertView.findViewById(R.id.newsTime);
            TextView newsSource = (TextView) convertView.findViewById(R.id.newsSource);

            JSONObject jNews = NewsList.get(position);
            String _time = jNews.getString("news_Time");
            String year = _time.substring(0, 4);
            String month = _time.substring(4, 6);
            String day = _time.substring(6, 8);
            String hour = _time.substring(8, 10);
            String minute = _time.substring(10, 12);
            String second = _time.substring(12, 14);
            String time = year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;

            if (Global.getReadState(jNews))
                newsTitle.setTextColor(Color.rgb(128, 128, 128));
            else if (Global.night)
                newsTitle.setTextColor(Color.rgb(255, 255, 255));
            else
                newsTitle.setTextColor(Color.rgb(0, 0, 0));

            newsTitle.setText(jNews.getString("news_Title"));
            newsTime.setText(time);
            newsSource.setText(jNews.getString("news_Source"));
            return convertView;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
