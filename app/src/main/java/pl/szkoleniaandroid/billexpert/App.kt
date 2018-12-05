package pl.szkoleniaandroid.billexpert

import android.app.Application
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.android.startKoin
import pl.szkoleniaandroid.billexpert.api.BillApi
import pl.szkoleniaandroid.billexpert.db.BillDao
import pl.szkoleniaandroid.billexpert.db.BillDatabase
import pl.szkoleniaandroid.billexpert.di.appModule
import pl.szkoleniaandroid.billexpert.session.SessionRepository
import pl.szkoleniaandroid.billexpert.session.SharedPrefsSessionRepository
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.*

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)
        }

        startKoin(this, listOf(appModule))

    }
}
