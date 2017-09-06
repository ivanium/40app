package com.java.group40;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.*;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final PullToRefreshListView list = (PullToRefreshListView) findViewById(R.id.list);
        final Activity activity = this;

        Button button = (Button) findViewById(R.id.button_search);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText editText =(EditText)findViewById(R.id.editText_search);
                String str=editText.getText().toString();

                MyList myList = new MyList(list, activity, - 1);
                String head = "http://166.111.68.66:2042/news/action/query/search?keyword="+str+"&pageNo=";
                String tail = "&pageSize=" + Global.PAGE_SIZE;
                myList.initFromURLGenerator(new URLGenerator(head, tail), MyList.NEW);
            }
        });
    }
}
