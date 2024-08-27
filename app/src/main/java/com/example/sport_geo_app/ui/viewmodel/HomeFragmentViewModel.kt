package com.example.sport_geo_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sport_geo_app.data.model.WorkOutsTotalResponse
import com.example.sport_geo_app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject


@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _getWorkoutsTotalResult = MutableLiveData<Result<WorkOutsTotalResponse>>()
    val getWorkoutsTotalResult: LiveData<Result<WorkOutsTotalResponse>> = _getWorkoutsTotalResult

    fun getWorkoutsTotal(userId: Int) {
        viewModelScope.launch {
            val result = workoutRepository.getWorkoutsTotal(userId)
            _getWorkoutsTotalResult.value = result
        }
    }
}
