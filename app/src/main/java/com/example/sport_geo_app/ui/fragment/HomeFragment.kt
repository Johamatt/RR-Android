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
import com.example.sport_geo_app.R
import com.example.sport_geo_app.ui.activity.LoginActivity
import com.example.sport_geo_app.ui.viewmodel.HomeFragmentViewModel
import com.example.sport_geo_app.utils.AdManager
import com.example.sport_geo_app.utils.Constants.USER_EMAIL_KEY
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


        homeFragmentViewModel.getWorkoutsTotalResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { workOutsTotal ->
                try {
                    view.findViewById<TextView>(R.id.user_id_text_view).text = workOutsTotal.totalDistanceKM.toString()
                    view.findViewById<TextView>(R.id.user_email_text_view).text = workOutsTotal.totalTime
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

