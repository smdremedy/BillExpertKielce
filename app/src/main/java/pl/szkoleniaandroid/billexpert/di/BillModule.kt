package pl.szkoleniaandroid.billexpert.di

import android.preference.PreferenceManager
import androidx.room.Room
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import pl.szkoleniaandroid.billexpert.BuildConfig
import pl.szkoleniaandroid.billexpert.LoginViewModel
import pl.szkoleniaandroid.billexpert.api.BillApi
import pl.szkoleniaandroid.billexpert.db.BillDatabase
import pl.szkoleniaandroid.billexpert.session.SessionRepository
import pl.szkoleniaandroid.billexpert.session.SharedPrefsSessionRepository
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

val appModule = module {

    single {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(interceptor)
            builder.addNetworkInterceptor(StethoInterceptor())
        }

        val client = builder.build()
        return@single client
    }

    single {
        Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://parseapi.back4app.com")
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .client(get())
            .build()

    }

    single {

        get<Retrofit>().create(BillApi::class.java)
    }

    single<SessionRepository> {
        val sessionRepository: SessionRepository = SharedPrefsSessionRepository(
            PreferenceManager.getDefaultSharedPreferences(androidApplication())
        )
        return@single sessionRepository
    }

    single {
        val database = Room.databaseBuilder(androidApplication(), BillDatabase::class.java, "bills.db")
            .build()
        database.getBillDao()
    }

    viewModel {
        LoginViewModel(get(), get())
    }

}