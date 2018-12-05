package pl.szkoleniaandroid.billexpert

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.activity_bills.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.bindingcollectionadapter2.ItemBinding
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import pl.szkoleniaandroid.billexpert.api.Bill
import pl.szkoleniaandroid.billexpert.api.BillApi
import pl.szkoleniaandroid.billexpert.api.BillsResponse
import pl.szkoleniaandroid.billexpert.api.Category
import pl.szkoleniaandroid.billexpert.databinding.ActivityBillsBinding
import pl.szkoleniaandroid.billexpert.databinding.BillItemBinding
import pl.szkoleniaandroid.billexpert.db.BillDao
import pl.szkoleniaandroid.billexpert.db.BillDto
import pl.szkoleniaandroid.billexpert.session.SessionRepository
import pl.szkoleniaandroid.billexpert.session.SharedPrefsSessionRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.*

interface OnBillClicked {
    fun billClicked(bill: Bill)
}


const val REQUEST_CODE_ADD_BILL = 123

class BillsActivity : AppCompatActivity() {


    lateinit var binding: ActivityBillsBinding
    val billsAdapter = BillsAdapter()

    lateinit var viewModel: BillsViewModel

    val sessionRepository: SessionRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (sessionRepository.isLoggedIn()) {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_bills)
            setSupportActionBar(toolbar)

            fab.setOnClickListener { view ->
                goToAdd()
            }
            viewModel = BillsViewModel(get(), sessionRepository)

            viewModel.showBillLiveData.observe(this, Observer {
                if (!it.consumed) {
                    val bill = it.consume()
                    //TODO show bill
                    val intent = Intent(
                        this,
                        DetailsActivity::class.java
                    )
                    intent.putExtra("bill", bill)
                    startActivity(intent)
                }
            })

//            binding.billsContent.billsRecyclerView.adapter = billsAdapter
//            binding.billsContent.billsRecyclerView.layoutManager = LinearLayoutManager(this)

            binding.viewmodel = viewModel
            binding.setLifecycleOwner(this)

        } else {
            goToLogin()
        }
    }

    private fun goToAdd() {
        val intent = Intent(
            this,
            DetailsActivity::class.java
        )
        startActivityForResult(intent, REQUEST_CODE_ADD_BILL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_ADD_BILL) {
            if (resultCode == Activity.RESULT_OK) {
                refreshBills()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
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
                goToAdd()
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

        val call = get<BillApi>().getBills(sessionRepository.getToken())
        call.enqueue(object : Callback<BillsResponse> {
            override fun onFailure(call: Call<BillsResponse>, t: Throwable) {
            }

            override fun onResponse(call: Call<BillsResponse>, response: Response<BillsResponse>) {
                if (response.isSuccessful) {
                    val billsResponse = response.body()!!
                    billsResponse.results.forEach { bill ->
                        Timber.d(bill.toString())
                    }

                    //billsAdapter.setData(billsResponse.results)

                    GlobalScope.launch(context = Dispatchers.IO) {

                        val billDtos: List<BillDto> = billsResponse.results.map {
                            return@map BillDto().apply {
                                objectId = it.objectId
                                userId = it.userId
                                date = it.date
                                name = it.name
                                amount = it.amount
                                category = it.category
                                comment = it.comment
                            }
                        }.toList()

                        get<BillDao>().insert(billDtos)


//                        val bills = billDao.getBills(sessionRepository.getUserId())
//
//                        val billsToDisply = bills.map {
//                            Bill(
//                                userId = it.userId,
//                                date = it.date,
//                                name = it.name,
//                                amount = it.amount,
//                                category = it.category,
//                                comment = it.comment,
//                                objectId = it.objectId
//                            )
//                        }
//                        withContext(Dispatchers.Main) {
//                            viewModel.bills.clear()
//                            viewModel.bills.addAll(billsToDisply)
//                        }
//


                    }

                }
            }

        })
    }

}


class BillsAdapter : RecyclerView.Adapter<ViewHolder>() {

    val bills = mutableListOf<Bill>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(BillItemBinding.inflate(inflater, parent, false))
    }

    override fun getItemCount(): Int {
        return bills.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.item = bills[position]
    }

    fun setData(results: List<Bill>) {
        bills.clear()
        bills.addAll(results)
        notifyDataSetChanged()
    }

}

class ViewHolder(val binding: BillItemBinding) : RecyclerView.ViewHolder(binding.root) {

}

class BillsViewModel(
    private val billDao: BillDao,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val showBillLiveData = MutableLiveData<Event<Bill>>()

    val bills = ObservableArrayList<Bill>()
    val billsLiveData: LiveData<List<Bill>> =
        Transformations.map(billDao.getBills(sessionRepository.getUserId())) { billDtos ->
            val billsToDisply = billDtos.map {
                Bill(
                    userId = it.userId,
                    date = it.date,
                    name = it.name,
                    amount = it.amount,
                    category = it.category,
                    comment = it.comment,
                    objectId = it.objectId
                )
            }
            return@map billsToDisply
        }


    val itemBinding = ItemBinding.of<Bill>(BR.item, R.layout.bill_item)
        .bindExtra(BR.listener, object : OnBillClicked {
            override fun billClicked(bill: Bill) {
                showBillLiveData.value = Event(bill)
            }
        })
}
