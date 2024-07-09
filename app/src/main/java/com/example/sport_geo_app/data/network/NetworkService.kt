package com.example.sport_geo_app.data.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.sport_geo_app.R
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import org.json.JSONException
import org.json.JSONObject


// TODO change to Retrofit
class NetworkService(private val context: Context) {
    private val EC2PublicIP = context.getString(R.string.EC2_PUBLIC_IP)
    private val encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(context)

    fun checkProximity(
        requestBody: JSONObject,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val token = encryptedSharedPreferences.getString("jwtToken", null)

        val request = object : JsonObjectRequest(
            Method.POST,
            "$EC2PublicIP/places/check-proximity",
            requestBody,
            { response ->
                val isNearby = response.getBoolean("isNearby")
                onSuccess(isNearby)
            },
            { error ->
                handleError(error, onError)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                token?.let {
                    headers["Authorization"] = "Bearer $it"
                }
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        addToRequestQueue(request)
    }

    fun claimReward(
        requestBody: JSONObject,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val token = encryptedSharedPreferences.getString("jwtToken", null)
        val request = object : JsonObjectRequest(
            Method.POST,
            "$EC2PublicIP/visits",
            requestBody,
            { response ->
                onSuccess()
            },
            { error ->
                handleError(error, onError)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                token?.let {
                    headers["Authorization"] = "Bearer $it"
                }
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        addToRequestQueue(request)
    }

    private fun addToRequestQueue(request: Request<JSONObject>) {
        Volley.newRequestQueue(context).add(request)
    }

    private fun handleError(error: VolleyError, onError: (String) -> Unit) {
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
}
