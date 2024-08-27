package com.example.sport_geo_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.SharedPreferences
import androidx.activity.viewModels
import com.example.sport_geo_app.di.Toaster
import com.example.sport_geo_app.ui.activity.LoginActivity
import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
import com.example.sport_geo_app.ui.viewmodel.NavigationViewModel
import com.example.sport_geo_app.utils.Constants.JWT_TOKEN_KEY
import com.example.sport_geo_app.utils.ErrorManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences
    private val navigationViewModel: NavigationViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    @Inject lateinit var errorManager: ErrorManager
    @Inject lateinit var toaster: Toaster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = encryptedSharedPreferences.getString(JWT_TOKEN_KEY, null)

        if (token.isNullOrEmpty()) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        } else {
            authViewModel.validateToken(token)
        }

        authViewModel.tokenResult.observe(this) { result ->
            result.onSuccess { expired ->
                if (!expired) {
                    encryptedSharedPreferences.edit().clear()
                    toaster.showToast("Token expired")
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                    finish()
                }
            }.onFailure { throwable ->
                val errorMessage = errorManager.handleErrorResponse(throwable)
                toaster.showToast(errorMessage)
                encryptedSharedPreferences.edit().clear()
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                finish()
            }
        }


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

    companion object {
        var TAG = "MainActivity"
    }
}
