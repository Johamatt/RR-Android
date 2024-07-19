package com.example.sport_geo_app.ui.fragment

import android.content.SharedPreferences
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sport_geo_app.BuildConfig
import com.example.sport_geo_app.utils.LocationListener
import com.example.sport_geo_app.utils.BitmapUtils
import com.example.sport_geo_app.utils.LocationPermissionHelper
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.format
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.expressions.dsl.generated.has
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.utils.ColorUtils
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import java.lang.ref.WeakReference
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.PlaceMapMarkerModel
import com.example.sport_geo_app.data.network.NetworkService
import com.example.sport_geo_app.ui.viewmodel.UserViewModel
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationListener: LocationListener
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private lateinit var userViewModel: UserViewModel
    private lateinit var networkService: NetworkService
    private lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(requireContext())
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        networkService = NetworkService(requireContext())
        return inflater.inflate(R.layout.fragment_map, container, false).apply {
            mapView = findViewById(R.id.mapView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            initializeMap()
        }
    }

    // TODO deprecated
    @Deprecated("This declaration overrides deprecated member but not marked as deprecated itself. Please add @Deprecated annotation or suppress. See https://youtrack.jetbrains.com/issue/KT-47902 for details")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationListener.isInitialized) {
            locationListener.onDestroy(mapView)
        }
    }

    private fun initializeMap() {
        mapView.mapboxMap.apply {
            setCamera(CameraOptions.Builder().zoom(10.0).pitch(0.0).build())
            loadStyle(Style.STANDARD) {
                locationListener = LocationListener(mapView)
                locationListener.setupGesturesListener(mapView)
                locationListener.initLocationComponent(mapView)
                viewAnnotationManager = mapView.viewAnnotationManager
                addClusteredGeoJsonSource(it)
                addOnMapClickListener { point ->
                    handleMapClick(point)
                    true
                }
                for ((drawableRes, id) in resourcesAndIds) {
                    BitmapUtils.bitmapFromDrawableRes(requireContext(), drawableRes)
                        ?.let { bitmap ->
                            it.addImage(id, bitmap, true)
                        }
                }
            }
        }
    }


    // TODO fix mess
    private fun handleMapClick(point: Point) {
        if (!::viewAnnotationManager.isInitialized) {
            return
        }
        val screenPoint = mapView.mapboxMap.pixelForCoordinate(point)
        viewAnnotationManager.removeAllViewAnnotations()
        mapView.mapboxMap.queryRenderedFeatures(
            RenderedQueryGeometry(screenPoint),
            RenderedQueryOptions(listOf("unclustered-points", "clusters"), null)
        ) { features ->
            features.value?.firstOrNull()?.let { feature ->
                val layer = feature.layers.getOrNull(0)
                val values = feature.queriedFeature.feature
                when (layer) {
                    "clusters" -> {
                        val coordinates = values.geometry() as? Point
                        coordinates?.let {
                            val currentZoom = mapView.mapboxMap.cameraState.zoom
                            val newZoom = if (currentZoom >= 12.0) currentZoom + 1 else 12.0
                            mapView.mapboxMap.flyTo(
                                CameraOptions.Builder()
                                    .zoom(newZoom)
                                    .pitch(0.0)
                                    .center(coordinates)
                                    .build(),
                                MapAnimationOptions.Builder()
                                    .duration(1000)
                                    .build()
                            )
                        }
                    }

                    "unclustered-points" -> {
                        val coordinates = values.geometry() as? Point
                        coordinates?.let {
                            val viewAnnotation = viewAnnotationManager.addViewAnnotation(
                                resId = R.layout.point_info_layout,
                                options = viewAnnotationOptions {
                                    geometry(coordinates)
                                }
                            )
                            val placeMarker = Gson().fromJson(values.properties().toString(), PlaceMapMarkerModel::class.java)
                            val name = placeMarker.nameFi
                            val address = placeMarker.katuosoite
                            val type = placeMarker.liikuntapaikkaTyyppi
                            viewAnnotation.findViewById<TextView>(R.id.point_name).text = name
                            viewAnnotation.findViewById<TextView>(R.id.point_address).text = address
                            viewAnnotation.findViewById<TextView>(R.id.point_type).text = type
                            val button = viewAnnotation.findViewById<Button>(R.id.claim_reward_button)
                            val infoButton = viewAnnotation.findViewById<Button>(R.id.info_button)
                            button.setOnClickListener {
                                val currentLocation = locationListener.getCurrentLocation()
                                if (currentLocation != null) {
                                    checkProximityAndClaimReward(currentLocation,  values)
                                } else {
                                    showCustomToast("Unable to get current location")
                                }
                            }
                            infoButton.setOnClickListener {
                                val infoFragment = InfoFragment.newInstance(name, address, type)
                                infoFragment.show(parentFragmentManager, "infoFragment")
                            }
                        }

                    }
                }
            }
        }
    }


    // TODO replace globalscope
    private fun addClusteredGeoJsonSource(style: Style) {
        val userCountry = encryptedSharedPreferences.getString("user_country", null)
        val token = encryptedSharedPreferences.getString("jwtToken", null)

        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("${getString(R.string.EC2_PUBLIC_IP)}/places/country/$userCountry")
                .header("Authorization", "Bearer $token")
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val geoJsonString = response.body?.string()

                    requireActivity().runOnUiThread {
                        style.addSource(
                            geoJsonSource(GEOJSON_SOURCE_ID) {
                                if (!geoJsonString.isNullOrEmpty()) {
                                    data(geoJsonString)
                                }
                                cluster(true)
                                maxzoom(14)
                                clusterRadius(50)
                            }
                        )
                    }
                } else {
                    Log.e("MapFragment", "Unsuccessful response: ${response.code}")
                }
            } catch (e: IOException) {
                Log.e("MapFragment", "Network error: ${e.message}")
            }
        }

        style.addLayer(
            symbolLayer("unclustered-points", GEOJSON_SOURCE_ID) {
                iconAllowOverlap(false)
                iconImage(
                    literal(PIN_ID)
                )
                iconSize(literal(1))
            }
        )

        val layers = arrayOf(
            intArrayOf(150, ContextCompat.getColor(requireContext(), R.color.red)),
            intArrayOf(20, ContextCompat.getColor(requireContext(), R.color.green)),
            intArrayOf(0, ContextCompat.getColor(requireContext(), R.color.blue))
        )

        style.addLayer(
            circleLayer("clusters", GEOJSON_SOURCE_ID) {
                circleColor(
                    Expression.step(
                        input = get("point_count"),
                        output = literal(ColorUtils.colorToRgbaString(layers[2][1])),
                        stops = arrayOf(
                            literal(layers[1][0].toDouble()) to literal(
                                ColorUtils.colorToRgbaString(
                                    layers[1][1]
                                )
                            ),
                            literal(layers[0][0].toDouble()) to literal(
                                ColorUtils.colorToRgbaString(
                                    layers[0][1]
                                )
                            )
                        )
                    )
                )
                circleRadius(18.0)
                filter(has("point_count"))
            }
        )

        style.addLayer(
            symbolLayer("count", GEOJSON_SOURCE_ID) {
                textField(format {
                    formatSection(com.mapbox.maps.extension.style.expressions.dsl.generated.toString {
                        get {
                            literal(
                                "point_count"
                            )
                        }
                    })
                })
                textSize(12.0)
                textColor(Color.WHITE)
                textIgnorePlacement(true)
                textAllowOverlap(true)
            }
        )
    }


    private fun checkProximityAndClaimReward(userCoordinates: Point, values: Feature) {
        val coordinates = (values.geometry() as Point).coordinates()
        val requestBody = JSONObject().apply {
            put("userLatitude", 60.250620) // userCoordinates.latitude()
            put("userLongitude", 24.846747) // userCoordinates.longitude()
            put("markerLatitude", coordinates[1])
            put("markerLongitude", coordinates[0])
        }

        networkService.checkProximity(requestBody) { response, error ->
            if (error != null) {
                showCustomToast("Error: ${error.message}")
            } else {
                val responseBody = response?.string()
                if (responseBody != null) {
                    val isNearby = JSONObject(responseBody).getBoolean("isNearby")
                    if (isNearby) {
                        claimReward(values)
                    } else {
                        showCustomToast("You are not nearby the marker")
                    }
                } else {
                    showCustomToast("Unknown error occurred")
                }
            }
        }
    }

    private fun claimReward(values: Feature) {

        Log.d("MapFragment", values.toString())
        val properties = Gson().fromJson(values.properties().toString(), PlaceMapMarkerModel::class.java)
        val userId = encryptedSharedPreferences.getInt("user_id", -1)

        Log.d("MapFragment", properties.toString())
        val requestBody = JSONObject().apply {
            put("user_id", userId)
            put("placeId", properties.placeId)
        }

        networkService.claimReward(
            requestBody,
            callback = { response, error ->
                if (error != null) {
                    try {
                        val errorJson = JSONObject(error.message)
                        val errorMessage = errorJson.getString("message")
                        showCustomToast(errorMessage)
                    } catch (e: JSONException) {
                        showCustomToast("Error parsing err response")
                    }
                } else {
                    showCustomToast("Reward claimed successfully!")
                }
            }
        )



    }


    private fun showCustomToast(message: String) {
        val layoutInflater = layoutInflater
        val layout: View = layoutInflater.inflate(
            R.layout.custom_toast, requireView().findViewById(
                R.id.custom_toast_container
            ))

        val textView: TextView = layout.findViewById(R.id.custom_toast_message)
        textView.text = message

        // TODO 'setter for view: View?' is deprecated.
        with(Toast(requireContext())) {
            duration = Toast.LENGTH_LONG
            view = layout
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    companion object {
        private const val GEOJSON_SOURCE_ID = "places"
        private const val PIN_ID = "pin-icon-id"
    }

    private val resourcesAndIds = arrayOf(
        Pair(R.drawable.ic_pin, PIN_ID),
    )
}