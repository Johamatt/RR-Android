package com.example.sport_geo_app.data.network


import android.content.Context
import androidx.core.content.ContextCompat.getString
import com.example.sport_geo_app.R
import okhttp3.*
import java.io.IOException


// TODO change to Retrofit
class AuthService(private val context: Context) {

    private val client = OkHttpClient()
    val EC2PublicIP = context.getString(R.string.EC2_PUBLIC_IP)

    fun loginWithEmail(email: String, password: String, callback: (response: Response?, error: IOException?) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()


        val request = Request.Builder()
            .url("$EC2PublicIP/auth/login")  // Ensure webClientId is a String
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }
            override fun onResponse(call: Call, response: Response) {
                callback(response, null)
            }
        })
    }

    fun sendTokenToBackend(idToken: String, callback: (response: Response?, error: IOException?) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("idToken", idToken)
            .build()
        val request = Request.Builder()
            .url("$EC2PublicIP/auth/google")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response, null)
            }
        })
    }

    fun registerUser(email: String, password: String, callback: (response: Response?, error: IOException?) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()
        val request = Request.Builder()
            .url("$EC2PublicIP/auth/register")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response, null)
            }
        })
    }
}
