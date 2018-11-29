package pl.szkoleniaandroid.billexpert

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel

import kotlinx.android.synthetic.main.activity_bills.*
import pl.szkoleniaandroid.billexpert.api.BillsResponse
import pl.szkoleniaandroid.billexpert.databinding.ActivityBillsBinding
import pl.szkoleniaandroid.billexpert.session.SessionRepository
import pl.szkoleniaandroid.billexpert.session.SharedPrefsSessionRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class BillsActivity : AppCompatActivity() {

    private lateinit var sessionRepository: SessionRepository

    lateinit var binding: ActivityBillsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionRepository = SharedPrefsSessionRepository(
            android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )



        if (sessionRepository.isLoggedIn()) {
            setContentView(R.layout.activity_bills)
            setSupportActionBar(toolbar)

            fab.setOnClickListener { view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        } else {
            goToLogin()
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bills, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                return true
            }
            R.id.action_refresh -> {
                refreshBills()
                return true
            }
            R.id.action_logout -> {
                sessionRepository.removeUser()
                goToLogin()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshBills() {

        val call = getBillApi().getBills(sessionRepository.getToken())
        call.enqueue(object : Callback<BillsResponse> {
            override fun onFailure(call: Call<BillsResponse>, t: Throwable) {
            }

            override fun onResponse(call: Call<BillsResponse>, response: Response<BillsResponse>) {
                if (response.isSuccessful) {
                    val billsResponse = response.body()!!
                    billsResponse.results.forEach { bill ->
                        Timber.d(bill.toString())
                    }
                }
            }

        })
    }

}

class BillsViewModel : ViewModel() {

}
