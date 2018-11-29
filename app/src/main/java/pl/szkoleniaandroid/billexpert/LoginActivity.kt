package pl.szkoleniaandroid.billexpert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
        val loginViewModel = LoginViewModel(
            sessionRepository
        )
        binding.viewmodel = loginViewModel
        loginViewModel.goToBillsLiveData.observe(this, Observer<Event<Unit>> {

            if (!it.consumed) {
                it.consume()
                startActivity(Intent(this, BillsActivity::class.java))
                finish()
            }
        })
        loginViewModel.showErrorLiveData.observe(this, Observer<Event<Int>> {
            if (!it.consumed) {
                val errorId = it.consume()!!
                Toast.makeText(this, errorId, Toast.LENGTH_SHORT).show()
            }
        })
    }
}

class LoginViewModel(private val sessionRepository: SessionRepository) : ViewModel() {

    val usernameError = ObservableInt(0)
    val username = ObservableField<String>("test")
    var password = ObservableString("pass")
    val passwordError = ObservableInt(0)
    val inProgress = ObservableBoolean(false)

    val goToBillsLiveData = MutableLiveData<Event<Unit>>()
    val showErrorLiveData = MutableLiveData<Event<Int>>()


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

        inProgress.set(true)

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
                showErrorLiveData.value = Event(R.string.network_error)
                inProgress.set(false)
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body: LoginResponse = response.body()!!
                    Timber.d("Response:$body")
                    sessionRepository.saveUser(body.objectId, body.sessionToken)

                    //GO TO BILLS!!

                    goToBillsLiveData.value = Event(Unit)
                    inProgress.set(false)
                } else {


                    val error = response.errorBody()
                    showErrorLiveData.value = Event(R.string.login_error)
                    inProgress.set(false)
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

@BindingAdapter("visible")
fun visible(view: ProgressBar, visible: Boolean) {
    view.visibility = if(visible) View.VISIBLE else View.GONE
}