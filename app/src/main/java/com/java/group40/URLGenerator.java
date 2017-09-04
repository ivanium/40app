package com.java.group40;

import java.net.*;

/**
 * Created by zyh_111 on 2017/9/4.
 */

public class URLGenerator {

    private String head;
    private String tail;
    private int page = 1;

    public URLGenerator(String head, String tail) {
        this.head = head;
        this.tail = tail;
    }

    private URL getURL() {
        try {
            URL url = new URL(head + page + tail);
            return url;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public URL firstPage() {
        page = 1;
        return getURL();
    }

    public URL nextPage() {
        page++;
        return getURL();
    }

    public URL prevPage() {
        if (page > 1)
            page--;
        return getURL();
    }

}