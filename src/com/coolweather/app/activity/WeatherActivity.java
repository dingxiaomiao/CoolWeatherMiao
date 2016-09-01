package com.coolweather.app.activity;

import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import com.example.coolweather.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.Window;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	private Button switch_city;
	private Button refresh_weather;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		switch_city = (Button)findViewById(R.id.switch_city);
		refresh_weather = (Button)findViewById(R.id.refresh_weather);
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switch_city.setOnClickListener(this);
		refresh_weather.setOnClickListener(this);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			publishText.setText("同步中。。。。。。。。。。。。。。");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);

		} else {
			showWeather();// 如果countyCode为空，则调用显示天气函数,这里是如果是地级市的意思，那么就直接显示天气
		}

	}
	@Override
	public void onClick(View v){
		switch(v.getId()){
		case R.id.refresh_weather:
			publishText.setText("正在努力加载中。。。。。。。。。。");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weatherCode", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
			default:
				break;
		}
	}

	// 查询县级代码对应的天气代号
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");

	}


	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(final String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// 从服务器返回的数据中解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// 处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}
		});
	}
	// 从SharedPreferences文件中读出存储的天气信息，然后显示到界面上
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);

	}
}
