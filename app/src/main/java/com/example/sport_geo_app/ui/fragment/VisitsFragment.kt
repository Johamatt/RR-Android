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
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.CoordinatesPoint
import com.example.sport_geo_app.data.model.PlaceModel
import com.example.sport_geo_app.data.model.Workout
import com.example.sport_geo_app.data.network.main.NetworkService
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.ResponseBody
import org.json.JSONArray
import java.text.ParseException
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutsAdapter: WorkoutsAdapter
    @Inject lateinit var encryptedSharedPreferences: SharedPreferences
    @Inject lateinit var networkService: NetworkService
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workouts, container, false)

   //     encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(requireContext())
   //     networkService = NetworkService(requireContext())


        val userId = encryptedSharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
            fetchWorkoutsData(userId, view)
        } else {
            showCustomToast("Unknown error occurred")
        }

        return view
    }

    private fun fetchWorkoutsData(userId: Int, view: View) {
     //   val networkService = NetworkService(requireContext())
        networkService.getWorkouts(userId) { response, error ->
            when {
                error != null -> showCustomToast(error.message)
                response != null -> {
                    val workoutsData = parseWorkoutsResponse(response)
                    initializeRecyclerView(view, workoutsData)
                }
                else -> showCustomToast("Unknown error occurred")
            }
        }
    }
    private fun showCustomToast(message: String?) {
        val layoutInflater = layoutInflater
        val layout: View = layoutInflater.inflate(
            R.layout.custom_toast, requireView().findViewById(R.id.custom_toast_container)
        )
        val textView: TextView = layout.findViewById(R.id.custom_toast_message)
        textView.text = message ?: "An unknown error occurred"
        with(Toast(requireContext())) {
            duration = Toast.LENGTH_LONG
            view = layout
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }



    private fun parseWorkoutsResponse(responseBody: ResponseBody): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val jsonArray = JSONArray(responseBody.string())


        for (i in 0 until jsonArray.length()) {
            Log.d("WorkoutsFragment", jsonArray.toString(i))

            val jsonObject = jsonArray.getJSONObject(i)
            val placeObject = jsonObject.getJSONObject("place")
            Log.d("WorkoutsFragment",placeObject.getJSONObject("point_coordinates").toString())
            val coordinatesArray = placeObject.getJSONObject("point_coordinates").getJSONArray("coordinates")

            val coordinates = CoordinatesPoint(
                type = placeObject.getJSONObject("point_coordinates").getString("type"),
                coordinates = listOf(coordinatesArray.getDouble(0), coordinatesArray.getDouble(1))
            )


            //TODO requestModels
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
                www = null,
            )

            val workout = Workout(
                workout_id = jsonObject.getInt("workout_id"),
                createdAt = jsonObject.getString("created_at"),
                place = place
            )

            workouts.add(workout)
        }

        return workouts
    }

    private fun initializeRecyclerView(view: View, workoutsData: List<Workout>) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        workoutsAdapter = WorkoutsAdapter(workoutsData)
        recyclerView.adapter = workoutsAdapter
    }
}



class WorkoutsAdapter(private val workouts: List<Workout>) : RecyclerView.Adapter<WorkoutsAdapter.WorkoutViewHolder>() {

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
        var formattedDate: String
        try {
            val date = inputFormat.parse(dateString)
            formattedDate = if (date != null) outputFormat.format(date) else ""
        } catch (e: ParseException) {
            e.printStackTrace()
            formattedDate = ""
        }
        return formattedDate
    }

}


