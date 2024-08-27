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
import com.example.sport_geo_app.data.model.WorkoutsGetResponse
import com.example.sport_geo_app.ui.viewmodel.WorkoutsFragmentViewModel
import com.example.sport_geo_app.utils.Constants.USER_ID_KEY
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.ResponseBody
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutsFragment : Fragment() {

    private val workoutsFragmentViewModel: WorkoutsFragmentViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutsAdapter: WorkoutsAdapter
    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences

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

        workoutsFragmentViewModel.getWorkoutsResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { workoutsData ->
                try {
                    workoutsAdapter.submitList(workoutsData)
                } catch (e: Exception) {
                    Log.e("WorkoutsFragment", "Error updating UI", e)
                }
            }.onFailure { throwable ->
                Log.e("WorkoutsFragment", "Failed to get workouts", throwable)
            }
        }

        val userId = encryptedSharedPreferences.getInt(USER_ID_KEY, -1)
        if (userId != -1) {
            workoutsFragmentViewModel.getWorkouts(userId)
        } else {
            //    User ID not found
        }

        return view
    }

    class WorkoutsAdapter(private var workouts: List<WorkoutsGetResponse>) :
        RecyclerView.Adapter<WorkoutsAdapter.WorkoutViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
            return WorkoutViewHolder(view)
        }

        override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
            val workout = workouts[position]
            holder.nameTextView.text = workout.name
            holder.timeTextView.text = workout.time
            holder.sportTextView.text = workout.sport
            holder.distanceMetersTextView.text = String.format("%.2f", workout.distanceMeters)
        }

        override fun getItemCount(): Int {
            return workouts.size
        }

        fun submitList(newWorkouts: List<WorkoutsGetResponse>) {
            workouts = newWorkouts
            notifyDataSetChanged()
        }

        inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
            val sportTextView: TextView= itemView.findViewById(R.id.sportTextView)
            val distanceMetersTextView: TextView = itemView.findViewById(R.id.distanceMetersTextView)

        }
    }
    companion object {
        private const val TAG = "WorkoutsFragment"
    }
}
