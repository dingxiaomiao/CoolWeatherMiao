package com.coolweather.app.util;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import android.text.TextUtils;


public class Utility {
//	解析和处理服务器返回的省级数据
	public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB,String response){
		if(!TextUtils.isEmpty(response)){
			String [] allProvince = response.split(",");
			if(allProvince !=null && allProvince.length >0){
				for (String p:allProvince){
					String [] array =  p.split("\\|");
					Province province = new Province ();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
//	解析和处理服务器返回的市级数据
	public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String [] allCity = response.split(",");
			if(allCity != null && allCity.length > 0){
				for(String c :allCity){
					String [] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
// 解析和处理服务器返回的县级数据
	public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
		if(!TextUtils.isEmpty(response)){
			String [] allCounty = response.split(",");
			for(String c:allCounty){
				String[]array = c.split("\\|");
				County county = new County();
				county.setCityId(cityId);
				county.setCountyCode(array[0]);
				county.setCountyName(array[1]);
				coolWeatherDB.saveCounty(county);
			}
			return true;
		}
		return false;
	}
}
