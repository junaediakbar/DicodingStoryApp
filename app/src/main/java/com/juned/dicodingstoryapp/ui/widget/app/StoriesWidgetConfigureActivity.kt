package com.juned.dicodingstoryapp.ui.widget.app

import com.juned.dicodingstoryapp.data.pref.SessionPreferences
import com.juned.dicodingstoryapp.ui.view.home.dataStore
import com.juned.dicodingstoryapp.ui.view.login.LoginViewModel
import com.juned.dicodingstoryapp.ui.view.SessionViewModel

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.juned.dicodingstoryapp.R
import com.juned.dicodingstoryapp.data.api.ApiConfig
import com.juned.dicodingstoryapp.data.repository.AuthRepository
import com.juned.dicodingstoryapp.databinding.ActivityStoriesWidgetConfigureBinding
import com.juned.dicodingstoryapp.helper.showSnackBar
import com.juned.dicodingstoryapp.helper.visibility
import com.juned.dicodingstoryapp.ui.widget.text.EditTextGeneral

class StoriesWidgetConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var binding: ActivityStoriesWidgetConfigureBinding

    private val sessionViewModel by viewModels<SessionViewModel> {
        SessionViewModel.Factory(SessionPreferences.getInstance(dataStore))
    }

    private val loginViewModel by viewModels<LoginViewModel> {
        LoginViewModel.Factory(
            AuthRepository(
                ApiConfig.getApiService()
            )
        )
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        setResult(RESULT_CANCELED)

        binding = ActivityStoriesWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading(false)

        sessionViewModel.getToken().observe(this) {
            if (it.isNotEmpty() || it !== "") {
                showWidget()
            }
        }

        loginViewModel.apply {
            isLoading.observe(this@StoriesWidgetConfigureActivity) {
                showLoading(it)
            }

            token.observe(this@StoriesWidgetConfigureActivity) {
                it.getContentIfNotHandled()?.let { token ->
                    Toast.makeText(
                        this@StoriesWidgetConfigureActivity,
                        getString(R.string.login_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    sessionViewModel.saveToken(token)
                }
            }

            error.observe(this@StoriesWidgetConfigureActivity) {
                it.getContentIfNotHandled()?.let { message ->
                    showSnackBar(binding.root, message)
                }
            }
        }

        binding.apply {
            edtEmail.setValidationCallback(object : EditTextGeneral.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.email_validation_message)

                override fun validate(input: String) = input.isNotEmpty()
                        && Patterns.EMAIL_ADDRESS.matcher(input).matches()
            })

            edtPassword.setValidationCallback(object : EditTextGeneral.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.password_validation_message)

                override fun validate(input: String) = input.length >= 6
            })

            btnLogin.setOnClickListener {
                val isEmailValid = edtEmail.validateInput()
                val isPasswordValid = edtPassword.validateInput()

                if (!isEmailValid || !isPasswordValid) {
                    showSnackBar(root, getString(R.string.validation_error))
                    return@setOnClickListener
                }

                loginViewModel.login(edtEmail.text.toString(), edtPassword.text.toString())
            }
        }

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.wigdetProgressBar.visibility = visibility(isLoading)
    }

    private fun showWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        StoriesWidget.updateAppWidget(this, appWidgetManager, appWidgetId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}