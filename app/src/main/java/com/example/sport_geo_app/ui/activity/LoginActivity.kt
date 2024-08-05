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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.auth.AuthViewModel
import com.example.sport_geo_app.utils.Constants.USER_ID_KEY
import com.example.sport_geo_app.utils.ErrorManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

//TODO GOOGLE SIGN-IN DEPRECATED https://developers.google.com/identity/sign-in/android/legacy-sign-in
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient
    private lateinit var googleBtn: ImageView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var emailLoginBtn: Button
    private lateinit var registerText: TextView
    private val authViewModel: AuthViewModel by viewModels()
    @Inject lateinit var encryptedSharedPreferences: SharedPreferences
    @Inject lateinit var errorManager: ErrorManager

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        checkAndHandleGoogleSignIn { isGoogleSignInSuccessful ->
            if (!isGoogleSignInSuccessful && isLoggedIn()) {
                navigateToMainActivity()
            } else {
                initializeViews()
                setupGoogleSignIn()
                setupListeners()
            }
        }

        authViewModel.loginResult.observe(this) { result ->
            result.onSuccess { responseBody ->
                Log.d(TAG, responseBody.toString())
                authViewModel.handleSuccessResponse(responseBody.string())
                navigateToMainActivity()
            }.onFailure { throwable ->
                Log.d(TAG, throwable.toString())
                errorManager.handleErrorResponse(throwable)
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
        authViewModel.loginWithGoogle(idToken)
    }


    private fun isLoggedIn(): Boolean {
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
        authViewModel.loginWithEmail(email, password)
    }



    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "Result Code: ${result.resultCode}")
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
                displayErrorMessage("Google Sign-In failed: ${e.localizedMessage}")
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

