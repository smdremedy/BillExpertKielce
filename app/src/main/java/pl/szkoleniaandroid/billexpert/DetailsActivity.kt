package pl.szkoleniaandroid.billexpert

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pl.szkoleniaandroid.billexpert.api.Bill
import timber.log.Timber

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val bill = intent.getSerializableExtra("bill")
                as Bill
        Timber.d(bill.toString())
    }
}
