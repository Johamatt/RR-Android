package com.example.sport_geo_app.ui.activity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.AuthService
import com.example.sport_geo_app.ui.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import okhttp3.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var googleBtn: ImageView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var emailLoginBtn: Button
    private lateinit var registerText: TextView
    private lateinit var authService: AuthService

    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authService = AuthService(this)
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[UserViewModel::class.java]

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("user_id")) {
            val userId = sharedPreferences.getInt("user_id", -1)
            val userEmail = sharedPreferences.getString("user_email", "")
            val userPoints = sharedPreferences.getString("user_points", "")
            val userCountry = sharedPreferences.getString("user_country", null)
            if (userId != -1 && !userEmail.isNullOrEmpty() && !userPoints.isNullOrEmpty()) {
                Log.d("LoginActivity", userCountry.toString())
                navigateToMainActivity(userId, userEmail, userPoints)
                return
            }
        }

        googleBtn = findViewById(R.id.google_btn)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        emailLoginBtn = findViewById(R.id.email_login_btn)
        registerText = findViewById(R.id.register_btn)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.WEB_CLIENT_ID))
            .requestEmail()
            .build()

        gsc = GoogleSignIn.getClient(this, gso)

        val acct: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val idToken = acct.idToken
            sendTokenToBackend(idToken)
        }

        googleBtn.setOnClickListener {
            signIn()
        }

        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        emailLoginBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithEmail(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn() {
        val signInIntent = gsc.signInIntent
        signInLauncher.launch(signInIntent)
    }


    @Deprecated("This declaration overrides deprecated member but not marked as deprecated itself. See https://youtrack.jetbrains.com/issue/KT-47902 for details")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                sendTokenToBackend(idToken)
            } catch (e: ApiException) {
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendTokenToBackend(idToken: String?) {
        if (idToken != null) {
            authService.sendTokenToBackend(idToken) { response, error ->
                if (error != null) {
                    error.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                } else if (response != null && response.isSuccessful) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        handleSuccessResponse(responseBody)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        authService.loginWithEmail(email, password) { response, error ->
            if (error != null) {
                error.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Login failed", Toast.LENGTH_SHORT).show()
                }
            } else if (response != null && response.isSuccessful) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    handleSuccessResponse(responseBody)
                }
            } else {
                runOnUiThread {
                    handleErrorResponse(response)
                }
            }
        }
    }

    private fun handleSuccessResponse(responseBody: String?) {
        if (responseBody != null) {
            try {
                val jsonObject = JSONObject(responseBody)
                val userJson = jsonObject.getJSONObject("user")

                val userId = userJson.getInt("user_id")
                val userEmail = userJson.getString("email")
                val userPoints = userJson.getString("points")
                val userCountry = if (!userJson.isNull("country")) {
                    userJson.getString("country")
                } else {
                    null
                }


                // Store user data in SharedPreferences
                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putInt("user_id", userId)
                    putString("user_email", userEmail)
                    putString("user_points", userPoints)
                    putString("user_country", userCountry)
                    apply()
                }


                navigateToMainActivity(userId, userEmail, userPoints)
            } catch (e: Exception) {
                e.printStackTrace()
                displayErrorMessage("Failed to parse user info")
            }
        }
    }

    private fun handleErrorResponse(response: Response?) {
        val responseBody = response?.body?.string()

        responseBody?.let { body ->
            val jsonObject = JSONObject(body)
            val errorArray = jsonObject.getJSONArray("message")

            val errorMessage = StringBuilder()
            for (i in 0 until errorArray.length()) {
                errorMessage.append(errorArray.getString(i))
                if (i < errorArray.length() - 1) {
                    errorMessage.append(", ")
                }
            }

            when (response.code) {
                400 -> {
                    displayErrorMessage(errorMessage.toString())
                }
                401 -> {
                    displayErrorMessage(errorMessage.toString())
                }
                else -> {
                    displayErrorMessage("Authentication failed: ${response.message}")
                }
            }
        } ?: run {
            // Handle case where responseBody is null
            displayErrorMessage("Response body is null")
        }
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // Handle the result here
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                sendTokenToBackend(idToken)
            } catch (e: ApiException) {
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayErrorMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity(userId: Int, userEmail: String, userPoints: String) {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.putExtra("user_id", userId)
        intent.putExtra("user_email", userEmail)
        intent.putExtra("user_points", userPoints)
        startActivity(intent)
        finish()
    }
}

