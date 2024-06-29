package com.example.sport_geo_app.data.network

import okhttp3.*
import java.io.IOException

class AuthService {

    private val client = OkHttpClient()

    fun loginWithEmail(email: String, password: String, callback: (response: Response?, error: IOException?) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()
        val request = Request.Builder()
            .url("http://10.0.2.2:3000/auth/login")
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
            .url("http://10.0.2.2:3000/auth/google")
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
            .url("http://10.0.2.2:3000/auth/register")
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
