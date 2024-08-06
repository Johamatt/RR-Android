package com.example.sport_geo_app.ui.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
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
import com.example.sport_geo_app.data.network.workouts.WorkoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// TODO data models
@AndroidEntryPoint
class CreateWorkoutFragment : DialogFragment() {
    private var placeName: String? = null
    private var placeAddress: String? = null
    private var placeType: String? = null
    private var placeId: String? = null
    private var userId: Int = 0
    private lateinit var selectedDateTextView: TextView
    private lateinit var selectedTimeTextView: TextView
    private val workOutViewModel: WorkoutViewModel by viewModels()
    private var TAG = "CreateWorkoutFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeId = it.getString(ARG_PLACE_ID)
            userId = it.getInt(ARG_USER_ID)
            placeName = it.getString(ARG_PLACE_NAME)
            placeAddress = it.getString(ARG_PLACE_ADDRESS)
            placeType = it.getString(ARG_PLACE_TYPE)
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
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_createworkout, container, false).apply {
            findViewById<TextView>(R.id.workout_place_name).text = placeName
            findViewById<TextView>(R.id.workout_place_address).text = placeAddress
            findViewById<TextView>(R.id.workout_place_type).text = placeType
            selectedDateTextView = findViewById(R.id.selected_date)
            selectedTimeTextView = findViewById(R.id.selected_time)

            findViewById<Button>(R.id.save_button).setOnClickListener {
                val updatedPlaceName = findViewById<TextView>(R.id.workout_place_name).text.toString()
                val updatedPlaceAddress = findViewById<TextView>(R.id.workout_place_address).text.toString()
                val updatedPlaceType = findViewById<TextView>(R.id.workout_place_type).text.toString()
                val duration = findViewById<EditText>(R.id.workout_duration).text.toString()
                val intensity = findViewById<EditText>(R.id.workout_intensity).text.toString()
                val sportType = findViewById<EditText>(R.id.workout_sport_type).text.toString()
                val notes = findViewById<EditText>(R.id.workout_notes).text.toString()

                val isoDateTime = combineDateAndTime(
                    selectedDateTextView.text.toString(),
                    selectedTimeTextView.text.toString()
                )

                saveWorkoutDetails(
                    placeId,
                    userId,
                    updatedPlaceName,
                    updatedPlaceAddress,
                    updatedPlaceType,
                    duration,
                    isoDateTime,
                    intensity,
                    sportType,
                    notes
                )
            }

            findViewById<Button>(R.id.cancel_button).setOnClickListener {
                dismiss()
            }

            findViewById<Button>(R.id.workout_date_button).setOnClickListener {
                showDatePicker()
            }

            findViewById<Button>(R.id.workout_time_button).setOnClickListener {
                showTimePicker()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                val date = "$year-${month + 1}-" + String.format("%02d", dayOfMonth)
                selectedDateTextView.text = date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                selectedTimeTextView.text = time
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun combineDateAndTime(date: String, time: String): String {
        val dateTimeString = "$date$time"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateTime = inputFormat.parse(dateTimeString)
            dateTime?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    //TODO data models
    private fun saveWorkoutDetails(
        placeId: String?,
        userId: Int,
        name: String,
        address: String,
        type: String,
        duration: String,
        dateTime: String,
        intensity: String,
        sport: String,
        notes: String
    ) {
        val jsonObject = JSONObject().apply {
            put("place_id", placeId)
            put("user_id", userId)
//            put("name", name)
//            put("address", address)
//            put("type", type)
//            put("duration", duration)
//            put("date_time", dateTime)
//            put("intensity", intensity)
//            put("sport", sport)
//            put("notes", notes)
        }
        val requestBody = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        Log.d(TAG, "Request body: $requestBody") // Log request body
        workOutViewModel.createWorkOut(requestBody)

    }

    private fun showCustomToast(message: String) {
        //TODO
    }

    companion object {
        private const val ARG_PLACE_NAME = "place_name"
        private const val ARG_PLACE_ADDRESS = "place_address"
        private const val ARG_PLACE_TYPE = "place_type"
        private const val ARG_PLACE_ID = "place_id"
        private const val ARG_USER_ID = "user_id"

        @JvmStatic
        fun newInstance(name: String, address: String, type: String, placeId: String, userId: Int) =
            CreateWorkoutFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PLACE_NAME, name)
                    putString(ARG_PLACE_ADDRESS, address)
                    putString(ARG_PLACE_TYPE, type)
                    putString(ARG_PLACE_ID, placeId)
                    putInt(ARG_USER_ID, userId)
                }
            }
    }
}



