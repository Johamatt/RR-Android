package com.example.sport_geo_app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)

        val isFirstTimeUser = checkIfFirstTimeUser()
        if (isFirstTimeUser) {
            navController.navigate(R.id.getStartedFragment)
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            when (destination.id) {
                R.id.recordFragment, R.id.mapFragment, R.id.getStartedFragment -> {
                    toolbar.visibility = View.GONE
                }
                else -> {
                    toolbar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkIfFirstTimeUser(): Boolean {
        // TODO
        // logic to determine if first time user (e.g. SharedPreferences)
        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
