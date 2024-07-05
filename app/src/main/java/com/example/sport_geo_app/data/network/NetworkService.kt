package com.example.sport_geo_app.data.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.sport_geo_app.R
import org.json.JSONException
import org.json.JSONObject

class NetworkService(private val context: Context) {

    val webClientId = context.getString(R.string.WEB_CLIENT_ID)
    val EC2PublicIP = context.getString(R.string.EC2_PUBLIC_IP)
    fun checkProximity(requestBody: JSONObject, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        val request = JsonObjectRequest(
            Request.Method.POST,
            "$EC2PublicIP/places/check-proximity",
            requestBody,
            { response ->
                val isNearby = response.getBoolean("isNearby")
                onSuccess(isNearby)
            },
            { error ->
                if (error.networkResponse != null) {
                    val errorResponse = String(error.networkResponse.data)
                    Log.e("Volley Error", errorResponse)
                    try {
                        val jsonObject = JSONObject(errorResponse)
                        val errorMessage = jsonObject.getJSONArray("message").getString(0)
                        onError(errorMessage)
                    } catch (e: JSONException) {
                        onError("Failed to parse error message")
                    }
                } else {
                    Log.e("Volley Error", error.message ?: "Unknown error")
                    onError(error.message ?: "Unknown error")
                }
            }

        )
        Volley.newRequestQueue(context).add(request)
    }

    fun claimReward(requestBody: JSONObject, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val request = JsonObjectRequest(
            Request.Method.POST,
            "$EC2PublicIP/visits",
            requestBody,
            { response ->
                onSuccess()
            },
            { error ->
                if (error.networkResponse != null) {
                    val errorResponse = String(error.networkResponse.data)
                    Log.e("Volley Error", errorResponse)
                    try {
                        val jsonObject = JSONObject(errorResponse)
                        val errorMessage = jsonObject.getJSONArray("message").getString(0)
                        onError(errorMessage)
                    } catch (e: JSONException) {
                        onError("Failed to parse error message")
                    }
                } else {
                    Log.e("Volley Error", error.message ?: "Unknown error")
                    onError(error.message ?: "Unknown error")
                }
            }

        )
        Volley.newRequestQueue(context).add(request)
    }
}
