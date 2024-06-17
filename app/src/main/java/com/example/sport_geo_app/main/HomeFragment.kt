package com.example.sport_geo_app.main
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sport_geo_app.R
import com.example.sport_geo_app.auth.LoginActivity
import com.example.sport_geo_app.model.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener

class HomeFragment : Fragment() {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var signOutBtn: Button
    private lateinit var userViewModel: UserViewModel

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

        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        userViewModel.userId.observe(viewLifecycleOwner, Observer { userId ->
            view.findViewById<TextView>(R.id.user_id_text_view).text = userId.toString()
        })

        userViewModel.userEmail.observe(viewLifecycleOwner, Observer { userEmail ->
            view.findViewById<TextView>(R.id.user_email_text_view).text = userEmail
        })

        userViewModel.userPoints.observe(viewLifecycleOwner, Observer { userPoints ->
            view.findViewById<TextView>(R.id.user_points_text_view).text = userPoints
        })

        return view
    }

    private fun signOut() {
        gsc.signOut().addOnCompleteListener(requireActivity(), OnCompleteListener<Void> { task ->
            if (task.isSuccessful) {
                // Clear SharedPreferences
                val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Sign out failed", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
