package com.example.sport_geo_app.ui.activity
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
    private lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupGoogleSignIn()
        setupListeners()

        authService = AuthService(this)
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(this)

        checkAndHandleGoogleSignIn()
    }

    private fun initializeViews() {
        googleBtn = findViewById(R.id.google_btn)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        emailLoginBtn = findViewById(R.id.email_login_btn)
        registerText = findViewById(R.id.register_btn)
    }

    private fun setupGoogleSignIn() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.WEB_CLIENT_ID))
            .requestEmail()
            .build()
        gsc = GoogleSignIn.getClient(this, gso)
    }

    private fun setupListeners() {
        googleBtn.setOnClickListener { signIn() }
        registerText.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        emailLoginBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithEmail(email, password)
            } else {
                showToast("Please enter email and password")
            }
        }
    }

    private fun checkAndHandleGoogleSignIn() {
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val idToken = acct.idToken
            sendTokenToBackend(idToken)
        }
    }

    private fun signIn() {
        val signInIntent = gsc.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun sendTokenToBackend(idToken: String?) {
        if (idToken != null) {
            authService.sendTokenToBackend(idToken) { response, error ->
                runOnUiThread {
                    if (error != null) {
                        showToast("Authentication failed")
                    } else if (response != null && response.isSuccessful) {
                        handleSuccessResponse(response.body?.string())
                    } else {
                        showToast("Authentication failed")
                    }
                }
            }
        }
    }


    private fun loginWithEmail(email: String, password: String) {
        authService.loginWithEmail(email, password) { response, error ->
            runOnUiThread {
                if (error != null) {
                    handleErrorResponse(response)
                } else if (response != null && response.isSuccessful) {
                    handleSuccessResponse(response.body?.string())
                } else {
                    handleErrorResponse(response)
                }
            }
        }
    }


    private fun handleSuccessResponse(responseBody: String?) {
        responseBody?.let {
            try {


                val jsonObject = JSONObject(responseBody)
                Log.d("LoginActivity", jsonObject.toString())
                val userJson = jsonObject.getJSONObject("user")
                val jwtToken = jsonObject.getString("jwtToken")
                val userId = userJson.getInt("user_id")
                val userEmail = userJson.getString("email")
                val userPoints = userJson.getString("points")
                val userCountry = userJson.optString("country", null)

                saveUserData(userId, jwtToken, userEmail, userPoints, userCountry)
                navigateToMainActivity(userId, userEmail, userPoints)
            } catch (e: Exception) {
                runOnUiThread {
                    showToast("Failed to parse user info")
                }
                e.printStackTrace()
            }
        }
    }

    // TODO ? just return err msg from back
    private fun handleErrorResponse(response: Response?) {
        response?.let {
            val responseBody = response.body?.string()
            responseBody?.let { body ->
                try {
                    val jsonObject = JSONObject(body)
                    val errorArray = jsonObject.optJSONArray("message")

                    val errorMessage = if (errorArray != null) {
                        val errorMessages = mutableListOf<String>()
                        for (i in 0 until errorArray.length()) {
                            errorMessages.add(errorArray.getString(i))
                        }
                        errorMessages.joinToString(", ")
                    } else {
                        "Unknown error"
                    }

                    runOnUiThread {
                        showToast(errorMessage)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        showToast("Failed to parse error response")
                    }
                    e.printStackTrace()
                }
            } ?: runOnUiThread {
                showToast("Response body is null")
            }
        } ?: runOnUiThread {
            showToast("Response is null")
        }
    }


    private fun saveUserData(userId: Int, jwtToken: String, userEmail: String, userPoints: String, userCountry: String?) {
        with(encryptedSharedPreferences.edit()) {
            putInt("user_id", userId)
            putString("jwtToken", jwtToken)
            putString("user_email", userEmail)
            putString("user_points", userPoints)
            putString("user_country", userCountry)
            apply()
        }
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                sendTokenToBackend(idToken)
            } catch (e: ApiException) {
                showToast("Something went wrong")
                e.printStackTrace()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity(userId: Int, userEmail: String, userPoints: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("user_id", userId)
            putExtra("user_email", userEmail)
            putExtra("user_points", userPoints)
        }
        startActivity(intent)
        finish()
    }
}

