package com.example.sport_geo_app.ui.activity
import AuthService
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.json.JSONException
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
    private lateinit var encryptedSharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupGoogleSignIn()
        setupListeners()

        authService = AuthService(this)
        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(this)

        checkAndHandleGoogleSignIn()

        if (isLoggedIn()) {
            navigateToMainActivity()
        }
    }

    private fun isLoggedIn(): Boolean {
        return encryptedSharedPreferences.contains("user_id")
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
                displayErrorMessage("Please enter email and password")
            }
        }
    }

    private fun checkAndHandleGoogleSignIn() {
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val idToken = acct.idToken
            loginWithGoogle(idToken)
        }
    }

    private fun signIn() {
        val signInIntent = gsc.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun loginWithGoogle(idToken: String?) {
        if (idToken != null) {
            authService.loginWithGoogle(idToken) { response, error ->
                runOnUiThread {
                    if (error != null) {
                        handleErrorResponse(error)
                    } else if (response != null) {
                        handleSuccessResponse(response.string())
                    } else {
                        displayErrorMessage("Authentication failed")
                    }
                }
            }
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        authService.loginWithEmail(email, password) { response, error ->
            runOnUiThread {
                if (error != null) {
                    handleErrorResponse(error)
                } else if (response != null) {
                    handleSuccessResponse(response.string())
                } else {
                    handleErrorResponse(null)
                }
            }
        }
    }

    private fun handleSuccessResponse(responseBody: String?) {
        responseBody?.let {
            try {
                val jsonObject = JSONObject(responseBody)
                val userJson = jsonObject.getJSONObject("user")
                val jwtToken = jsonObject.getString("jwtToken")
                val userId = userJson.getInt("user_id")
                val userEmail = userJson.getString("email")
                val userCountry = userJson.optString("country", null)
                saveUserData(userId, jwtToken, userEmail, userCountry)
                navigateToMainActivity()
            } catch (e: Exception) {
                runOnUiThread {
                    displayErrorMessage("Failed to parse user info")
                }
                e.printStackTrace()
            }
        }
    }

    private fun handleErrorResponse(error: Throwable?) {
        val errorMessage = error?.message
        val messageToShow = errorMessage?.let { message ->
            try {
                val jsonObject = JSONObject(message)
                jsonObject.getString("message")
            } catch (e: JSONException) {
                e.printStackTrace()
                "Failed to parse error response"
            }
        } ?: "Unknown error occurred: ${error?.javaClass?.simpleName ?: "Unknown"}"

        runOnUiThread {
            displayErrorMessage(messageToShow)
        }
    }

    private fun saveUserData(userId: Int, jwtToken: String, userEmail: String, userCountry: String?) {
        with(encryptedSharedPreferences.edit()) {
            putInt("user_id", userId)
            putString("user_email", userEmail)
            putString("user_country", userCountry)
            putString("jwtToken", jwtToken)
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
                loginWithGoogle(idToken)
            } catch (e: ApiException) {
                displayErrorMessage("Something went wrong")
                e.printStackTrace()
            }
        }
    }

    private fun displayErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
