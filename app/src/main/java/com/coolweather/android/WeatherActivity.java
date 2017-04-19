package com.coolweather.android;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.SpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView mWeatherLayout;
    private LinearLayout mForecastLayout;
    private TextView mTitleCityTextView;
    private TextView mTitleUpdateTextView;
    private TextView mDegreeTextView;
    private TextView mWeatherInfoTextView;
    private TextView mAqiTextView;
    private TextView mPm25TextView;
    private TextView mComfortTextView;
    private TextView mCarWashTextView;
    private TextView mSportTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initUI();

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//        String weatherString = prefs.getString("weather", "");

        String weatherString = SpUtil.getStoredWeather(getApplicationContext());

        if (!TextUtils.isEmpty(weatherString)) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {

            String weatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    /**
     * 根据id请求城市天气信息
     * @param weatherId
     */
    private void requestWeather(String weatherId) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=f7b257faef4741da9c20626d7aab5d76";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
//                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                                    .edit();
//
//                            editor.putString("weather", responseText);
//                            editor.commit();

                            SpUtil.setStoredWeather(getApplicationContext(), responseText);

                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(getApplicationContext(),"获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });



    }

    /**
     * 处理并展示数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperatrue + "C";
        String weatherInfo = weather.now.more.info;
        //设置控件内容
        mTitleCityTextView.setText(cityName);
        mTitleUpdateTextView.setText(updateTime);
        mDegreeTextView.setText(degree);
        mWeatherInfoTextView.setText(weatherInfo);
        mForecastLayout.removeAllViews();
        for (Forecast forecast : weather.mForecasts) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, mForecastLayout, false);

            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            mForecastLayout.addView(view);

            if (weather.aqi != null) {
                mAqiTextView.setText(weather.aqi.city.aqi);
                mPm25TextView.setText(weather.aqi.city.pm25);
            }

            String comfort = "舒适度：" + weather.suggestion.comfort.info;
            String carWash = "洗车指数：" + weather.suggestion.carWash.info;
            String sport = "运动建议：" + weather.suggestion.sport.info;

            mComfortTextView.setText(comfort);

            mCarWashTextView.setText(carWash);
            mSportTextView.setText(sport);

            mWeatherLayout.setVisibility(View.VISIBLE);



        }

    }

    private void initUI() {

        mWeatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        mForecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        mTitleCityTextView = (TextView) findViewById(R.id.title_city);
        mTitleUpdateTextView = (TextView) findViewById(R.id.title_update_time);
        mDegreeTextView = (TextView) findViewById(R.id.degree_text);
        mWeatherInfoTextView = (TextView) findViewById(R.id.weather_info_text);
        mAqiTextView = (TextView) findViewById(R.id.aqi_text);
        mPm25TextView = (TextView) findViewById(R.id.pm25_text);
        mComfortTextView = (TextView) findViewById(R.id.comfort_text);
        mCarWashTextView = (TextView) findViewById(R.id.car_wash_text);
        mSportTextView = (TextView) findViewById(R.id.sport_text);
    }
}
