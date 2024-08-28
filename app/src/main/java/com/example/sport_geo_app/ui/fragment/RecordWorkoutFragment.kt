package com.example.sport_geo_app.ui.fragment
import androidx.fragment.app.Fragment
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.example.sport_geo_app.R
import com.example.sport_geo_app.ui.fragment.dialog.CreateWorkoutDialogFragment
import android.os.SystemClock
import android.widget.Chronometer
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sport_geo_app.utils.LocationListener
import com.example.sport_geo_app.utils.simplifyPoints
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

class RecordWorkoutFragment : Fragment() {

    var TAG = "RecordWorkoutFragment"
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var finishButton: Button
    private lateinit var resumeButton: Button
    private lateinit var distanceTextView: TextView
    private lateinit var speedTextView: TextView
    private lateinit var chronometer: Chronometer
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationListener: LocationListener
    private lateinit var mapView: MapView
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var lastLocation: Location? = null

    private var distanceTravelled = 0.0f
    private var isRunning = false
    private var isPaused = false
    private var pausedTime: Long = 0
    private val locationList = mutableListOf<Point>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_record_workout, container, false)
        initializeUI(view)
        setupButtonListeners()
        setupPermissions()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        updateButtonVisibility(startVisible = true)
        return view
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


    private fun initializeUI(view: View) {
        mapView = view.findViewById(R.id.mapView)
        startButton = view.findViewById(R.id.buttonStart)
        stopButton = view.findViewById(R.id.buttonStop)
        finishButton = view.findViewById(R.id.buttonFinish)
        resumeButton = view.findViewById(R.id.buttonResume)
        distanceTextView = view.findViewById(R.id.textViewDistance)
        speedTextView = view.findViewById(R.id.textViewSpeed)
        chronometer = view.findViewById(R.id.chronometer)

    }

    private fun setupButtonListeners() {
        startButton.setOnClickListener { startRecording() }
        stopButton.setOnClickListener { stopRecording() }
        resumeButton.setOnClickListener { resumeRecording() }
        finishButton.setOnClickListener { finishRecording() }
    }

    private fun startRecording() {
        updateButtonVisibility(stopVisible = true)
        distanceTravelled = 0.0f
        chronometer.base = SystemClock.elapsedRealtime() - pausedTime
        chronometer.start()
        isRunning = true
        isPaused = false

        if (!hasLocationPermissions()) return

        val locationRequest = createLocationRequest()
        startLocationUpdates(locationRequest)
    }

    private fun stopRecording() {
        updateButtonVisibility(finishVisible = true, resumeVisible = true)
        fusedLocationClient.removeLocationUpdates(locationCallback)

        if (isRunning) {
            chronometer.stop()
            pausedTime = SystemClock.elapsedRealtime() - chronometer.base
            isRunning = false
        }
        isPaused = true

        updateSpeed()
    }

    private fun resumeRecording() {
        updateButtonVisibility(stopVisible = true)
        if (isPaused) {
            chronometer.base = SystemClock.elapsedRealtime() - pausedTime
            chronometer.start()
            isRunning = true
            isPaused = false
        }

        if (!hasLocationPermissions()) return

        val locationRequest = createLocationRequest()
        startLocationUpdates(locationRequest)
    }

    private fun finishRecording() {
        updateButtonVisibility(startVisible = true)
        if (isRunning) {
            chronometer.stop()
            pausedTime = SystemClock.elapsedRealtime() - chronometer.base
            isRunning = false
        }

        val elapsedTimeMillis = SystemClock.elapsedRealtime() - chronometer.base
        val elapsedTimeSeconds = elapsedTimeMillis / 1000
        val hours = (elapsedTimeSeconds / 3600).toInt()
        val minutes = ((elapsedTimeSeconds % 3600) / 60).toInt()
        val seconds = (elapsedTimeSeconds % 60).toInt()

        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)


        val simplifiedPoints = simplifyPoints(locationList, 0.0001)
        val lineString = LineString.fromLngLats(simplifiedPoints)

        val createWorkoutDialogFragment = CreateWorkoutDialogFragment.newInstance(formattedTime, distanceTravelled, lineString)
        createWorkoutDialogFragment.show(parentFragmentManager, "CreateWorkoutDialogFragment")

        resetWorkoutData()
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).apply {
            setMinUpdateIntervalMillis(5000)
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

    private fun updateLocation(location: Location) {
        if (lastLocation != null) {
            distanceTravelled += lastLocation!!.distanceTo(location)
        }
        lastLocation = location
        val point = Point.fromLngLat(location.longitude, location.latitude)
        locationList.add(point)

        distanceTextView.text = String.format("%.2f m", distanceTravelled)
        updateSpeed()
    }

    private fun updateSpeed() {
        val elapsedTime = (SystemClock.elapsedRealtime() - chronometer.base) / 1000.0
        val avgSpeed = if (elapsedTime > 0) distanceTravelled / elapsedTime else 0.0
        speedTextView.post {
            speedTextView.text = String.format("%.2f m/s", avgSpeed)
        }
    }

    private fun resetWorkoutData() {
        pausedTime = 0
        distanceTravelled = 0.0f
        distanceTextView.text = "0.00 km"
        speedTextView.text = "0.00 m/s"
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.stop()
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
