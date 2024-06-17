package com.example.sport_geo_app.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    val userId = MutableLiveData<Int>()
    val userEmail = MutableLiveData<String>()
    val userPoints = MutableLiveData<String>()

    fun setUserId(id: Int) {
        userId.value = id
    }

    fun setUserEmail(email: String) {
        userEmail.value = email
    }

    fun setUserPoints(points: String) {
        userPoints.value = points
    }
}
