package com.example.topoint.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "서울 시청 위치"
    }
    val text: LiveData<String> = _text

    private val _location = MutableLiveData<LatLng>().apply {
        value = LatLng(37.5662952, 126.97794509999994)
    }

    val location: LiveData<LatLng> = _location

    fun updateText(newText: String) {
        _text.value = newText
    }

    fun updateLocation(newLocation: LatLng) {
        _location.value = newLocation
    }

    fun loadData() {
        _text.value = "새로운 위치"
        _location.value = LatLng(37.5662952, 126.97794509999994)
    }
}