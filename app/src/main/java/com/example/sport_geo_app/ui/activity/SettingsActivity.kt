package com.example.sport_geo_app.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SettingsActivity : AppCompatActivity() {

    private lateinit var countrySpinner: Spinner
    private lateinit var saveCountryBtn: Button
    private val client = OkHttpClient()
    private lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        countrySpinner = findViewById(R.id.country_spinner)
        saveCountryBtn = findViewById(R.id.save_country_btn)

        val countries = resources.getStringArray(R.array.countries)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.adapter = adapter

        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(this)


        val userCountry = encryptedSharedPreferences.getString("user_country", null)
        userCountry?.let {
            val position = countries.indexOf(it)
            if (position != -1) {
                countrySpinner.setSelection(position)
            }
        }

        saveCountryBtn.setOnClickListener {
            val selectedCountry = countrySpinner.selectedItem.toString()
            save(selectedCountry)
        }
    }

    private fun save(country: String) {
        val userId = encryptedSharedPreferences.getInt("user_id", -1)
        val token = encryptedSharedPreferences.getString("jwtToken", null)
        Log.d("SettingsActivity", token.toString())
        if (userId == -1 || token == null) {
            Toast.makeText(this, "User ID or JWT Token not found", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "${getString(R.string.EC2_PUBLIC_IP)}/users/$userId/country"

        val json = JSONObject()
        json.put("country", country)
        val requestBody =
            json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .patch(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to save", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        val editor = encryptedSharedPreferences.edit()
                        editor.putString("user_country", country)
                        editor.apply()

                        navigateToMainActivity()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to save", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

