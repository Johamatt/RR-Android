package com.example.sport_geo_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.SharedPreferences
import androidx.activity.viewModels
import com.example.sport_geo_app.ui.activity.LoginActivity
import com.example.sport_geo_app.ui.viewmodel.NavigationViewModel
import com.example.sport_geo_app.utils.Constants.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences
    private val navigationViewModel: NavigationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO move to splash?
        val isLoggedIn = encryptedSharedPreferences.contains(USER_ID_KEY)
        if (!isLoggedIn) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
            return
        }
        //

        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            navigationViewModel.navigateTo(menuItem.itemId)
            true
        }
        navigationViewModel.currentFragment.observe(this) { fragment ->
            openFragment(fragment)
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.bottom_home
        }
    }

    private fun openFragment(fragment: Fragment) {
        val fragmentTag = fragment.javaClass.simpleName
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        if (currentFragment != null && currentFragment.javaClass.simpleName == fragmentTag) {
            return
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment, fragmentTag)
            .commit()
    }
}
