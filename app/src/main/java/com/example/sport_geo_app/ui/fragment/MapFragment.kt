package com.example.sport_geo_app.ui.fragment
import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.PointPin
import com.example.sport_geo_app.di.Toaster
import com.example.sport_geo_app.ui.fragment.dialog.BottomSheetFragment
import com.example.sport_geo_app.ui.viewmodel.MapFragmentViewModel
import com.example.sport_geo_app.utils.BitmapUtils
import com.example.sport_geo_app.utils.LocationListener
import com.google.gson.Gson
import com.mapbox.geojson.Feature
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
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.utils.ColorUtils
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private lateinit var locationListener: LocationListener
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    @Inject lateinit var toaster: Toaster
    private val mapStyles = listOf(
        Style.MAPBOX_STREETS,
        Style.SATELLITE,
        Style.DARK
    )
    private var currentMapStyleIndex = 0


    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences

    private val mapFragmentViewModel: MapFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false).apply {
            mapView = findViewById(R.id.mapView)
        }
    }

    private fun initializeButtonListeners(view: View) {
        val maptypeButton: ImageButton = view.findViewById(R.id.mapTypeButton)
        maptypeButton.setOnClickListener {
            toggleMapStyle()
        }

        val locationButton: ImageButton = view.findViewById(R.id.locationButton)
        locationButton.setOnClickListener {
            moveToCurrentLocation()
        }
    }

    private fun toggleMapStyle() {
        currentMapStyleIndex = (currentMapStyleIndex + 1) % mapStyles.size
        mapView.mapboxMap.loadStyle(mapStyles[currentMapStyleIndex]) {
            setupLocationListener()
            setupViewAnnotationManager()
            mapFragmentViewModel.getGeoJson()
            setupMapClickListener()
            addMapImages()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPermissions()
        setupObservers()
        initializeButtonListeners(view)

        val searchBar: EditText = view.findViewById(R.id.searchBar)
        searchBar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val searchString = v.text.toString()
                if (searchString.length >= 2) {
                    mapFragmentViewModel.searchGeoJson(searchString)
                }
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun moveToCurrentLocation() {
        val location = locationListener.getCurrentLocation()
        if (location != null) {
            val cameraPosition = CameraOptions.Builder()
                .center(location)
                .build()
            mapView.mapboxMap.setCamera(cameraPosition)
        } else {
            toaster.showToast("current location is null")
        }
    }

    private fun setupPermissions() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.values.all { it }
            if (allPermissionsGranted) {
                initializeMap()
            } else {
                toaster.showToast("Location permission not granted")
            }
        }

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setupObservers() {
        mapFragmentViewModel.geoDataResults.observe(viewLifecycleOwner) { result ->
            result.onSuccess { geoJsonString ->
                mapView.mapboxMap.getStyle { style ->
                    if (style != null) {
                        addClusteredGeoJsonSource(style, geoJsonString)
                    } else {
                        Log.e(TAG, "Map style is not loaded yet")
                    }
                }
            }.onFailure { throwable ->
                toaster.showToast(throwable.toString())
            }
        }
    }

    private fun initializeMap() {
        mapView.mapboxMap.apply {
            setCamera(CameraOptions.Builder().zoom(10.0).pitch(0.0).build())
            loadStyle(Style.MAPBOX_STREETS) {
                setupLocationListener()
                setupViewAnnotationManager()
                mapFragmentViewModel.getGeoJson()
                setupMapClickListener()
                addMapImages()
            }
        }
    }

    private fun setupLocationListener() {
        locationListener = LocationListener(mapView).apply {
            setupGesturesListener(mapView)
            initLocationComponent(mapView)
        }
    }

    private fun setupViewAnnotationManager() {
        viewAnnotationManager = mapView.viewAnnotationManager
    }

    private fun setupMapClickListener() {
        mapView.mapboxMap.addOnMapClickListener { point ->
            handleMapClick(point)
            true
        }
    }

    private fun addMapImages() {
        for ((drawableRes, id) in resourcesAndIds) {
            BitmapUtils.bitmapFromDrawableRes(requireContext(), drawableRes)
                ?.let { bitmap ->
                    mapView.mapboxMap.style?.addImage(id, bitmap, true)
                }
        }
    }

    private fun addClusteredGeoJsonSource(style: Style, geoJsonString: String) {
        if (geoJsonString.isNotEmpty()) {
            val existingSource = style.getSource(GEOJSON_SOURCE_ID)
            if (existingSource != null) {
                style.removeStyleLayer(UNCLUSTERED_LAYER_ID)
                style.removeStyleLayer(CLUSTER_LAYER_ID)
                style.removeStyleLayer(COUNT_LAYER_ID)
                style.removeStyleSource(GEOJSON_SOURCE_ID)
            }
            style.addSource(
                geoJsonSource(GEOJSON_SOURCE_ID) {
                    data(geoJsonString)
                    cluster(true)
                    maxzoom(14)
                    clusterRadius(50)
                }
            )
            addMapLayers(style)
        } else {
            toaster.showToast("GeoJSON data is empty")
        }
    }





    private fun addMapLayers(style: Style) {
        style.addLayer(
            symbolLayer(UNCLUSTERED_LAYER_ID, GEOJSON_SOURCE_ID) {
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
            circleLayer(CLUSTER_LAYER_ID, GEOJSON_SOURCE_ID) {
                circleColor(
                    Expression.step(
                        input = get("point_count"),
                        output = literal(ColorUtils.colorToRgbaString(layers[2][1])),
                        stops = arrayOf(
                            literal(layers[1][0].toDouble()) to literal(ColorUtils.colorToRgbaString(layers[1][1])),
                            literal(layers[0][0].toDouble()) to literal(ColorUtils.colorToRgbaString(layers[0][1]))
                        )
                    )
                )
                circleRadius(18.0)
                filter(has("point_count"))
            }
        )

        style.addLayer(
            symbolLayer(COUNT_LAYER_ID, GEOJSON_SOURCE_ID) {
                textField(format {
                    formatSection(
                        com.mapbox.maps.extension.style.expressions.dsl.generated.toString { get { literal("point_count") } }
                    )
                })
                textSize(12.0)
                textColor(Color.WHITE)
                textIgnorePlacement(true)
                textAllowOverlap(true)
            }
        )
    }

    private fun handleMapClick(point: Point) {
        if (!::viewAnnotationManager.isInitialized) return

        val screenPoint = mapView.mapboxMap.pixelForCoordinate(point)
        viewAnnotationManager.removeAllViewAnnotations()

        mapView.mapboxMap.queryRenderedFeatures(
            RenderedQueryGeometry(screenPoint),
            RenderedQueryOptions(listOf(UNCLUSTERED_LAYER_ID, CLUSTER_LAYER_ID), null)
        ) { features ->
            features.value?.firstOrNull()?.let { feature ->
                val layer = feature.layers.getOrNull(0)
                val values = feature.queriedFeature.feature

                when (layer) {
                    CLUSTER_LAYER_ID -> handleClusterClick(values)
                    UNCLUSTERED_LAYER_ID -> handleUnclusteredPointClick(values)
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
        val placeMarker = Gson().fromJson(values.properties().toString(), PointPin::class.java)
        val name = placeMarker.name_fi
        val address = placeMarker.katuosoite
        val type = placeMarker.liikuntapaikkatyyppi

        val bottomSheetFragment = BottomSheetFragment.newInstance(name, address, type)
        bottomSheetFragment.show(parentFragmentManager, "bottomSheetFragment")
    }




    companion object {
        private val GEOJSON_SOURCE_ID = "places"
        private val COUNT_LAYER_ID = "count"
        private val CLUSTER_LAYER_ID = "clusters"
        private val UNCLUSTERED_LAYER_ID = "unclustered-points"
        private const val PIN_ID = "pin-icon-id"
        private const val TAG = "MapFragment"
    }

    private val resourcesAndIds = arrayOf(
        Pair(R.drawable.ic_pin, PIN_ID),
    )
}
