package com.example.sport_geo_app.ui.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sport_geo_app.data.repository.GeoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapFragmentViewModel @Inject constructor(
    private val geoRepository: GeoRepository,
) : ViewModel() {

    private val _geoDataResults = MutableLiveData<Result<String>>()
    val geoDataResults: LiveData<Result<String>> = _geoDataResults

    fun getGeoJson() {
        viewModelScope.launch {
            val result = geoRepository.getGeoJson()
            _geoDataResults.value = result
        }
    }

    fun searchGeoJson(search: String) {
        viewModelScope.launch {
            val result = geoRepository.searchGeoJson(search)
            _geoDataResults.value = result
        }
    }
}