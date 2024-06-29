package com.example.sport_geo_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sport_geo_app.ui.fragment.HomeFragment
import com.example.sport_geo_app.ui.fragment.MapFragment
import com.example.sport_geo_app.ui.viewmodel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userId = intent.getIntExtra("user_id", -1)
        val userEmail = intent.getStringExtra("user_email")
        val userPoints = intent.getStringExtra("user_points")

        val userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        userViewModel.setUserId(userId)
        userViewModel.setUserEmail(userEmail ?: "")
        userViewModel.setUserPoints(userPoints ?: "")

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_home -> {
                    openFragment(HomeFragment())
                    true
                }
                R.id.bottom_map -> {
                    openFragment(MapFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.bottom_home
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        val currentFragment = supportFragmentManager.primaryNavigationFragment
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }

        val tag = fragment.javaClass.simpleName
        var fragmentTemp = supportFragmentManager.findFragmentByTag(tag)
        if (fragmentTemp == null) {
            fragmentTemp = fragment
            transaction.add(R.id.frame_container, fragmentTemp, tag)
        } else {
            transaction.show(fragmentTemp)
        }

        transaction.setPrimaryNavigationFragment(fragmentTemp)
        transaction.setReorderingAllowed(true)
        transaction.commitNowAllowingStateLoss()
    }
}

