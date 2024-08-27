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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.di.Toaster
import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
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

    private lateinit var credentialManager: CredentialManager
    private lateinit var googleBtn: ImageView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var emailLoginBtn: Button
    private lateinit var registerText: TextView
    private val authViewModel: AuthViewModel by viewModels()
    @Inject lateinit var encryptedSharedPreferences: SharedPreferences
    @Inject lateinit var errorManager: ErrorManager
    @Inject lateinit var toaster: Toaster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initializeViews()
        setupGoogleSignIn()
        setupListeners()

        authViewModel.loginResult.observe(this) { result ->
            result.onSuccess { authResponse ->
                authViewModel.handleSuccessResponse(authResponse)
                navigateToMainActivity()
            }.onFailure { throwable ->
                val errorMessage = errorManager.handleErrorResponse(throwable)
                toaster.showToast(errorMessage)
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

    private fun initializeViews() {
        googleBtn = findViewById(R.id.google_btn)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        emailLoginBtn = findViewById(R.id.email_login_btn)
        registerText = findViewById(R.id.navigate_register_btn)
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
                toaster.showToast("Please enter email and password")
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
                toaster.showToast("Google Sign-In failed: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }


    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    companion object {
        private const val TAG = "LoginActivity"
    }
}

