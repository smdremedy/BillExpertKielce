package pl.szkoleniaandroid.billexpert

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.szkoleniaandroid.billexpert.api.BillApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.*

class App : Application() {

    lateinit var billApi: BillApi

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://parseapi.back4app.com")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

        billApi = retrofit.create(BillApi::class.java)
    }
}