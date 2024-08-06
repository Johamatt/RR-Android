package com.example.sport_geo_app.data.network.workouts
import android.util.Log
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutInterface: WorkoutInterface
) {

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
}


