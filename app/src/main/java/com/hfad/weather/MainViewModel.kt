package com.hfad.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hfad.weather.adapters.WeatherModel

class MainViewModel:ViewModel() {

    val liveDataCurrent = MutableLiveData<WeatherModel>()
    val liveDataList = MutableLiveData<List<WeatherModel>>()
}