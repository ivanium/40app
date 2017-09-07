package com.java.group40;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

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

}
