package com.example.sport_geo_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.fragment.app.Fragment
import com.example.sport_geo_app.R
import com.example.sport_geo_app.ui.fragment.HomeFragment
import com.example.sport_geo_app.ui.fragment.MapFragment
import com.example.sport_geo_app.ui.fragment.RecordWorkoutFragment
import com.example.sport_geo_app.ui.fragment.WorkoutsFragment

class NavigationViewModel : ViewModel() {
    private val _currentFragment = MutableLiveData<Fragment>()
    val currentFragment: LiveData<Fragment> = _currentFragment

    fun navigateTo(menuItemId: Int) {
        val fragment = when (menuItemId) {
            R.id.bottom_home -> HomeFragment()
            R.id.bottom_map -> MapFragment()
            R.id.bottom_workouts -> WorkoutsFragment()
            R.id.bottom_recordWorkout -> RecordWorkoutFragment()
            else -> null
        }
        fragment?.let {
            _currentFragment.value = it
        }
    }
}
