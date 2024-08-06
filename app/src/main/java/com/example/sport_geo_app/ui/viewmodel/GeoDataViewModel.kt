package com.example.sport_geo_app.ui.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sport_geo_app.data.repository.GeoDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeoDataViewModel @Inject constructor(
    private val geoDataRepository: GeoDataRepository,
) : ViewModel() {

    private val _geoDataResults = MutableLiveData<Result<String>>()
    val geoDataResults: LiveData<Result<String>> = _geoDataResults

    fun getGeoJson() {
        viewModelScope.launch {
            val result = geoDataRepository.getGeoJson()
            _geoDataResults.value = result
        }
    }
}