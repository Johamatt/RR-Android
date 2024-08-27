package com.example.sport_geo_app.ui.fragment
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.model.Workout
import com.example.sport_geo_app.ui.activity.LoginActivity
import com.example.sport_geo_app.ui.viewmodel.HomeFragmentViewModel
import com.example.sport_geo_app.utils.AdManager
import com.example.sport_geo_app.utils.Constants.USER_ID_KEY
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var signOutBtn: Button
    private lateinit var adManager: AdManager
    private lateinit var loadAdBtn: Button
    @Inject lateinit var encryptedSharedPreferences: SharedPreferences

    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var latestWorkoutsRecyclerView: RecyclerView
    private val homeFragmentViewModel: HomeFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        adManager = AdManager(requireContext())
        loadAdBtn = view.findViewById(R.id.load_ad_btn)
        loadAdBtn.setOnClickListener {
            showRewardedAd()
        }
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


        homeFragmentViewModel.getWorkoutsTotalResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { workOutsTotal ->
                try {
                    Log.d(TAG,workOutsTotal.toString())
                    view.findViewById<TextView>(R.id.total_distance).text = "${workOutsTotal.totalDistanceKM} km"
                    view.findViewById<TextView>(R.id.total_time).text = workOutsTotal.totalTime

                    workoutAdapter = WorkoutAdapter(workOutsTotal.latestWorkouts)
                    latestWorkoutsRecyclerView.adapter = workoutAdapter
                } catch (e: Exception) {
                    Log.e(TAG, "Exception in setting text: ${e.message}", e)
                }
            }.onFailure { throwable ->
                Log.e(TAG, "Failed to get workout totals: ${throwable.message}", throwable)
            }
        }
        val userId = encryptedSharedPreferences.getInt(USER_ID_KEY, -1)
        if (userId != -1) {
            homeFragmentViewModel.getWorkoutsTotal(userId)
        } else {
            //    User ID not found
        }
        return view
    }

    class WorkoutAdapter(private val workouts: List<Workout>) :
        RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

        inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val workoutName: TextView = itemView.findViewById(R.id.workout_name)
            val workoutDistance: TextView = itemView.findViewById(R.id.workout_distance)
            val workoutTime: TextView = itemView.findViewById(R.id.workout_time)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_workout_latest, parent, false)
            return WorkoutViewHolder(view)
        }


        override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
            val workout = workouts[position]
            holder.workoutName.text = workout.name
            holder.workoutDistance.text = "${workout.distanceMeters} km"
            holder.workoutTime.text = workout.time
        }

        override fun getItemCount() = workouts.size
    }

    private fun showRewardedAd() {
        adManager.showRewardedAd { rewardItem ->
            val rewardAmount = rewardItem.amount
            val rewardType = rewardItem.type
            Log.d("HomeFragment", "User earned the reward: $rewardAmount $rewardType")
            // rewardUser(userId, rewardAmount) // Example usage of userId
        }
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
                Toast.makeText(requireContext(), "Sign out failed", Toast.LENGTH_SHORT).show()
            }
            //TODO inject errormanager
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}

