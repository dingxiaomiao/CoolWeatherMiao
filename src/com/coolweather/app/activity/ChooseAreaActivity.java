package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;


import android.text.TextUtils;
import android.view.View;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;
import com.example.coolweather.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();

	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;

	private Province selectedProvince;
	private City selectedCity;
	private County seletedCounty;

	private int currentLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)){
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
				dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if(currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(index);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(index);
					queryCounties();
				}else if(currentLevel == LEVEL_COUNTY){
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();//加载省级数据
	}
//	当第一次进入程序时，没有点击listview,系统会从数据库中加载数据，如果数据库中没有数据，那么就发送网络请求，从服务器进行加载数据
	private void queryProvinces(){
		provinceList = coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromServer(null,"province");
		}
	}
//	根据用户点击省级listView的index，进入指定省份下面的世级菜单
	private void queryCities(){
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size() > 0){
			dataList.clear();
			for(City city :cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
//	根据用户点击市级listview的index，进入指定市级下面的县级菜单
	private void queryCounties(){
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
	}
//	根据传入的代号和类型从服务器中查询省市县数据
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address ="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			@Override
			public void onFinish(String response){
				boolean result = false;
				
				if("province".equals(type)){
					result = Utility.handleProvinceResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result = Utility.handleCityResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result = Utility.handleCountyResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){
					runOnUiThread(new Runnable(){
						@Override
						public void run(){
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
						}
					});
				}
			}
			@Override
			public void onError(Exception e){
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
		
	}
	private void showProgressDialog(){
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载。。。。。");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	private void closeProgressDialog(){
		if(progressDialog !=null){
			progressDialog.dismiss();
		}
	}
	@Override
	public void onBackPressed(){
		if(currentLevel ==LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel == LEVEL_CITY){
			queryProvinces();
		}else{
			finish();
		}
	}
}
