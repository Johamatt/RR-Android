import android.content.Context
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.AuthInterface
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthService(private val context: Context) {

    private val retrofit: Retrofit
    private val authInterface: AuthInterface
    private val EC2PublicIP = context.getString(R.string.EC2_PUBLIC_IP)

    init {
        val baseUrl = context.getString(R.string.EC2_PUBLIC_IP)
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authInterface = retrofit.create(AuthInterface::class.java)
    }

    fun loginWithEmail(email: String, password: String, callback: (response: ResponseBody?, error: Throwable?) -> Unit) {
        val url = "$EC2PublicIP/auth/login"
        val call = authInterface.loginWithEmail(url, email, password)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = response.message()
                    callback(null, Throwable(errorBody ?: errorMessage ?: "Unknown error occurred"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(null, t)
            }
        })
    }

    fun loginWithGoogle(idToken: String, callback: (response: ResponseBody?, error: Throwable?) -> Unit) {
        val url = "$EC2PublicIP/auth/google"
        val call = authInterface.loginWithGoogle(url, idToken)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    callback(null, Throwable(response.message()))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(null, t)
            }
        })
    }

    fun registerUser(email: String, password: String, callback: (response: ResponseBody?, error: Throwable?) -> Unit) {
        val url = "$EC2PublicIP/auth/register"
        val call = authInterface.registerUser(url, email, password)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = response.message()
                    callback(null, Throwable(errorBody ?: errorMessage ?: "Unknown error occurred"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(null, t)
            }
        })
    }

}


