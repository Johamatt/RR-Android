package com.example.sport_geo_app.ui.fragment
import android.content.Context
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
import com.airbnb.lottie.LottieAnimationView
import com.example.sport_geo_app.R
import com.example.sport_geo_app.ui.activity.LoginActivity
import com.example.sport_geo_app.utils.AdManager
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class HomeFragment : Fragment() {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var signOutBtn: Button
    private lateinit var adManager: AdManager
    private lateinit var loadAdBtn: Button
    private lateinit var encryptedSharedPreferences: SharedPreferences


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

        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(requireContext())

        val userId = encryptedSharedPreferences.getInt("user_id", -1)
        val userEmail = encryptedSharedPreferences.getString("user_email", "")
        val userPoints = encryptedSharedPreferences.getString("user_points", "")

        view.findViewById<TextView>(R.id.user_id_text_view).text = userId.toString()
        view.findViewById<TextView>(R.id.user_email_text_view).text = userEmail
        view.findViewById<TextView>(R.id.user_points_text_view).text = userPoints

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
        gsc.signOut().addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                val sharedPreferences = requireContext().getSharedPreferences("encrypted_user_prefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    clear()
                    apply()
                }
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Sign out failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

