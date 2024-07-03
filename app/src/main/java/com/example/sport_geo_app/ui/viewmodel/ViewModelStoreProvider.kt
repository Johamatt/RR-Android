package com.example.sport_geo_app.ui.viewmodel

import androidx.lifecycle.ViewModelStore

object ViewModelStoreProvider {
    private val viewModelStore = ViewModelStore()

    fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }
}

