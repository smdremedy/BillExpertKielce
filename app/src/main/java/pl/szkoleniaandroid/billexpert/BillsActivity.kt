package pl.szkoleniaandroid.billexpert

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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.activity_bills.*
import me.tatarka.bindingcollectionadapter2.ItemBinding
import pl.szkoleniaandroid.billexpert.api.Bill
import pl.szkoleniaandroid.billexpert.api.BillsResponse
import pl.szkoleniaandroid.billexpert.databinding.ActivityBillsBinding
import pl.szkoleniaandroid.billexpert.databinding.BillItemBinding
import pl.szkoleniaandroid.billexpert.session.SessionRepository
import pl.szkoleniaandroid.billexpert.session.SharedPrefsSessionRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

interface OnBillClicked {
    fun billClicked(bill: Bill)
}


class BillsActivity : AppCompatActivity() {


    lateinit var binding: ActivityBillsBinding
    val billsAdapter = BillsAdapter()

    lateinit var viewModel: BillsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        if (sessionRepository.isLoggedIn()) {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_bills)
            setSupportActionBar(toolbar)

            fab.setOnClickListener { view ->
               goToAdd()
            }
            viewModel = BillsViewModel()
            viewModel.showBillLiveData.observe(this, Observer {
                if(!it.consumed) {
                    val bill = it.consume()
                    //TODO show bill
                    val intent = Intent(this,
                        DetailsActivity::class.java)
                    intent.putExtra("bill", bill)
                    startActivity(intent)
                }
            })

//            binding.billsContent.billsRecyclerView.adapter = billsAdapter
//            binding.billsContent.billsRecyclerView.layoutManager = LinearLayoutManager(this)

            binding.viewmodel = viewModel

        } else {
            goToLogin()
        }
    }

    private fun goToAdd() {
        val intent = Intent(this,
            DetailsActivity::class.java)
        startActivity(intent)
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
                    viewModel.bills.clear()
                    viewModel.bills.addAll(billsResponse.results)
                    //billsAdapter.setData(billsResponse.results)
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

class BillsViewModel : ViewModel() {

    val showBillLiveData = MutableLiveData<Event<Bill>>()

    val bills = ObservableArrayList<Bill>()
    val itemBinding = ItemBinding.of<Bill>(BR.item, R.layout.bill_item)
        .bindExtra(BR.listener, object: OnBillClicked {
            override fun billClicked(bill: Bill) {
                showBillLiveData.value = Event(bill)
            }
        })
}
