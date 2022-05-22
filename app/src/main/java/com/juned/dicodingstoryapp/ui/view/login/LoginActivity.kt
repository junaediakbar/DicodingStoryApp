package com.juned.dicodingstoryapp.ui.view.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.juned.dicodingstoryapp.R
import com.juned.dicodingstoryapp.data.api.ApiConfig
import com.juned.dicodingstoryapp.data.pref.SessionPreferences
import com.juned.dicodingstoryapp.data.repository.AuthRepository
import com.juned.dicodingstoryapp.databinding.ActivityLoginBinding
import com.juned.dicodingstoryapp.helper.hideKeyboard
import com.juned.dicodingstoryapp.helper.showSnackBar
import com.juned.dicodingstoryapp.helper.visibility
import com.juned.dicodingstoryapp.ui.view.home.HomeActivity
import com.juned.dicodingstoryapp.ui.view.home.dataStore
import com.juned.dicodingstoryapp.ui.view.register.RegisterActivity
import com.juned.dicodingstoryapp.ui.widget.text.EditTextGeneral

import com.juned.dicodingstoryapp.ui.view.SessionViewModel

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding

    private val loginViewModel by viewModels<LoginViewModel> {
        LoginViewModel.Factory(
            AuthRepository(
                ApiConfig.getApiService()
            )
        )
    }

    private val sessionViewModel by viewModels<SessionViewModel> {
        SessionViewModel.Factory(SessionPreferences.getInstance(dataStore))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupTextField()
        setupButton()
        setupSignifier()
        checkIsLogin()

    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
    }

    private fun checkIsLogin() {
        sessionViewModel.getToken().observe(this@LoginActivity) {
            if (it.isNotEmpty()) {
                goToHome()
            }
        }
    }

    private fun setupSignifier() {
        loginViewModel.apply {
            isLoading.observe(this@LoginActivity, ::showLoading)


            token.observe(this@LoginActivity) { e ->
                e.getContentIfNotHandled()?.let {
                    loggedIn(it)
                }
            }

            error.observe(this@LoginActivity) { e ->
                e.getContentIfNotHandled()?.let { message ->
                    binding?.root?.let { showSnackBar(it, message) }
                }
            }
        }
    }

    private fun setupTextField() {
        binding?.apply {
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

    private fun setupButton() {
        binding?.apply {
            btnToRegister.setOnClickListener {
                goToRegister()
            }

            btnLogin.setOnClickListener {
                tryLogin()
                hideKeyboard(this@LoginActivity)
            }
        }
    }

    private fun goToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loggedIn(token: String) {
        sessionViewModel.saveToken(token)
        binding?.root?.let { showSnackBar(it, getString(R.string.login_success)) }
    }


    private fun goToHome() {
        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun tryLogin() {
        with(binding) {
            val isEmailValid = this?.edtEmail?.validateInput()
            val isPasswordValid = this?.edtPassword?.validateInput()

            if (!isEmailValid!! || !isPasswordValid!!) {
                binding?.root?.let { showSnackBar(it, getString(R.string.validation_error)) }
                return
            }

            loginViewModel.login(
                this?.edtEmail?.text.toString(),
                this?.edtPassword?.text.toString()
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.loadingProgressBar?.visibility = visibility(isLoading)
    }

}