package pl.szkoleniaandroid.billexpert

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.koin.android.ext.android.get
import pl.szkoleniaandroid.billexpert.api.Bill
import pl.szkoleniaandroid.billexpert.api.BillApi
import pl.szkoleniaandroid.billexpert.api.Category
import pl.szkoleniaandroid.billexpert.api.PostBillResponse
import pl.szkoleniaandroid.billexpert.databinding.ActivityDetailsBinding
import pl.szkoleniaandroid.billexpert.session.SessionRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.lang.NumberFormatException
import java.util.*

class DetailsActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details)

        binding.viewmodel = DetailsViewModel(get(), get())
        binding.viewmodel?.finishedLiveData?.observe(this, androidx.lifecycle.Observer {
            if (!it.consumed) {
                val bill = it.consume()
                val intent = Intent()
                intent.putExtra("bill", bill)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
        val bill = intent.getSerializableExtra("bill")
                as Bill?

    }
}


class DetailsViewModel(
    private val billApi: BillApi,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    val userId = ObservableField<String>("")
    val date = ObservableField<Date>(Date())
    val name = ObservableField<String>("")
    val amount = ObservableField<String>("0.0")
    val category = ObservableField<Category>(Category.OTHER)
    val comment = ObservableField<String>("")
    val objectId = ObservableField<String>("")
    val categories = Category.values().toList()
    val isChecked = ObservableBoolean(false)
    val selectedCategoryIndex = ObservableInt(0)

    val finishedLiveData = MutableLiveData<Event<Bill>>()

    fun save() {
        var selectedAmount = 0.0
        try {
            selectedAmount = amount.get()!!.toDouble()

        } catch (e: NumberFormatException) {
            return //handle error
        }

        val bill = Bill(
            userId = sessionRepository.getUserId(),
            date = date.get()!!,
            name = name.get()!!,
            amount = selectedAmount,
            category = Category.values()[selectedCategoryIndex.get()],
            comment = comment.get()!!
        )

        val call = billApi.postBill(sessionRepository.getToken(), bill)

        call.enqueue(object : Callback<PostBillResponse> {
            override fun onFailure(call: Call<PostBillResponse>, t: Throwable) {
                Timber.e(t)
            }

            override fun onResponse(call: Call<PostBillResponse>, response: Response<PostBillResponse>) {
                if (response.isSuccessful) {
                    finishedLiveData.value = Event(bill.copy(objectId = response.body()!!.objectId))

                }
            }

        })
    }
}