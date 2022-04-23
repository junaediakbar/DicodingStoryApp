package com.juned.dicodingstoryapp.ui.view.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.juned.dicodingstoryapp.R
import com.juned.dicodingstoryapp.databinding.ActivityRegisterBinding
import com.juned.dicodingstoryapp.helper.hideKeyboard
import com.juned.dicodingstoryapp.helper.showSnackBar
import com.juned.dicodingstoryapp.helper.visibility
import com.juned.dicodingstoryapp.ui.view.login.LoginActivity
import com.juned.dicodingstoryapp.ui.widget.text.EditTextGeneral

class RegisterActivity : AppCompatActivity() {

    private val viewModel by viewModels<RegisterViewModel>()

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.apply {
            edtNama.setValidationCallback(object : EditTextGeneral.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.name_validation_message)

                override fun validate(input: String) = input.isNotEmpty()
            })

            edtEmail.setValidationCallback(object : EditTextGeneral.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.name_validation_message)

                override fun validate(input: String) = input.isNotEmpty()
            })

            edtPassword.setValidationCallback(object : EditTextGeneral.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.password_validation_message)

                override fun validate(input: String) = input.length >= 6
            })

            btnRegister.setOnClickListener {
                tryRegister()
                hideKeyboard(this@RegisterActivity)
            }

            btnToLogin.setOnClickListener {
              goToLogin()
            }

        }

        viewModel.apply {
            isLoading.observe(this@RegisterActivity) {
                showLoading(it)
            }

            isSuccess.observe(this@RegisterActivity) {
                it.getContentIfNotHandled()?.let { success ->
                    if (success) {
                        registerSuccess()
                    }
                }
            }

            error.observe(this@RegisterActivity) { e ->
                e.getContentIfNotHandled()?.let { message ->
                    binding?.root?.let { showSnackBar(it, message) }
                }
            }
        }
    }

    private fun goToLogin(){
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerSuccess() {
        binding?.root?.let { showSnackBar(it, getString(R.string.register_success)) }
        val intent= Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding?.progressBar?.visibility = visibility(true)
        } else {
            binding?.progressBar?.visibility = visibility(false)
        }
    }

    private fun tryRegister() {
        with(binding) {

            val isNameValid = this?.edtNama?.validateInput()
            val isEmailValid = this?.edtEmail?.validateInput()
            val isPasswordValid = this?.edtPassword?.validateInput()

            if (!isNameValid!! || !isEmailValid!! || !isPasswordValid!!) {
                binding?.root?.let { showSnackBar(it, getString(R.string.validation_error)) }
                return
            }

            viewModel.register(
                this?.edtNama?.text.toString(),
                this?.edtEmail?.text.toString(),
                this?.edtPassword?.text.toString()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
    }
}