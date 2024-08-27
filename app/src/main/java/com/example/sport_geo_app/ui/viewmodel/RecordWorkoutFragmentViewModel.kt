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
class RecordWorkoutFragmentViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _createWorkoutResult = MutableLiveData<Result<ResponseBody>>()
    val createWorkoutResult: LiveData<Result<ResponseBody>> = _createWorkoutResult

    fun createWorkOut(requestBody: RequestBody) {
        viewModelScope.launch {
            val result = workoutRepository.createWorkout(requestBody)
            _createWorkoutResult.value = result
        }
    }

}
