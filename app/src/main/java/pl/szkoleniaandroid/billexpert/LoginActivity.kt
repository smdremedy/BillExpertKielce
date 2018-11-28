package pl.szkoleniaandroid.billexpert

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import pl.szkoleniaandroid.billexpert.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.viewmodel = LoginViewModel()

        binding.signInButton.setOnClickListener {
            val vm = binding.viewmodel as LoginViewModel
            val username = vm.username
            Log.d("TAG","$username")
            if(username.isEmpty()) {
                binding.usernameLayout.error = "Empty!"
            }
        }


    }
}

class LoginViewModel {

    var username = "user"
    var password = "pass"
}
