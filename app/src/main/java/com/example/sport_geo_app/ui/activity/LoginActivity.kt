package com.example.sport_geo_app.ui.activity
import com.example.sport_geo_app.data.network.AuthService
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
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.utils.Constants.JWT_TOKEN_KEY
import com.example.sport_geo_app.utils.Constants.USER_EMAIL_KEY
import com.example.sport_geo_app.utils.Constants.USER_ID_KEY
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

    private var TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authService = AuthService(this)
        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(this)

        checkAndHandleGoogleSignIn { isGoogleSignInSuccessful ->
            if (!isGoogleSignInSuccessful && isLoggedIn()) {
                navigateToMainActivity()
            } else {
                initializeViews()
                setupGoogleSignIn()
                setupListeners()
            }
        }
    }

    private fun checkAndHandleGoogleSignIn(callback: (Boolean) -> Unit) {
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            val idToken = acct.idToken
            if (idToken != null) {
                loginWithGoogle(idToken, callback)
            } else {
                callback(false)
            }
        } else {
            callback(false)
        }
    }

    private fun loginWithGoogle(idToken: String, callback: (Boolean) -> Unit) {
        authService.loginWithGoogle(idToken) { response, error ->
            runOnUiThread {
                if (error != null) {
                    EncryptedPreferencesUtil.clearEncryptedPreferences(this)
                    handleErrorResponse(error)
                    callback(false)
                } else if (response != null) {
                    handleSuccessResponse(response.string())
                    callback(true)
                } else {
                    displayErrorMessage("Authentication failed")
                    callback(false)
                }
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        Log.d(TAG, encryptedSharedPreferences.all.toString())
        return encryptedSharedPreferences.contains(USER_ID_KEY)
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
        googleBtn.setOnClickListener { signInWithGoogle() }
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

    private fun signInWithGoogle() {
        val signInIntent = gsc.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun loginWithEmail(email: String, password: String) {
        authService.loginWithEmail(email, password) { response, error ->
            runOnUiThread {
                if (error != null) {
                    Log.d(TAG, error.toString())
                    handleErrorResponse(error)
                } else if (response != null) {
                    Log.d(TAG, response.toString())
                    handleSuccessResponse(response.string())
                } else {
                    handleErrorResponse(null)
                }
            }
        }
    }

    private fun handleSuccessResponse(responseBody: String) {
        Log.d(TAG, responseBody)
        try {
            val jsonObject = JSONObject(responseBody)
            val userJson = jsonObject.getJSONObject("user")
            val jwtToken = jsonObject.getString("jwtToken")
            val userId = userJson.getInt("user_id")
            val userEmail = userJson.getString("email")
            saveUserData(userId, jwtToken, userEmail)
            navigateToMainActivity()
        } catch (e: Exception) {
            displayErrorMessage("Failed to parse user info")
            e.printStackTrace()
        }
    }

    private fun handleErrorResponse(error: Throwable?) {
        val errorMessage = error?.message?.let { message ->
            try {
                val jsonObject = JSONObject(message)
                jsonObject.getString("message")
            } catch (e: JSONException) {
                e.printStackTrace()
                "Failed to parse error response"
            }
        } ?: "Unknown error occurred: ${error?.javaClass?.simpleName ?: "Unknown"}"

        displayErrorMessage(errorMessage)
    }

    private fun saveUserData(userId: Int, jwtToken: String, userEmail: String) {
        with(encryptedSharedPreferences.edit()) {
            putInt(USER_ID_KEY, userId)
            putString(USER_EMAIL_KEY, userEmail)
            putString(JWT_TOKEN_KEY, jwtToken)
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
                if (idToken != null) {
                    loginWithGoogle(idToken) { success ->
                        if (success) {
                            navigateToMainActivity()
                        }
                    }
                }
            } catch (e: ApiException) {
                displayErrorMessage("Google Sign-In failed")
                e.printStackTrace()
            }
        }
    }

    private fun displayErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

