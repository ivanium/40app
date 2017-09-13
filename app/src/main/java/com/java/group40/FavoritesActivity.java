package com.java.group40;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/*
public class FavoritesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String page = intent.getStringExtra("page");
        //if favorate activity is atarted in the main activity, the page will equals ""
        if(page.equals("")) {
            page = "233";
        }
        TextView tv = (TextView) findViewById(R.id.text);

        tv.setText(page);
    }

}*/
public class FavoritesActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView listview;
    private List<myInfo> info = new ArrayList<>();
    private List<Integer> selectId = new ArrayList<>();
    private boolean isDeleteMode = false; //是否多选
    private Adapter adapter;
    private RelativeLayout layout;
    private ImageView delete;

    private Cursor offlineCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();
        initDatas();
        initEvent();
    }

    private void initView() {
        listview = (ListView) findViewById(R.id.list_view);
        layout = (RelativeLayout) findViewById(R.id.rll_view);
        delete = (ImageView) findViewById(R.id.btn_delete);
        adapter = new Adapter();
    }

    private void initDatas() {
        offlineCursor = Global.dbCache.rawQuery("select * from "+ Global.LIST_FAVORITES , null);
        offlineCursor.moveToFirst();

        info.clear();
        for (int i = 0; i < offlineCursor.getCount(); i++) {
            final myInfo save=new myInfo();
            save.time=offlineCursor.getString(0);
            save.id=offlineCursor.getString(1);
            save.title=offlineCursor.getString(2);
            info.add(save);
            offlineCursor.move(1);
        }
        Collections.sort(info);
    }

    private void initEvent() {
        listview.setAdapter(adapter);
        delete.setOnClickListener(this);
        final Activity activity = this;
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long idd) {
                Intent intent = new Intent(activity, NewsActivity.class);
                intent.putExtra("id", info.get((int) idd).id);
                activity.startActivity(intent);
                //Toast.makeText(FavoritesActivity.this, "被点击了", Toast.LENGTH_SHORT).show();
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isDeleteMode = true;
                selectId.clear();
                selectId.add(position);
                delete.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDatas();
        initEvent();
    }

    @Override
    public void onBackPressed() {
        if (isDeleteMode) {
            isDeleteMode = false;
            delete.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();
        }else super.onBackPressed();
        //layout.setVisibility(View.VISIBLE);
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_delete:
                isDeleteMode = false;
                Integer[] select = selectId.toArray(new Integer[selectId.size()]);
                Arrays.sort(select);
                for (int i = 0; i < select.length; i++) {
                    Log.d("tag",select[i]+"****************");
                }
                for (int i = select.length-1; i>=0 ; i--) {
                    Global.dbCache.delete(Global.LIST_FAVORITES, Global.LIST_FAVORITES_NEWS_ID+"=?", new String[] {info.get(select[i]).id});
                }
                for (int i = select.length-1; i>=0 ; i--) {
                    info.remove((int) select[i]);
                }
                selectId.clear();
                adapter.notifyDataSetChanged();
                delete.setVisibility(View.GONE);
                break;
            default:
                break;
        }

    }
    class Adapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater = null;

        public int getCount() {
            return info.size();
        }

        public Object getItem(int position) {
            return info.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(FavoritesActivity.this, R.layout.item_favorites, null);
                holder = new ViewHolder();
                holder.content = (TextView) convertView.findViewById(R.id.txtName);
                holder.time = (TextView) convertView.findViewById(R.id.txtTime);
                holder.check = (CheckBox) convertView.findViewById(R.id.check);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final String content = info.get(position).title;
            final String stime = info.get(position).time;
            final String ftime = "收藏时间："+stime.substring(0,4)+"-"+stime.substring(4,6)+"-"+stime.substring(6,8)+" "+stime.substring(8,10)+":"+stime.substring(10,12)+":"+stime.substring(12,14);
            holder.content.setText(content);
            holder.time.setText(ftime);
            holder.check.setChecked(false);
            if (selectId.contains(position)){
                holder.check.setChecked(true);
            }
            if (isDeleteMode) {
                holder.check.setVisibility(View.VISIBLE);
            } else {
                holder.check.setVisibility(View.INVISIBLE);
            }
            holder.check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.check.isChecked()) {

                        selectId.add(position);
                        Log.d("tag","添加"+position);
                    }else{
                        //需要强制转化使用移除对象的方法
                        selectId.remove((Integer)position);
                    }
                    Log.d("tag",position+"被选中");
                }
            });

            return convertView;
        }

        class ViewHolder {
            TextView content;
            TextView time;
            CheckBox check;
        }
    }
    class myInfo implements Comparable<myInfo>{
        String title;
        String time;
        String id;
        @Override
        public int compareTo(myInfo o) {
            int res=this.time.compareTo(o.time);
            return -res;
        }
    }
}