package com.example.sport_geo_app.ui.fragment.dialog

import android.content.SharedPreferences
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
import com.example.sport_geo_app.data.model.WorkoutCreateRequest
import com.example.sport_geo_app.ui.viewmodel.RecordWorkoutFragmentViewModel
import com.example.sport_geo_app.utils.Constants.USER_ID_KEY
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mapbox.geojson.LineString
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@AndroidEntryPoint
class CreateWorkoutDialogFragment : DialogFragment() { //TODO change to material dialog
    private var name: String? = null
    private var time: String = ""
    private var distanceMeters: Int = 0
    private var sport: String? = null
    private var linestring: LineString? = null
    private val recordWorkoutFragmentViewModel: RecordWorkoutFragmentViewModel by viewModels()
    private val gson = Gson()
    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            time = it.getString(ARG_TIME).toString()
            distanceMeters = it.getInt(ARG_DISTANCE_TRAVELLED)
            val coordinatesJson = it.getString(ARG_LINESTRING)
            linestring = coordinatesJson?.let { json ->
                LineString.fromJson(json)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recordWorkoutFragmentViewModel.createWorkoutResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { responseBody ->
                Log.d(TAG, responseBody.toString())
                dismiss()
            }.onFailure { throwable ->
                Log.d(TAG, throwable.toString())
                // TODO: Handle error
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_createworkout, container, false).apply {
            findViewById<TextView>(R.id.workout_name).text = name
            findViewById<TextView>(R.id.workout_sport).text = sport
            findViewById<TextView>(R.id.workout_distanceTravelled).text = String.format(
                distanceMeters.toString()
            )
            findViewById<TextView>(R.id.workout_time).text = time
            findViewById<Button>(R.id.save_button).setOnClickListener {
                val name = findViewById<TextView>(R.id.workout_name).text.toString()
                val sport = findViewById<EditText>(R.id.workout_sport).text.toString()
                saveWorkoutDetails(
                    name,
                    time,
                    distanceMeters,
                    sport,
                    linestring
                )
            }
            findViewById<Button>(R.id.cancel_button).setOnClickListener {
                dismiss()
            }
        }
    }

    private fun saveWorkoutDetails(
        name: String,
        time: String,
        distanceMeters: Int,
        sport: String,
        lineString: LineString?
    ) {


        val userId = encryptedSharedPreferences.getInt(USER_ID_KEY, -1)

        // Create JSON object for LineString
        val lineStringJson = JsonObject().apply {
            addProperty("TYPE", "LineString")
            add("coordinates", lineString?.let {
                val coordinatesArray = JsonArray()
                it.coordinates().forEach { point ->
                    val coordinatePair = JsonArray().apply {
                        add(point.longitude())
                        add(point.latitude())
                    }
                    coordinatesArray.add(coordinatePair)
                }
                coordinatesArray
            })
        }

        val workoutRequest = WorkoutCreateRequest(
            userId = userId,
            name = name,
            time = time,
            distanceMeters = distanceMeters,
            sport = sport,
            linestring_coordinates = lineStringJson
        )

        val jsonRequest = gson.toJson(workoutRequest)
        val requestBody = jsonRequest.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        recordWorkoutFragmentViewModel.createWorkOut(requestBody)
    }


    private fun showCustomToast(message: String) {
        // TODO
    }

    companion object {
        private const val ARG_LINESTRING = "linestring"
        private const val ARG_TIME = "time"
        private const val ARG_DISTANCE_TRAVELLED = "distanceTravelled"
        private val TAG = "CreateWorkoutDialogFragment"

        @JvmStatic
        fun newInstance(time: String, distanceTravelled: Int, lineString: LineString) =
            CreateWorkoutDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TIME, time)
                    putInt(ARG_DISTANCE_TRAVELLED, distanceTravelled)
                    putString(ARG_LINESTRING, lineString.toJson()) // Add LineString to arguments
                }
            }
    }
}

