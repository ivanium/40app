package com.java.group40;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Created by zyh_111 on 2017/9/2.
 */

public class Global {
    public static final String J_NIGHT = "night";
    public static final String J_NO_IMAGE = "noimage";
    public static final String J_VOICE = "voice";
    public static final String J_CAT = "category";
    public static final String FILE_SETTINGS = "settings.json";
    public static final String PATH_SETTINGS = "/data/data/com.java.group40/files/settings.json";

    public static boolean newSettings = false;
    public static boolean night;
    public static boolean noImage;
    public static boolean voice;
    public static ArrayList<Integer> catList = new ArrayList<Integer>();

    public static void setCatList(int cat) {
        catList.clear();
        for (int i = 0; i < 12; i++)
            if ((cat & (1 << i)) != 0)
                catList.add(i);
    }

    public static String getJson(URL url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            if (conn.getResponseCode() == 200) {
                InputStream __netIn = conn.getInputStream();
                InputStreamReader _netIn = new InputStreamReader(__netIn);
                BufferedReader netIn = new BufferedReader(_netIn);
                String s = netIn.readLine();
                netIn.close();
                _netIn.close();
                __netIn.close();
                return s;
            }
            else
                return "";
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
