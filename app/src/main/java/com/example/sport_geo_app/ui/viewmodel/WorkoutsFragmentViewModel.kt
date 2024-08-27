package com.example.sport_geo_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sport_geo_app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class WorkoutsFragmentViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _getWorkoutsResult = MutableLiveData<Result<ResponseBody>>()
    val getWorkoutsResult: LiveData<Result<ResponseBody>> = _getWorkoutsResult

    fun getWorkouts(userId: Int) {
        viewModelScope.launch {
            val result = workoutRepository.getWorkouts(userId)
            _getWorkoutsResult.value = result
        }
    }

}
