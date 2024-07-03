package com.example.sport_geo_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    var userId: Int = -1
    var userEmail: String = ""
    var userPoints: String = ""
    var userCountry: String = ""
}


