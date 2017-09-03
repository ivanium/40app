package com.java.group40;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.json.*;

public class MainActivity extends AppCompatActivity {

    private void importDefaultSettings() {
        Global.night = false;
        Global.noImage = false;
        Global.voice = false;
        Global.setCatList(15);
    }

    private void importSettings() {
        try {
            File fileSettings = new File(Global.PATH_SETTINGS);
            if (fileSettings.exists()) {
                InputStream __settingsIn = openFileInput(Global.FILE_SETTINGS);
                InputStreamReader _settingsIn = new InputStreamReader(__settingsIn);
                BufferedReader settingsIn = new BufferedReader(_settingsIn);
                String s = settingsIn.readLine();
                if (s != "") {
                    JSONObject jSettings = new JSONObject(s);
                    Global.night = jSettings.getBoolean(Global.J_NIGHT);
                    Global.noImage = jSettings.getBoolean(Global.J_NO_IMAGE);
                    Global.voice = jSettings.getBoolean(Global.J_VOICE);
                    Global.setCatList(jSettings.getInt(Global.J_CAT));
                }
                else
                    importDefaultSettings();
                settingsIn.close();
                _settingsIn.close();
                __settingsIn.close();
            }
            else
                importDefaultSettings();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        importSettings();

        if (Global.night)
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Global.newSettings) {
            Global.newSettings = false;
            mTabLayout.getTabAt(0).select();
            recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            final PullToRefreshListView list = (PullToRefreshListView) rootView.findViewById(R.id.list);
            final Activity activity = this.getActivity();

            final ArrayList<String> titleList = new ArrayList<String>();
            final ArrayList<String> idList = new ArrayList<String>();

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(activity, NewsActivity.class);
                    intent.putExtra("id", idList.get((int) id));
                    startActivity(intent);
                }
            });

            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        try {
                            String s = String.valueOf(msg.obj);
                            if (s != "") {
                                JSONObject jObject = new JSONObject(s);
                                JSONArray jNewsList = jObject.getJSONArray("list");
                                for (int i = 0; i < jNewsList.length(); i++) {
                                    JSONObject jNews = jNewsList.getJSONObject(i);
                                    titleList.add(jNews.getString("news_Title"));
                                    idList.add(jNews.getString("news_ID"));
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, titleList);
                            list.setAdapter(adapter);
                        }
                        catch (Exception e) {
                            e.printStackTrace();;
                        }
                    }
                }
            };

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int category = Global.catList.get(getArguments().getInt(ARG_SECTION_NUMBER) - 1) + 1;
                        URL url = new URL("http://166.111.68.66:2042/news/action/query/latest?pageNo=1&pageSize=20&category=" + category);
                        String s = Global.getJson(url);
                        Message msg = handler.obtainMessage(1, s);
                        handler.sendMessage(msg);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return Global.catList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.category)[Global.catList.get(position)];
        }
    }
}
