package com.example.sport_geo_app.utils
import org.json.JSONException
import org.json.JSONObject

class ErrorManager {
    fun extractErrorMessage(errorJson: String): String {
        return try {
            val jsonObject = JSONObject(errorJson)
            jsonObject.optString("message", "Unknown error occurred")
        } catch (e: JSONException) {
            "Failed to parse error response"
        }
    }
    fun handleErrorResponse(error: Throwable?): String {
        if (error == null) {
            return "Unknown error occurred"
        }
        return error.message?.let { message ->
            extractErrorMessage(message)
        } ?: "Unknown error occurred: ${error.javaClass.simpleName}"
    }
    companion object {
        private const val TAG = "ErrorHandler"
    }
}
