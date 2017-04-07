package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by lfs-ios on 2017/4/7.
 */

public class HttpUtil {

    /**
     * 向服务器发起网络请求，获取数据
     * @param address  请求的url地址
     * @param callback
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(address).build();

        client.newCall(request).enqueue(callback);

    }
}
