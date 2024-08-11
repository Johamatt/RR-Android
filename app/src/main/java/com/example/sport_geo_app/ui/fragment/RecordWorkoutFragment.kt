package com.example.sport_geo_app.ui.fragment
import androidx.fragment.app.Fragment
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.example.sport_geo_app.R
import com.example.sport_geo_app.utils.LocationListener
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import java.util.concurrent.TimeUnit

class RecordWorkoutFragment : Fragment() {

    var TAG = "RecordWorkoutFragment"
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var finishButton: Button
    private lateinit var resumeButton: Button
    private lateinit var distanceTextView: TextView
    private lateinit var speedTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var mapView: MapView
    private lateinit var locationListener: LocationListener
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    private var distanceTravelled = 0.0f
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_record_workout, container, false)
        initializeUI(view)
        setupButtonListeners()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        updateButtonVisibility(startVisible = true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPermissions()
    }

    private fun initializeUI(view: View) {
        mapView = view.findViewById(R.id.mapView)
        startButton = view.findViewById(R.id.buttonStart)
        stopButton = view.findViewById(R.id.buttonStop)
        finishButton = view.findViewById(R.id.buttonFinish)
        resumeButton = view.findViewById(R.id.buttonResume)
        distanceTextView = view.findViewById(R.id.textViewDistance)
        speedTextView = view.findViewById(R.id.textViewSpeed)
        timeTextView = view.findViewById(R.id.textViewTime)
    }

    private fun setupButtonListeners() {
        startButton.setOnClickListener { startRecording() }
        stopButton.setOnClickListener { stopRecording() }
        resumeButton.setOnClickListener { resumeRecording() }
        finishButton.setOnClickListener { finishRecording() }
    }

    private fun setupPermissions() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                initializeMap()
            } else {
                // Handle permissions not granted
            }
        }

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun initializeMap() {
        mapView.mapboxMap.apply {
            setCamera(CameraOptions.Builder().zoom(10.0).pitch(0.0).build())
            loadStyle(Style.STANDARD) {
                setupLocationListener()
            }
        }
    }

    private fun setupLocationListener() {
        locationListener = LocationListener(mapView).apply {
            setupGesturesListener(mapView)
            initLocationComponent(mapView)
        }
    }

    private fun startRecording() {
        updateButtonVisibility(stopVisible = true)
        distanceTravelled = 0.0f
        startTime = System.currentTimeMillis() - pausedTime

        startElapsedTimeUpdate()
        if (!hasLocationPermissions()) return

        val locationRequest = createLocationRequest()
        startLocationUpdates(locationRequest)
    }

    private fun stopRecording() {
        updateButtonVisibility(finishVisible = true, resumeVisible = true)
        stopLocationUpdates()

        stopElapsedTimeUpdate()
        pausedTime = System.currentTimeMillis() - startTime

        updateSpeed()
    }

    private fun resumeRecording() {
        updateButtonVisibility(stopVisible = true)
        startTime = System.currentTimeMillis() - pausedTime

        startElapsedTimeUpdate()
        if (!hasLocationPermissions()) return

        val locationRequest = createLocationRequest()
        startLocationUpdates(locationRequest)
    }

    private fun finishRecording() {
        updateButtonVisibility(startVisible = true)
        stopElapsedTimeUpdate()
        resetWorkoutData()
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).apply {
            setMinUpdateIntervalMillis(1000)
            setWaitForAccurateLocation(false)
        }.build()
    }

    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationUpdates(locationRequest: LocationRequest) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocation(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.d(TAG, e.toString())
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocation(location: Location) {
        distanceTravelled += location.distanceTo(location)
        distanceTextView.text = String.format("Distance: %.2f m", distanceTravelled)
        updateSpeed()
    }

    private fun updateSpeed() {
        val elapsedTime = System.currentTimeMillis() - startTime
        val avgSpeed = if (elapsedTime > 0) distanceTravelled / (elapsedTime / 1000.0) else 0.0
        speedTextView.text = String.format("Avg Speed: %.2f m/s", avgSpeed)
    }

    private fun startElapsedTimeUpdate() {
        updateTimeRunnable = object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTime
                val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
                val milliseconds = (elapsedTime % 1000) / 10
                timeTextView.text = String.format("Elapsed Time: %02d:%02d.%02d", minutes, seconds, milliseconds)
                handler.postDelayed(this, 10)
            }
        }
        handler.post(updateTimeRunnable!!)
    }

    private fun stopElapsedTimeUpdate() {
        updateTimeRunnable?.let { handler.removeCallbacks(it) }
        updateTimeRunnable = null
    }

    private fun resetWorkoutData() {
        pausedTime = 0
        distanceTravelled = 0.0f
        distanceTextView.text = "Distance: 0.00 m"
        speedTextView.text = "Avg Speed: 0.00 m/s"
        timeTextView.text = "Elapsed Time: 00:00.00"
    }

    private fun updateButtonVisibility(
        startVisible: Boolean = false,
        stopVisible: Boolean = false,
        finishVisible: Boolean = false,
        resumeVisible: Boolean = false
    ) {
        startButton.visibility = if (startVisible) View.VISIBLE else View.GONE
        stopButton.visibility = if (stopVisible) View.VISIBLE else View.GONE
        finishButton.visibility = if (finishVisible) View.VISIBLE else View.GONE
        resumeButton.visibility = if (resumeVisible) View.VISIBLE else View.GONE
    }
}
