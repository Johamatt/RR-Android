package com.example.sport_geo_app.ui.fragment.Dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.WorkoutCreate
import com.example.sport_geo_app.ui.viewmodel.WorkoutViewModel
import com.google.gson.Gson
import com.mapbox.geojson.Point
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class CreateWorkoutDialogFragment : DialogFragment() {
    private var name: String? = null
    private var duration: String? = null
    private var sport: String? = null
    private var userId: Int = 0
    private var coordinates: Point? = null
    private val workOutViewModel: WorkoutViewModel by viewModels()
    private val TAG = "CreateWorkoutDialogFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_NAME)
            userId = it.getInt(ARG_USER_ID)
            val coordinatesJson = it.getString(ARG_COORDINATES)
            coordinates = coordinatesJson?.let { json ->
                Point.fromJson(json)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workOutViewModel.createWorkoutResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { responseBody ->
                Log.d(TAG, responseBody.toString())
                dismiss()
            }.onFailure { throwable ->
                Log.d(TAG, throwable.toString())
                // TODO errormanager
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_createworkout, container, false).apply {
            findViewById<TextView>(R.id.workout_name).text = name
            findViewById<TextView>(R.id.workout_duration).text = duration
            findViewById<TextView>(R.id.workout_sport).text = sport
            findViewById<Button>(R.id.save_button).setOnClickListener {
                val name = findViewById<TextView>(R.id.workout_name).text.toString()
                val duration = findViewById<EditText>(R.id.workout_duration).text.toString()
                val sport = findViewById<EditText>(R.id.workout_sport).text.toString()
                saveWorkoutDetails(
                    userId,
                    name,
                    duration,
                    sport,
                    coordinates
                )
            }
            findViewById<Button>(R.id.cancel_button).setOnClickListener {
                dismiss()
            }
        }
    }

    private fun saveWorkoutDetails(
        userId: Int,
        name: String,
        duration: String,
        sport: String,
        coordinates: Point?
    ) {
        val gson = Gson()

        val workoutRequest = WorkoutCreate(
            userId = userId,
            name = name,
            duration = duration,
            sport = sport,
            pointCoordinates = coordinates
        )

        val jsonRequest = gson.toJson(workoutRequest)
        val requestBody = jsonRequest.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        workOutViewModel.createWorkOut(requestBody)
    }

    private fun showCustomToast(message: String) {
        //TODO
    }

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_USER_ID = "user_id"
        private const val ARG_COORDINATES = "coordinates"

        @JvmStatic
        fun newInstance(name: String, coordinatesJson: String, userId: Int) =
            CreateWorkoutDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putString(ARG_COORDINATES, coordinatesJson)
                    putInt(ARG_USER_ID, userId)
                }
            }
    }
}

