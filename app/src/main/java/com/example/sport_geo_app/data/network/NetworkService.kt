package com.example.sport_geo_app.data.network


import android.content.Context
import android.util.Log
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.utils.AuthInterceptor
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkService(private val context: Context) {
    private val EC2PublicIP = context.getString(R.string.EC2_PUBLIC_IP)
    private val encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(context)

    private val retrofit: Retrofit

    init {
        val token = encryptedSharedPreferences.getString("jwtToken", null)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(EC2PublicIP)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: NetworkInterface = retrofit.create(NetworkInterface::class.java)



    fun checkProximity(
        requestBody: JSONObject,
        callback: (response: ResponseBody?, error: Throwable?) -> Unit) {

        val body = requestBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val call = apiService.checkProximity(body)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = response.message()
                    callback(null, Throwable(errorBody ?: errorMessage ?: "Unknown error occurred"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(null, t)
            }
        })
    }

    fun claimReward(
        requestBody: JSONObject,
        callback: (response: ResponseBody?, error: Throwable?) -> Unit) {


        val body = requestBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val call = apiService.claimReward(body)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                Log.d("MapFragment", response.toString())
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = response.message()
                    callback(null, Throwable(errorBody ?: errorMessage ?: "Unknown error occurred"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(null, t)
            }
        })
    }
}
