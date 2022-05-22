package com.juned.dicodingstoryapp.ui.view.storywithmaps

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.juned.dicodingstoryapp.R
import com.juned.dicodingstoryapp.data.api.ApiConfig
import com.juned.dicodingstoryapp.data.api.response.StoryItem
import com.juned.dicodingstoryapp.data.database.StoryDatabase
import com.juned.dicodingstoryapp.data.repository.StoryRepository
import com.juned.dicodingstoryapp.databinding.ActivityStoryWithMapsBinding
import com.juned.dicodingstoryapp.ui.view.story.DetailStoryActivity

class StoryWithMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap : GoogleMap
    private lateinit var binding: ActivityStoryWithMapsBinding

    private var focusedStory: StoryItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryWithMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap =googleMap

        val token = intent.getStringExtra(EXTRA_TOKEN).toString()
        Log.d("token",token)

        val viewModel by viewModels<StoriesWithMapViewModel> {
            StoriesWithMapViewModel.Factory(StoryRepository(
                StoryDatabase.getDatabase(this@StoryWithMapsActivity),
                ApiConfig.getApiService(),
                getString(R.string.auth, token)
            ))
        }

        setMapStyle(mMap)

        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }


        mMap.setOnInfoWindowClickListener  {
            val story = it.tag as StoryItem

            focusedStory = story
            val intent = Intent(this@StoryWithMapsActivity, DetailStoryActivity::class.java)
            intent.putExtra(DetailStoryActivity.EXTRA_STORY, story)
            startActivity(intent)
        }

        viewModel.stories.observe(this@StoryWithMapsActivity) {
            mMap.clear()

            it.forEach { story ->
                val latLng = LatLng(story.lat, story.lon)
                val marker = mMap.addMarker(
                    MarkerOptions().position(latLng)
                        .title(getString(R.string.stories_content_description, story.name))
                        .snippet(getString(R.string.marker_snippet))
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        )
                )
                marker?.tag = story
            }

            val story = focusedStory ?: it[(1..it.size).random() - 1]



            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(story.lat, story.lon), 7f)
            )
        }

        getMyLocation()

    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            this.let {
                val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        it,
                        R.raw.map_style
                    )
                )

                if (!success) {
                    Log.e(TAG, "Style parsing failed.")
                }
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object{
        const val EXTRA_TOKEN = "extra_token"
    }
}