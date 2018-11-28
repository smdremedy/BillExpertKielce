package pl.szkoleniaandroid.billexpert

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.databinding.*
import androidx.lifecycle.ViewModel
import com.google.android.material.textfield.TextInputLayout
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.szkoleniaandroid.billexpert.api.BillApi
import pl.szkoleniaandroid.billexpert.api.LoginResponse
import pl.szkoleniaandroid.billexpert.databinding.ActivityLoginBinding
import pl.szkoleniaandroid.billexpert.session.SessionRepository
import pl.szkoleniaandroid.billexpert.session.SharedPrefsSessionRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

typealias ObservableString = ObservableField<String>

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionRepository = SharedPrefsSessionRepository(
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewmodel = LoginViewModel(
            sessionRepository
        )
    }
}

class LoginViewModel(private val sessionRepository: SessionRepository) : ViewModel() {

    val usernameError = ObservableInt(0)
    val username = ObservableField<String>("test")
    var password = ObservableString("pass")
    val passwordError = ObservableInt(0)
    val inProgress = ObservableBoolean(false)


//    var task: AsyncTask<String, Int, Boolean>? = null


    init {
        username.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (username.get()!!.isEmpty()) {
                    usernameError.set(R.string.username_empty_error)
                } else {
                    usernameError.set(0)
                }
            }

        })
    }

    override fun onCleared() {
        super.onCleared()

//        task?.cancel(true)
    }

    fun signInClicked() {
        Log.d("TAG", "${username.get()}")
        if (username.get()!!.isEmpty()) {
            usernameError.set(R.string.username_empty_error)
        }
        if (password.get()!!.isEmpty()) {
            passwordError.set(R.string.password_empty_error)
        }

        login(username.get()!!, password.get()!!)
    }

    private fun login(username: String, password: String) {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()


        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://parseapi.back4app.com")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()

        val billApi = retrofit.create(BillApi::class.java)
        val call = billApi.getLogin(username, password)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body: LoginResponse = response.body()!!
                    Timber.d("Response:$body")
                    sessionRepository.saveUser(body.objectId, body.sessionToken)

                    //GO TO BILLS!!
                } else {


                    val error = response.errorBody()
                }
            }

        })


//        task = object : AsyncTask<String, Int, Boolean>() {
//            override fun doInBackground(vararg params: String?): Boolean {
//                for (i in 1..10) {
//                    Thread.sleep(1000)
//                    publishProgress(i)
//
//                }
//                return true
//            }
//
//            override fun onPreExecute() {
//                super.onPreExecute()
//                inProgress.set(true)
//            }
//
//            override fun onPostExecute(result: Boolean) {
//                super.onPostExecute(result)
//                inProgress.set(false)
//                task = null
//            }
//
//            override fun onProgressUpdate(vararg values: Int?) {
//                super.onProgressUpdate(*values)
//                this@LoginViewModel.username.set(values[0].toString())
//
//            }
//
//        }
//
//        task?.execute(username, password)

    }

}


@BindingAdapter("error")
fun intErrorAdapter(view: TextInputLayout, errorId: Int) {
    if (errorId == 0) {
        view.error = ""
    } else {
        view.error = view.context.getString(errorId)
    }
}
