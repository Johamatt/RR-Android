package com.example.sport_geo_app.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject

class ErrorManager(private val context: Context) {

    fun extractErrorMessage(errorJson: String): String {
        return try {
            val jsonObject = JSONObject(errorJson)
            jsonObject.optString("message", "Unknown error occurred")
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse error response", e)
            "Failed to parse error response"
        }
    }

    fun handleErrorResponse(error: Throwable?) {
        if (error == null) {
            displayErrorMessage("Unknown error occurred")
            return
        }

        Log.e(TAG, "Error occurred", error)

        val errorMessage = error.message?.let { message ->
            extractErrorMessage(message)
        } ?: "Unknown error occurred: ${error.javaClass.simpleName}"

        displayErrorMessage(errorMessage)
    }

    private fun displayErrorMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "ErrorHandler"
    }
}
