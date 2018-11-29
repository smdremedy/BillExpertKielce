package pl.szkoleniaandroid.billexpert

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import pl.szkoleniaandroid.billexpert.api.Bill
import pl.szkoleniaandroid.billexpert.api.Category
import pl.szkoleniaandroid.billexpert.databinding.ActivityDetailsBinding
import timber.log.Timber
import java.util.*

class DetailsActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details)

        binding.viewmodel = DetailsViewModel()
        val bill = intent.getSerializableExtra("bill")
                as Bill?

    }
}


class DetailsViewModel : ViewModel() {
    val userId = ObservableField<String>("")
    val date = ObservableField<Date>(Date())
    val name = ObservableField<String>("")
    val amount = ObservableField<String>("0.0")
    val category = ObservableField<Category>(Category.OTHER)
    val comment = ObservableField<String>("")
    val objectId = ObservableField<String>("")
    val categories = Category.values().toList()
    val isChecked = ObservableBoolean(false)
}