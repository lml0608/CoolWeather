package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lfs-ios on 2017/4/18.
 */

public class Now {

    @SerializedName("tmp")
    public String temperatrue;

    @SerializedName("cond")
    public More More;

    public class More{

        @SerializedName("txt")
        public String info;
    }
}
