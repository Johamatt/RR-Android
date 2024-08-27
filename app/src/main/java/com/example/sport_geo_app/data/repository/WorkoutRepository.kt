package com.example.sport_geo_app.data.repository
import android.util.Log
import com.example.sport_geo_app.data.model.WorkOutsTotalResponse
import com.example.sport_geo_app.data.source.WorkoutInterface
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutInterface: WorkoutInterface
) {

    private val gson = Gson()

    suspend fun getWorkoutsTotal(userId: Int): Result<WorkOutsTotalResponse> {
        return try {
            val response = workoutInterface.getWorkoutsTotal(userId)
            if (response.isSuccessful) {

                val responseBody = response.body()
                if (responseBody != null) {
                    val jsonResponse = responseBody.string()
                    Log.d(TAG, "Raw JSON Response: $jsonResponse")

                    try {
                        val workOutsTotal = gson.fromJson(jsonResponse, WorkOutsTotalResponse::class.java)
                        Result.success(workOutsTotal)
                    } catch (e: JsonSyntaxException) {
                        Result.failure(Throwable("Error parsing JSON"))
                    }
                } else {
                    Result.failure(Throwable("Empty response body"))
                }
            } else {
                Result.failure(Throwable(response.errorBody()?.string() ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkouts(userId: Int): Result<ResponseBody> {
        return try {
            val response = workoutInterface.getWorkouts(userId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: "".toResponseBody(null))
            } else {
                Result.failure(Throwable(response.errorBody()?.string() ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    suspend fun createWorkout(requestBody: RequestBody): Result<ResponseBody> {
        return try {
            val response = workoutInterface.createWorkout(requestBody)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                Log.e("WorkoutRepository", "Error response: $errorBody") // Log error response
                Result.failure(Throwable(errorBody))
            }
        } catch (e: Exception) {
            Log.e("WorkoutRepository", "Exception: ${e.message}", e) // Log exception
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "WorkoutRepository"
    }
}


