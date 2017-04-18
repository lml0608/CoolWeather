package com.coolweather.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by lfs-ios on 2017/4/7.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;


    private ProgressDialog mProgressDialog;

    private TextView mTitleText;

    private Button mBackButton;

    private ListView mListView;

    private ArrayAdapter<String> mAdapter;


    private List<String> dataList = new ArrayList<>();

    private List<Province> mProvinceList;

    private List<City> mCityList;

    private List<County> mCountyList;

    //被选择点击的Province对象
    private Province selectedProvince;
    //被选择点击的City对象
    private City selectedCity;

    //当前所处的级别 0升级，1市级，2县级
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);

        mTitleText = (TextView) view.findViewById(R.id.title_text);
        mBackButton = (Button) view.findViewById(R.id.back_button);
        mListView = (ListView) view.findViewById(R.id.list_view);

        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (currentLevel == LEVEL_PROVINCE) {
                    //currentLevel = 0，，当前显示的省级数据
                    selectedProvince = mProvinceList.get(position);
                    //获取被选中的省包含的市的数据
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    //currentLevel = 1，，当前显示的市级数据
                    selectedCity = mCityList.get(position);
                    //获取被选中的市包含的县的数据
                    queryCounties();
                }
            }
        });

        //返回按钮，
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    //当前页面currentLevel = 2，按返回按钮，则显示市的数据
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    //当前页面currentLevel = 1，按返回按钮，则显示省的数据
                    queryProvinces();
                }
            }
        });
        //页面加载时默认显示省级名称
        queryProvinces();
    }


    /**
     * 获取显示省级名称 并listview展示
     */
    private void queryProvinces() {
        //标题显示国家
        mTitleText.setText("中国");
        //后退按钮不显示
        mBackButton.setVisibility(View.GONE);
        //省级数据列表，先从数据库查找放进mProvinceList，如果数据库没有保存，再去网络请求数据
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            //数据库有数据时，情况原有的dataList数据，从mProvinceList读取省的名称放入dataList列表，
            dataList.clear();

            for (Province province : mProvinceList) {

                dataList.add(province.getProvinceName());
            }
            //刷新适配器
            mAdapter.notifyDataSetChanged();

            mListView.setSelection(0);
            //当前水平为0，代表省
            currentLevel = LEVEL_PROVINCE;
        } else {
            //如果数据库没有保存，再去网络请求数据,请求url
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }

    }

    /**
     * 查询市的名称，并listview展示
     */
    private void queryCities() {
        //当前选择的省的名称
        mTitleText.setText(selectedProvince.getProvinceName());
        //返回按钮可见
        mBackButton.setVisibility(View.VISIBLE);
        //查询City表，找出符合provinceid的省下所有市的对象
        mCityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);

        if (mCityList.size() > 0) {

            dataList.clear();
            for (City city : mCityList) {

                dataList.add(city.getCityName());
            }

            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            //获取当前选择的省的代码provinceCode,用于拼接请求的url
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询县名称 并listview展示
     */
    private void queryCounties() {

        mTitleText.setText(selectedCity.getCityName());

        mBackButton.setVisibility(View.VISIBLE);
        mCountyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);

        if (mCountyList.size() > 0) {

            dataList.clear();
            for (County county : mCountyList) {

                dataList.add(county.getCountyName());
            }

            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            //获取当前选择的省的代码provinceCode,市的cityCode，用于拼接请求的url
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 连接网络请求数据
     * @param address 请求地址
     * @param type 类型，省，市， 县
     */
    private void queryFromServer(String address, final String type) {
        //显示进度弹框
        showProgressDialog();

        //网络请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //网络请求返回的String数据
                String responseText = response.body().string();

                boolean result = false;

                if ("province".equals(type)) {
                    //查询省的数据，解析后插入数据库，返回true表示操作成功
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    //查询市的数据，解析后插入数据库，返回true表示操作成功
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    //查询县的数据，解析后插入数据库，返回true表示操作成功
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                //为真表示获取数据并保存成功，调用更新数据方法去更新UI
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //进度弹框消失
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            }else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }


            }
            @Override
            public void onFailure(Call call, IOException e) {

                //更新主线程
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();

                        Toast.makeText(getContext(), "加载失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });
    }

    /**
     * 关闭加载进度弹框
     */
    private void closeProgressDialog() {

        if (mProgressDialog != null) {

            mProgressDialog.dismiss();
        }
    }

    /**
     * 定义加载进度弹框
     */
    private void showProgressDialog() {

        if (mProgressDialog == null) {

            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            //不受点击弹框外部事件影响
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();

    }


}
