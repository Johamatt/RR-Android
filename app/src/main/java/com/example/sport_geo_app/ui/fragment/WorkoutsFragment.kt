package com.example.sport_geo_app.ui.fragment
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.Workout
import com.example.sport_geo_app.ui.viewmodel.WorkoutViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.ResponseBody
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutsFragment : Fragment() {

    private val workOutViewModel: WorkoutViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutsAdapter: WorkoutsAdapter
    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences
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
                    workoutsAdapter.submitList(workoutsData)
                } catch (e: Exception) {
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
        return try {
            val gson = Gson()
            val json = responseBody.string()
            val workoutListType = object : TypeToken<List<Workout>>() {}.type
            gson.fromJson(json, workoutListType)
        } catch (e: Exception) {
            Log.e("WorkoutsFragment", "Error parsing JSON", e)
            emptyList() // Return an empty list in case of error
        }
    }

    //TODO split
    class WorkoutsAdapter(private var workouts: List<Workout>) :
        RecyclerView.Adapter<WorkoutsAdapter.WorkoutViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
            return WorkoutViewHolder(view)
        }

        override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
            val workout = workouts[position]
            holder.workoutIdTextView.text = workout.workout_id.toString()
            holder.nameTextView.text = workout.name
            holder.durationTextView.text = workout.duration
            holder.sportTextView.text = workout.sport
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
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
            val sportTextView: TextView= itemView.findViewById(R.id.sportTextView)

      //      val coordinatesTextView: TextView = itemView.findViewById(R.id.coordinatesTextView)
        }
    }
}