package com.juned.dicodingstoryapp.ui.view.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.juned.dicodingstoryapp.R
import com.juned.dicodingstoryapp.data.api.ApiConfig
import com.juned.dicodingstoryapp.data.repository.AuthRepository
import com.juned.dicodingstoryapp.databinding.ActivityRegisterBinding
import com.juned.dicodingstoryapp.helper.hideKeyboard
import com.juned.dicodingstoryapp.helper.showSnackBar
import com.juned.dicodingstoryapp.helper.visibility
import com.juned.dicodingstoryapp.ui.view.login.LoginActivity
import com.juned.dicodingstoryapp.ui.widget.text.EditTextGeneral

class RegisterActivity : AppCompatActivity() {

    private val registerViewModel by viewModels<RegisterViewModel> {
        RegisterViewModel.Factory(
            AuthRepository(
            ApiConfig.getApiService()
        )
        )
    }

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupTextField()
        setupButton()
        setupSignifier()
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
    }

    private fun setupSignifier(){
        registerViewModel.apply {
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

    private fun setupTextField(){
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
        }
    }

    private fun setupButton(){
        binding?.apply {
            btnRegister.setOnClickListener {
                tryRegister()
                hideKeyboard(this@RegisterActivity)
            }

            btnRegToLogin.setOnClickListener {
                goToLogin()
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
        binding?.registerProgressBar?.visibility = visibility(isLoading)
    }

    private fun tryRegister() {
        binding?.apply {

            val isNameValid =edtNama.validateInput()
            val isEmailValid = edtEmail.validateInput()
            val isPasswordValid = edtPassword.validateInput()

            if (!isNameValid || !isEmailValid || !isPasswordValid) {
                binding?.root?.let { showSnackBar(it, getString(R.string.validation_error)) }
                return
            }

            registerViewModel.register(
                edtNama.text.toString(),
                edtEmail.text.toString(),
                edtPassword.text.toString()
            )
        }
    }

}