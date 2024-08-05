package com.example.sport_geo_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sport_geo_app.ui.fragment.HomeFragment
import com.example.sport_geo_app.ui.fragment.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.SharedPreferences
import com.example.sport_geo_app.ui.fragment.WorkoutsFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    @Inject lateinit var encryptedSharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                R.id.bottom_workouts -> {
                    openFragment(WorkoutsFragment())
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
        val fragmentTag = fragment.javaClass.simpleName

        val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (existingFragment != null) {
            transaction.show(existingFragment)
        } else {
            transaction.add(R.id.frame_container, fragment, fragmentTag)
        }

        supportFragmentManager.fragments.forEach {
            if (it != fragment && it.isVisible) {
                transaction.hide(it)
            }
        }

        transaction.addToBackStack(fragmentTag)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }
}
