package com.example.sport_geo_app.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.main.MainActivity
import com.example.sport_geo_app.R
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("user_id")) {
            val userId = sharedPreferences.getInt("user_id", -1)
            val userEmail = sharedPreferences.getString("user_email", "")
            val userPoints = sharedPreferences.getString("user_points", "")
            if (userId != -1 && !userEmail.isNullOrEmpty() && !userPoints.isNullOrEmpty()) {
                navigateToMainActivity(userId, userEmail, userPoints!!)
                return
            }
        }

        emailInput = findViewById(R.id.register_email_input)
        passwordInput = findViewById(R.id.register_password_input)
        registerBtn = findViewById(R.id.register_btn)

        registerBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        val client = OkHttpClient()
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

                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                    Log.d("asd", response.body.toString())
                    Log.d("asd", response.isSuccessful.toString())
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        handleSuccessResponse(responseBody)
                    }
                } else {
                    runOnUiThread {
                        handleErrorResponse(response)
                    }
                }
            }

        })
    }

    private fun handleSuccessResponse(responseBody: String?) {
        if (responseBody != null) {
            try {
                val jsonObject = JSONObject(responseBody)
                Log.d("asd", jsonObject.toString())

                val userJson = jsonObject.getJSONObject("user")


                val userId = userJson.getInt("user_id")
                val userEmail = userJson.getString("email")
                val userPoints = userJson.getString("points")

                // Save login information to SharedPreferences
                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("user_id", userId)
                editor.putString("user_email", userEmail)
                editor.putString("user_points", userPoints)
                editor.apply()

                navigateToMainActivity(userId, userEmail, userPoints)
            } catch (e: Exception) {
                e.printStackTrace()
                displayErrorMessage("Failed to parse user info")
            }
        }
    }


    private fun handleErrorResponse(response: Response) {
        val responseBody = response.body?.string()
        val jsonObject = JSONObject(responseBody)
        val errorMessage = jsonObject.getJSONArray("message")

        Log.d("asd", response.code.toString())
        when (response.code) {
            400 -> {
                displayErrorMessage(errorMessage.toString())
            }
            401 -> {
                displayErrorMessage(errorMessage.toString())
            }
            409 -> {
                displayErrorMessage(errorMessage.toString())
            }
            else -> {
                displayErrorMessage("Authentication failed: ${response.message}")
            }
        }
    }
    private fun displayErrorMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity(userId: Int, userEmail: String, userPoints: String) {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        intent.putExtra("user_id", userId)
        intent.putExtra("user_email", userEmail)
        intent.putExtra("user_points", userPoints)
        startActivity(intent)
        finish()
    }
}
