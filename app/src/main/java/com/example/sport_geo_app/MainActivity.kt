package com.example.sport_geo_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sport_geo_app.ui.fragment.HomeFragment
import com.example.sport_geo_app.ui.fragment.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.example.sport_geo_app.ui.activity.SettingsActivity
import com.example.sport_geo_app.ui.fragment.RoutePlanFragment
import com.example.sport_geo_app.ui.fragment.VisitsFragment
import com.example.sport_geo_app.ui.viewmodel.UserViewModel
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: UserViewModel
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(this)

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
                R.id.bottom_routePlan -> {
                    openFragment(RoutePlanFragment())
                    true
                }
                R.id.bottom_visits -> {
                    openFragment(VisitsFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            // Check if country is defined in EncryptedSharedPreferences
            val userCountry = encryptedSharedPreferences.getString("user_country", null)
            if (userCountry.isNullOrEmpty()) {
                navigateToCountrySelection()
            } else {
                bottomNavigationView.selectedItemId = R.id.bottom_home
            }
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
        transaction.commit()
    }

    private fun navigateToCountrySelection() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
