package com.example.sport_geo_app.data.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class NetworkService(private val context: Context) {

    fun checkProximity(requestBody: JSONObject, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        val request = JsonObjectRequest(
            Request.Method.POST,
            "http://10.0.2.2:3000/places/check-proximity",
            requestBody,
            { response ->
                val isNearby = response.getBoolean("isNearby")
                onSuccess(isNearby)
            },
            { error ->
                onError(error.message ?: "Unknown error")
            }
        )
        Volley.newRequestQueue(context).add(request)
    }

    fun claimReward(requestBody: JSONObject, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val request = JsonObjectRequest(
            Request.Method.POST,
            "http://10.0.2.2:3000/visits",
            requestBody,
            { response ->
                onSuccess()
            },
            { error ->
                onError(error.message ?: "Unknown error")
            }
        )
        Volley.newRequestQueue(context).add(request)
    }
}
