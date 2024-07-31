package com.example.sport_geo_app.ui.fragment

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sport_geo_app.utils.LocationListener
import com.example.sport_geo_app.utils.BitmapUtils
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
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.PlaceMapMarkerModel
import com.example.sport_geo_app.data.network.NetworkService
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.Manifest
import androidx.lifecycle.lifecycleScope


class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationListener: LocationListener
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private lateinit var networkService: NetworkService
    private lateinit var encryptedSharedPreferences: SharedPreferences
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(requireContext())
        networkService = NetworkService(requireContext())
        return inflater.inflate(R.layout.fragment_map, container, false).apply {
            mapView = findViewById(R.id.mapView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.values.all { it }
            if (allPermissionsGranted) {
                initializeMap()
            } else {
                showCustomToast("Permissions not granted")
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
    private fun addClusteredGeoJsonSource(style: Style) {
        networkService.getGeoJson { response, error ->
            if (error != null) {
                Log.e("MapFragment", "Network error: ${error.message}")
                return@getGeoJson
            }

            val geoJsonString = response?.string() ?: run {
                Log.e("MapFragment", "GeoJSON data is null or empty")
                return@getGeoJson
            }

            if (geoJsonString.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.Main) {
                    style.addSource(
                        geoJsonSource(GEOJSON_SOURCE_ID) {
                            data(geoJsonString)
                            cluster(true)
                            maxzoom(14)
                            clusterRadius(50)
                        }
                    )
                    addMapLayers(style)
                }
            } else {
                Log.e("MapFragment", "GeoJSON data is empty")
            }
        }
    }


    private fun addMapLayers(style: Style) {
        style.addLayer(
            symbolLayer("unclustered-points", GEOJSON_SOURCE_ID) {
                iconAllowOverlap(false)
                iconImage(literal(PIN_ID))
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
                                ColorUtils.colorToRgbaString(layers[1][1])
                            ),
                            literal(layers[0][0].toDouble()) to literal(
                                ColorUtils.colorToRgbaString(layers[0][1])
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
                    formatSection(
                        com.mapbox.maps.extension.style.expressions.dsl.generated.toString {
                            get { literal("point_count") }
                        }
                    )
                })
                textSize(12.0)
                textColor(Color.WHITE)
                textIgnorePlacement(true)
                textAllowOverlap(true)
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
                    "clusters" -> handleClusterClick(values)
                    "unclustered-points" -> handleUnclusteredPointClick(values)
                }
            }
        }
    }

    private fun handleClusterClick(values: Feature) {
        (values.geometry() as? Point)?.let { coordinates ->
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
    private fun handleUnclusteredPointClick(values: Feature) {
        (values.geometry() as? Point)?.let { coordinates ->
            val viewAnnotation = viewAnnotationManager.addViewAnnotation(
                resId = R.layout.point_info_layout,
                options = viewAnnotationOptions {
                    geometry(coordinates)
                }
            )

            val placeMarker = Gson().fromJson(values.properties().toString(), PlaceMapMarkerModel::class.java)
            val name = placeMarker.name_fi
            val address = placeMarker.katuosoite
            val type = placeMarker.liikuntapaikkatyyppi

            viewAnnotation.apply {
                findViewById<TextView>(R.id.point_name).text = name
                findViewById<TextView>(R.id.point_address).text = address
                findViewById<TextView>(R.id.point_type).text = type
                findViewById<Button>(R.id.mark_workout_button).setOnClickListener {
                    markWorkoutButtonClick(values)
                }
                findViewById<Button>(R.id.info_button).setOnClickListener {
                    handleInfoButtonClick(name, address, type)
                }
            }
        }
    }

    private fun handleInfoButtonClick(name: String, address: String, type: String) {
        val infoFragment = InfoFragment.newInstance(name, address, type)
        infoFragment.show(parentFragmentManager, "infoFragment")
    }

    private fun markWorkoutButtonClick(values: Feature) {
        val properties =
            Gson().fromJson(values.properties().toString(), PlaceMapMarkerModel::class.java)
        val user_id = encryptedSharedPreferences.getInt("user_id", -1)

        val createWorkoutFragment = CreateWorkoutFragment.newInstance(
            properties.name_fi,
            properties.katuosoite,
            properties.liikuntapaikkatyyppi,
            properties.place_id,
            user_id
        )
        createWorkoutFragment.show(parentFragmentManager, "createWorkoutFragment")
    }



    companion object {
        private const val GEOJSON_SOURCE_ID = "places"
        private const val PIN_ID = "pin-icon-id"
    }

    private val resourcesAndIds = arrayOf(
        Pair(R.drawable.ic_pin, PIN_ID),
    )
}