package com.java.group40;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import java.io.*;

import org.json.*;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox mNight;
    private CheckBox mNoImage;
    private CheckBox mVoice;
    private CheckBox mCat[] = new CheckBox[12];
    private Button mOK;
    private Button mResetAll;

    private void emptyCategory() {
        AlertDialog.Builder emptyCat = new AlertDialog.Builder(this);
        emptyCat.setTitle(R.string.empty_category_title);
        emptyCat.setMessage(R.string.empty_category);
        emptyCat.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        emptyCat.create().show();
    }

    private void exportSettings(int tempCat) {
        try {
            JSONObject jSettings = new JSONObject();
            jSettings.put(Global.J_NIGHT, mNight.isChecked());
            jSettings.put(Global.J_NO_IMAGE, mNoImage.isChecked());
            jSettings.put(Global.J_VOICE, mVoice.isChecked());
            jSettings.put(Global.J_CAT, tempCat);
            FileOutputStream __settingsOut = openFileOutput(Global.FILE_SETTINGS, Context.MODE_PRIVATE);
            OutputStreamWriter _settingsOut = new OutputStreamWriter(__settingsOut);
            BufferedWriter settingsOut = new BufferedWriter(_settingsOut);
            settingsOut.write(jSettings.toString());
            settingsOut.flush();
            settingsOut.newLine();
            settingsOut.close();
            _settingsOut.close();
            __settingsOut.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNight = (CheckBox) findViewById(R.id.night);
        mNoImage = (CheckBox) findViewById(R.id.noImage);
        mVoice = (CheckBox) findViewById(R.id.voice);

        mCat[0] = (CheckBox) findViewById(R.id.cat0);
        mCat[1] = (CheckBox) findViewById(R.id.cat1);
        mCat[2] = (CheckBox) findViewById(R.id.cat2);
        mCat[3] = (CheckBox) findViewById(R.id.cat3);
        mCat[4] = (CheckBox) findViewById(R.id.cat4);
        mCat[5] = (CheckBox) findViewById(R.id.cat5);
        mCat[6] = (CheckBox) findViewById(R.id.cat6);
        mCat[7] = (CheckBox) findViewById(R.id.cat7);
        mCat[8] = (CheckBox) findViewById(R.id.cat8);
        mCat[9] = (CheckBox) findViewById(R.id.cat9);
        mCat[10] = (CheckBox) findViewById(R.id.cat10);
        mCat[11] = (CheckBox) findViewById(R.id.cat11);

        mOK = (Button) findViewById(R.id.ok);
        mResetAll = (Button) findViewById(R.id.resetAll);

        mNight.setChecked(Global.night);
        mNoImage.setChecked(Global.noImage);
        mVoice.setChecked(Global.voice);

        for (int i = 0; i < 12; i++) {
            mCat[i].setText(getResources().getStringArray(R.array.category)[i]);
            mCat[i].setChecked(false);
        }

        for (int i = 0; i < Global.catList.size(); i++)
            mCat[Global.catList.get(i)].setChecked(true);

        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tempCat = 0;
                for (int i = 0; i < 12; i++)
                    if (mCat[i].isChecked())
                        tempCat += 1 << i;
                if (tempCat != 0) {
                    Global.newSettings = true;
                    exportSettings(tempCat);
                    finish();
                }
                else
                    emptyCategory();
            }
        });

        mResetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.newSettings = true;
                File fSettings = new File(Global.PATH_SETTINGS);
                if (fSettings.exists())
                    fSettings.delete();
                File fDatabase = new File(Global.PATH_CACHE);
                if (fDatabase.exists())
                    fDatabase.delete();
                File dirDatabase = new File(Global.DIR_CACHE);
                if (dirDatabase.exists())
                    dirDatabase.delete();
                for (int i = 0; i < 12; i++)
                    Global.isLoaded[i] = false;
                finish();
            }
        });
    }

}
