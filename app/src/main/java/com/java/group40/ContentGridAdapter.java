package com.java.group40;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by ivanium on 2017/9/7.
 */

public class ContentGridAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<ImageView> ivList = new ArrayList<>();

    public ContentGridAdapter(Context context, ArrayList<ImageView> ivList) {
        layoutInflater = LayoutInflater.from(context);
        this.ivList = ivList;
    }

    @Override
    public int getCount() {return ivList.size();}
    @Override
    public Object getItem(int position) {
        return ivList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.view_adapter, null);
            }

            ImageView iView = ivList.get(position);

            return convertView;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
