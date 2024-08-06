package com.example.sport_geo_app.ui.fragment
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.CoordinatesPoint
import com.example.sport_geo_app.data.model.PlaceModel
import com.example.sport_geo_app.data.model.Workout
import com.example.sport_geo_app.data.network.workouts.WorkoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.ResponseBody
import org.json.JSONArray
import java.text.ParseException
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutsFragment : Fragment() {

    private val workOutViewModel: WorkoutViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutsAdapter: WorkoutsAdapter
    @Inject lateinit var encryptedSharedPreferences: SharedPreferences
    var TAG = "WorkoutsFragment"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workouts, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        workoutsAdapter = WorkoutsAdapter(emptyList())
        recyclerView.adapter = workoutsAdapter

        workOutViewModel.getWorkoutsResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { responseBody ->
                try {
                    val workoutsData = parseWorkoutsResponse(responseBody)
                    Log.d(TAG, workoutsData.toString())
                    workoutsAdapter.submitList(workoutsData)
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
             //       showCustomToast("Failed to parse data")
                    //TODO inject errormanager
                }
            }.onFailure { throwable ->
         //       throwable
            }
        }

        val userId = encryptedSharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
            workOutViewModel.getWorkouts(userId)
        } else {
        //    User ID not found
        }

        return view
    }



    private fun parseWorkoutsResponse(responseBody: ResponseBody): List<Workout> {
        val workouts = mutableListOf<Workout>()
        try {
            val jsonArray = JSONArray(responseBody.string())
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val placeObject = jsonObject.getJSONObject("place")
                val coordinatesArray = placeObject.getJSONObject("point_coordinates").getJSONArray("coordinates")

                val coordinates = CoordinatesPoint(
                    type = placeObject.getJSONObject("point_coordinates").getString("type"),
                    coordinates = listOf(coordinatesArray.getDouble(0), coordinatesArray.getDouble(1))
                )

                val place = PlaceModel(
                    place_id = placeObject.getString("place_id"),
                    name_fi = placeObject.getString("name_fi"),
                    lisätieto = placeObject.getString("lisätieto"),
                    point_coordinates = coordinates,
                    liikuntapaikkatyyppi = placeObject.getString("liikuntapaikkatyyppi"),
                    liikuntapaikkatyypinalaryhmä = placeObject.getString("liikuntapaikkatyypinalaryhmä"),
                    liikuntapaikkatyypinpääryhmä = placeObject.getString("liikuntapaikkatyypinpääryhmä"),
                    linestring_coordinates = null,
                    markkinointinimi = null,
                    muokattu_viimeksi = null,
                    polygon_coordinates = null,
                    puhelinnumero = null,
                    sähköposti = null,
                    www = null
                )

                val workout = Workout(
                    workout_id = jsonObject.getInt("workout_id"),
                    createdAt = jsonObject.getString("created_at"),
                    place = place
                )

                workouts.add(workout)
            }
        } catch (e: Exception) {
            Log.e("WorkoutsFragment", "Error parsing JSON", e)
        }
        return workouts
    }
}

//TODO split
class WorkoutsAdapter(private var workouts: List<Workout>) : RecyclerView.Adapter<WorkoutsAdapter.WorkoutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]

        holder.workoutIdTextView.text = workout.workout_id.toString()
        holder.createdAtTextView.text = formatDate(workout.createdAt)
        holder.nameTextView.text = workout.place.name_fi
        holder.descriptionTextView.text = workout.place.lisätieto
        holder.coordinatesTextView.text = workout.place.point_coordinates.toString()
    }

    override fun getItemCount(): Int {
        return workouts.size
    }

    fun submitList(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutIdTextView: TextView = itemView.findViewById(R.id.workoutIdTextView)
        val createdAtTextView: TextView = itemView.findViewById(R.id.createdAtTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val coordinatesTextView: TextView = itemView.findViewById(R.id.coordinatesTextView)
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return try {
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }
    }
}