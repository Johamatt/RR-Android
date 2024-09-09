package com.example.sport_geo_app.ui.fragment
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.Workout
import com.example.sport_geo_app.di.Toaster
import com.example.sport_geo_app.ui.activity.LoginActivity
import com.example.sport_geo_app.ui.viewmodel.HomeFragmentViewModel
import com.example.sport_geo_app.utils.Constants.USER_ID_KEY
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var signOutBtn: Button
    @Inject lateinit var encryptedSharedPreferences: SharedPreferences
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var latestWorkoutsRecyclerView: RecyclerView
    private val homeFragmentViewModel: HomeFragmentViewModel by viewModels()

    @Inject lateinit var toaster: Toaster

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        gsc = GoogleSignIn.getClient(requireActivity(), gso)
        signOutBtn = view.findViewById(R.id.sign_out_btn)
        signOutBtn.setOnClickListener {
            signOut()
        }

        latestWorkoutsRecyclerView = view.findViewById(R.id.latest_workouts_recyclerview)
        latestWorkoutsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dividerItemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.custom_list_divider)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        latestWorkoutsRecyclerView.addItemDecoration(dividerItemDecoration)

        homeFragmentViewModel.getWorkoutsTotalResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { workOutsTotal ->
                try {
                    view.findViewById<TextView>(R.id.total_workouts).text = workOutsTotal.totalWorkouts.toString();
                    view.findViewById<TextView>(R.id.total_time).text = workOutsTotal.totalTime
                    view.findViewById<TextView>(R.id.total_distance).text = if (workOutsTotal.totalDistance >= 1000) {
                        String.format("%.2f km", workOutsTotal.totalDistance / 1000f)
                    } else {
                        "${workOutsTotal.totalDistance} m"
                    }
                    workoutAdapter = WorkoutAdapter(workOutsTotal.latestWorkouts)
                    latestWorkoutsRecyclerView.adapter = workoutAdapter
                } catch (e: Exception) {
                    toaster.showToast("Exception in setting text: ${e.message}")
                }
            }.onFailure { throwable ->
                toaster.showToast("Failed to get workout totals: ${throwable.message}")
            }
        }


        val userId = encryptedSharedPreferences.getInt(USER_ID_KEY, -1)
        if (userId != -1) {
            homeFragmentViewModel.getWorkoutsTotal(userId)
        } else {
            encryptedSharedPreferences.edit().clear().apply();
        }
        return view
    }

    class WorkoutAdapter(private val workouts: List<Workout>) :
        RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

        inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val workoutName: TextView = itemView.findViewById(R.id.workout_name)
            val workoutDistance: TextView = itemView.findViewById(R.id.workout_distance)
            val workoutTime: TextView = itemView.findViewById(R.id.workout_time)
            val workoutDate: TextView = itemView.findViewById(R.id.workout_date)
            val workoutMap: ImageView = itemView.findViewById(R.id.workout_map)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_workout_latest, parent, false)
            return WorkoutViewHolder(view)
        }


        override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
            val workout = workouts[position]
            holder.workoutName.text = workout.name
            holder.workoutTime.text = workout.time
            holder.workoutDistance.text = if (workout.distanceMeters >= 1000) {
                String.format("%.2f km", workout.distanceMeters / 1000f)
            } else {
                "${workout.distanceMeters} m"
            }


            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            val date: Date? = inputFormat.parse(workout.created_at)
            val formattedDate = if (date != null) outputFormat.format(date) else workout.created_at

            holder.workoutDate.text = formattedDate

            Glide.with(holder.itemView.context)
                .load(workout.staticMapUrl)
                .into(holder.workoutMap)
        }

        override fun getItemCount() = workouts.size
    }

    private fun signOut() {
        encryptedSharedPreferences.edit().clear().apply()
        gsc.signOut().addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                try {
                    encryptedSharedPreferences.edit().clear().apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finish()
            } else {
                toaster.showToast("Sign out failed")
            }
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}

