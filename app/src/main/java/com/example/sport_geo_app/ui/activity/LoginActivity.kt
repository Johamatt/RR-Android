package com.example.sport_geo_app.ui.activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.AuthService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

// File: app/java/com/yourappname/ui/activity/LoginActivity.kt


class LoginActivity : AppCompatActivity() {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var googleBtn: ImageView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var emailLoginBtn: Button
    private lateinit var registerText: TextView
    private val authService = AuthService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("user_id")) {
            val userId = sharedPreferences.getInt("user_id", -1)
            val userEmail = sharedPreferences.getString("user_email", "")
            val userPoints = sharedPreferences.getString("user_points", "")
            if (userId != -1 && !userEmail.isNullOrEmpty() && !userPoints.isNullOrEmpty()) {
                navigateToMainActivity(userId, userEmail, userPoints!!)
                return
            }
        }

        googleBtn = findViewById(R.id.google_btn)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        emailLoginBtn = findViewById(R.id.email_login_btn)
        registerText = findViewById(R.id.register_btn)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
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
        startActivityForResult(signInIntent, 1000)
    }

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

                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("user_id", userId)
                editor.putString("user_email", userEmail)
                editor.putString("user_points", userPoints)
                editor.apply()

                navigateToMainActivity(userId, userEmail, userPoints)
            } catch (e: Exception) {
                e.printStackTrace()
                displayErrorMessage("Failed to parse user info")
            }
        }
    }

    private fun handleErrorResponse(response: Response?) {
        val responseBody = response?.body?.string()
        val jsonObject = JSONObject(responseBody)
        val errorMessage = jsonObject.getJSONArray("message")

        when (response?.code) {
            400 -> {
                displayErrorMessage(errorMessage.toString())
            }
            401 -> {
                displayErrorMessage(errorMessage.toString())
            }
            else -> {
                displayErrorMessage("Authentication failed: ${response?.message}")
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
