package com.example.sport_geo_app.ui.fragment

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sport_geo_app.utils.LocationPermissionHelper
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import java.lang.ref.WeakReference
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.NetworkService
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.viewannotation.geometry

class RoutePlanFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mapView: MapView
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private lateinit var networkService: NetworkService
    private lateinit var encryptedSharedPreferences: SharedPreferences

    // TODO load from back - Constants
    private val KEY_MARKED_PLACES = "marked_places2"

    // Views and adapters
    private lateinit var recyclerViewMarkedPlaces: RecyclerView
    private lateinit var markedPlacesAdapter: MarkedPlacesAdapter

    // Data
    private val markedPlaces = mutableListOf<MarkedPlace>()

    private var markerNumber = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val view = inflater.inflate(R.layout.fragment_routeplan, container, false)
        mapView = view.findViewById(R.id.mapView)
        recyclerViewMarkedPlaces = view.findViewById(R.id.recyclerViewMarkedPlaces)
        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(requireContext())
        networkService = NetworkService(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            markedPlaces.addAll(loadMarkedPlaces())
            markerNumber = markedPlaces.size
            markedPlacesAdapter = MarkedPlacesAdapter(markedPlaces)
            recyclerViewMarkedPlaces.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = markedPlacesAdapter
            }

                initializeMap()

            updateRecyclerViewVisibility()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun initializeMap() {
        mapView.mapboxMap.apply {
            setCamera(CameraOptions.Builder().zoom(10.0).build())
            loadStyle(Style.STANDARD) { style ->

                    cameraOptions {
                        zoom(15.0)

                }

                viewAnnotationManager = mapView.viewAnnotationManager
                addOnMapClickListener { point ->
                    handleMapClick(point)
                    true
                }
                markedPlaces.forEachIndexed { index, markedPlace ->
                    addMarkedPlaceToMap(markedPlace, index + 1)
                }
            }
        }
    }


    private fun addMarkedPlaceToMap(markedPlace: MarkedPlace, markerNumber: Int) {
        val pinView = LayoutInflater.from(requireContext()).inflate(R.layout.pin_view, null)
        pinView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val markerView = pinView.findViewById<TextView>(R.id.textViewMarkerNumber)
        markerView.text = markerNumber.toString()

        val layoutParams = ViewAnnotationOptions.Builder()
            .geometry(Point.fromLngLat(markedPlace.longitude, markedPlace.latitude))
            .build()

        try {
            viewAnnotationManager.addViewAnnotation(pinView, layoutParams)
            Log.d("RoutePlanFragment", "Marker added with number: $markerNumber")
        } catch (e: Exception) {
            Log.e("RoutePlanFragment", "Error adding marker: ${e.message}")
        }
    }


    private fun saveMarkedPlaces(markedPlaces: List<MarkedPlace>) {
        val json = Gson().toJson(markedPlaces)
        encryptedSharedPreferences.edit {
            putString(KEY_MARKED_PLACES, json)
        }
    }

    private fun loadMarkedPlaces(): List<MarkedPlace> {
        val json = encryptedSharedPreferences.getString(KEY_MARKED_PLACES, null)
        val type = object : TypeToken<List<MarkedPlace>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    private fun updateRecyclerViewVisibility() {
        recyclerViewMarkedPlaces.visibility = if (markedPlaces.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun handleMapClick(point: Point) {
        showNamePointDialog(point)
    }

    private fun showNamePointDialog(point: Point) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_name_point, null)
        val editTextPointName = dialogView.findViewById<EditText>(R.id.editTextPointName)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Name the Point")
            .create()

        buttonSave.setOnClickListener {
            val pointName = editTextPointName.text.toString().trim()
            if (pointName.isNotEmpty()) {
                val markedPlace = MarkedPlace(pointName, point.latitude(), point.longitude())
                markedPlaces.add(markedPlace)
                markedPlacesAdapter.notifyDataSetChanged()
                addMarkedPlaceToMap(markedPlace, markerNumber)
                saveMarkedPlaces(markedPlaces)
                updateRecyclerViewVisibility()
                markerNumber++
                dialog.dismiss()
            } else {
                editTextPointName.error = "Name cannot be empty"
            }
        }

        dialog.show()
    }
}


data class MarkedPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

class MarkedPlacesAdapter(private val markedPlaces: List<MarkedPlace>) : RecyclerView.Adapter<MarkedPlacesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_marked_place, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val markedPlace = markedPlaces[position]
        holder.bind(markedPlace)
    }
    override fun getItemCount(): Int {
        return markedPlaces.size
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(markedPlace: MarkedPlace) {
            itemView.findViewById<TextView>(R.id.textViewPlaceName).text = markedPlace.name

        }
    }
}