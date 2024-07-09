package com.example.sport_geo_app.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SettingsActivity : AppCompatActivity() {

    private lateinit var countrySpinner: Spinner
    private lateinit var saveCountryBtn: Button
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        countrySpinner = findViewById(R.id.country_spinner)
        saveCountryBtn = findViewById(R.id.save_country_btn)

        val countries = resources.getStringArray(R.array.countries)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.adapter = adapter

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userCountry = sharedPreferences.getString("user_country", null)
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
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        val token = sharedPreferences.getString("access_token", null)
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
                        val editor = sharedPreferences.edit()
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

