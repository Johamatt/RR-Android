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
import com.example.sport_geo_app.data.network.NetworkService
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class SettingsActivity : AppCompatActivity() {

    private lateinit var countrySpinner: Spinner
    private lateinit var saveCountryBtn: Button
    private lateinit var encryptedSharedPreferences: SharedPreferences
    private lateinit var networkService: NetworkService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(this)
        networkService = NetworkService(this)
        setContentView(R.layout.activity_settings)

        initializeViews()
        initializeData()
        setupListeners()
    }

    private fun initializeViews() {
        countrySpinner = findViewById(R.id.country_spinner)
        saveCountryBtn = findViewById(R.id.save_country_btn)

        val countries = resources.getStringArray(R.array.countries)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.adapter = adapter
    }

    private fun initializeData() {
        val userCountry = encryptedSharedPreferences.getString("user_country", null)
        userCountry?.let {
            val position = (countrySpinner.adapter as? ArrayAdapter<String>)?.getPosition(it)
            if (position != -1) {
                countrySpinner.setSelection(position!!)
            }
        }
    }

    private fun setupListeners() {
        saveCountryBtn.setOnClickListener {
            val selectedCountry = countrySpinner.selectedItem.toString()
            save(selectedCountry)
        }
    }

    private fun save(country: String) {
        val userId = encryptedSharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
            val requestBody = JSONObject().apply {
                put("user_id", userId)
                put("country", country)
            }

            networkService.updateUserCountry(requestBody) { response, error ->
                runOnUiThread {
                    if (error != null) {
                        handleErrorResponse(error)
                    } else {
                        if (response != null) {
                            val jsonObject = JSONObject(response.string())
                            val countryFromResponse = jsonObject.getString("country")
                            val editor = encryptedSharedPreferences.edit()
                            editor.putString("user_country", countryFromResponse)
                            editor.apply()
                            navigateToMainActivity()
                        } else {
                            showMessage("update failed")
                        }
                    }
                }
            }
        } else {
            showMessage("User ID not found")
        }
    }

    private fun handleErrorResponse(error: Throwable?) {
        val errorMessage = error?.message
        val msg = errorMessage?.let { message ->
            try {
                val jsonObject = JSONObject(message)
                jsonObject.getString("message")
            } catch (e: JSONException) {
                e.printStackTrace()
                "Failed to parse error response"
            }
        } ?: "Unknown error occurred: ${error?.javaClass?.simpleName ?: "Unknown"}"

        runOnUiThread {
            showMessage(msg)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}


