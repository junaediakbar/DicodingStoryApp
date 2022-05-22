package com.juned.dicodingstoryapp.ui.view.home

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.juned.dicodingstoryapp.R
import com.juned.dicodingstoryapp.data.api.ApiConfig
import com.juned.dicodingstoryapp.data.database.StoryDatabase
import com.juned.dicodingstoryapp.data.paging.LoadingStateAdapter
import com.juned.dicodingstoryapp.data.paging.StoriesAdapter
import com.juned.dicodingstoryapp.data.pref.SessionPreferences
import com.juned.dicodingstoryapp.data.repository.StoryRepository
import com.juned.dicodingstoryapp.databinding.ActivityHomeBinding
import com.juned.dicodingstoryapp.helper.visibility
import com.juned.dicodingstoryapp.ui.view.SessionViewModel
import com.juned.dicodingstoryapp.ui.view.login.LoginActivity
import com.juned.dicodingstoryapp.ui.view.story.AddStoryActivity
import com.juned.dicodingstoryapp.ui.view.storywithmaps.StoryWithMapsActivity


internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class HomeActivity : AppCompatActivity() {
    private val sessionViewModel by viewModels<SessionViewModel> {
        SessionViewModel.Factory(SessionPreferences.getInstance(dataStore))
    }

    private var token = ""

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        showLoading(true)

        sessionViewModel.getToken().observe(this@HomeActivity) { token ->
            this.token = token
            if (token.isEmpty() || token == "") {
                goToLogin()

            } else {
                setStories()
            }
        }

        binding?.btnAddStory?.setOnClickListener{
            startAddStory()
        }

        binding?.btnToMaps?.setOnClickListener{
            startMaps()
        }

    }

    private fun setStories(){
        val viewModel by viewModels<HomeViewModel> {
            HomeViewModel.Factory(
                StoryRepository(
                    StoryDatabase.getDatabase(this@HomeActivity),
                    ApiConfig.getApiService(),
                    getString(R.string.auth, token)
                )
            )
        }

        val adapter = StoriesAdapter()
        binding?.apply {
            rvHomeStories.layoutManager = if (resources.configuration.orientation
                == Configuration.ORIENTATION_PORTRAIT
            ) {
                LinearLayoutManager(this@HomeActivity)
            } else {
                GridLayoutManager(this@HomeActivity, 2)
            }
            rvHomeStories.adapter = adapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    adapter.retry()
                }
            )
            showLoading(false)
        }

        viewModel.stories.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    private fun startMaps(){
        val intent = Intent(this@HomeActivity, StoryWithMapsActivity::class.java).apply {
            putExtra(AddStoryActivity.EXTRA_TOKEN,token)
        }
        startActivity(intent)
    }

    private fun startAddStory(){
        val intent = Intent(this@HomeActivity, AddStoryActivity::class.java).apply {
            putExtra(AddStoryActivity.EXTRA_TOKEN,token)
        }
        startActivity(intent)
    }

    private fun goToLogin() {
        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBarHome?.visibility = visibility(isLoading)
    }

    override fun onResume() {
        super.onResume()
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                sessionViewModel.saveToken("")
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.setting -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
        }

        return true
    }

    companion object
}