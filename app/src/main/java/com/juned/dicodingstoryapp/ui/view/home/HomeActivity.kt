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
import com.juned.dicodingstoryapp.data.adapter.StoriesAdapter
import com.juned.dicodingstoryapp.data.api.response.StoryItem
import com.juned.dicodingstoryapp.data.pref.SessionPreferences
import com.juned.dicodingstoryapp.databinding.ActivityHomeBinding
import com.juned.dicodingstoryapp.helper.visibility
import com.juned.dicodingstoryapp.ui.view.login.LoginActivity
import com.juned.dicodingstoryapp.ui.view.story.AddStoryActivity
import com.juned.dicodingstoryapp.ui.view.SessionViewModel

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

        sessionViewModel.getToken().observe(this@HomeActivity) { token ->
            this.token = token
            if (token.isEmpty() || token == "") {
                goToLogin()
            } else {
                val homeViewModel by viewModels<HomeViewModel> {
                    HomeViewModel.Factory(getString(R.string.auth, token))
                }

                homeViewModel.apply {
                    isLoading.observe(this@HomeActivity) {
                        showLoading(it)
                    }

                    stories.observe(this@HomeActivity) {
                        setStories(ArrayList(it))
                        binding?.tvNotFound?.visibility = visibility(it.isEmpty())
                    }

                }
            }

        }
        binding?.btnAddStory?.setOnClickListener{
                startAddStory()
        }
    }

    private fun startAddStory(){
        val intent = Intent(this@HomeActivity, AddStoryActivity::class.java).apply {
            putExtra(AddStoryActivity.EXTRA_TOKEN,token)
        }
        startActivity(intent)
    }

    private fun setStories(stories: ArrayList<StoryItem>) {
        showLoading(true)
        binding?.apply {
            rvStories.layoutManager = if (resources.configuration.orientation
                == Configuration.ORIENTATION_PORTRAIT
            ) {
                LinearLayoutManager(this@HomeActivity)
            } else {
                GridLayoutManager(this@HomeActivity, 2)
            }

            rvStories.setHasFixedSize(true)
            rvStories.adapter = StoriesAdapter(stories)

            tvNotFound.apply {
                if (stories.isEmpty()) {
                    visibility = visibility(true)
                    text = getString(R.string.not_found)
                } else {
                    visibility = visibility(false)
                }
            }
            showLoading(false)
        }

    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding?.storyProgressBar?.visibility = visibility(true)
        } else {
            binding?.storyProgressBar?.visibility = visibility(false)
        }
    }

    private fun goToLogin() {
        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
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