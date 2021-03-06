package com.juned.dicodingstoryapp.ui.view.story


import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.juned.dicodingstoryapp.R
import com.juned.dicodingstoryapp.data.api.ApiConfig
import com.juned.dicodingstoryapp.data.database.StoryDatabase
import com.juned.dicodingstoryapp.data.repository.StoryRepository
import com.juned.dicodingstoryapp.databinding.ActivityAddStoryBinding
import com.juned.dicodingstoryapp.helper.*
import com.juned.dicodingstoryapp.ui.view.home.HomeActivity
import com.juned.dicodingstoryapp.ui.widget.text.EditTextGeneral
import java.io.*

class AddStoryActivity : AppCompatActivity() {

    private var _binding: ActivityAddStoryBinding? = null
    private val binding get() = _binding

    private var tempTakenImageFile: File? = null

    private var location: Location? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        val token = intent.getStringExtra(EXTRA_TOKEN).toString()

        val addStoryViewModel  by viewModels<AddStoryViewModel> {
            AddStoryViewModel.Factory(
                StoryRepository(
                    StoryDatabase.getDatabase(this@AddStoryActivity),
                    ApiConfig.getApiService(),
                    getString(R.string.auth, token)
                )
            )
        }

        binding?.apply {
            tvDescription.setValidationCallback(object : EditTextGeneral.InputValidation {
                override val errorMessage: String
                    get() = getString(R.string.desc_validation_message)
                override fun validate(input: String) = input.isNotEmpty()
            })

            cameraXButton.setOnClickListener { startCameraX() }
            galleryButton.setOnClickListener { startGallery() }
            uploadButton.setOnClickListener { uploadImage(addStoryViewModel) }
        }

        addStoryViewModel.apply {
            isLoading.observe(this@AddStoryActivity) {
                showLoading(it)
            }

            isSuccess.observe(this@AddStoryActivity) {
                it.getContentIfNotHandled()?.let { success ->
                    if (success) {
                        goToHome()
                    }
                }
            }

            error.observe(this@AddStoryActivity) {
                it.getContentIfNotHandled()?.let { message ->
                    binding?.root?.let { it1 -> showSnackBar(it1, message) }
                }
            }
        }
        getLocation()
    }

    private fun goToHome(){
        val intent = Intent(this@AddStoryActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.uploadProgressBar?.visibility = visibility(isLoading)
        binding?.uploadLoadingText?.visibility = visibility(isLoading)
    }


    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }


    private fun uploadImage(viewModel: AddStoryViewModel) {
        if (tempTakenImageFile != null && binding?.tvDescription?.validateInput() == true) {
            val file = reduceFileImage(tempTakenImageFile as File)

            val description = binding?.tvDescription?.text.toString()
            viewModel.uploadStory(file,description, location )
        }else{
            Toast.makeText(this@AddStoryActivity, R.string.error_upload_empty, Toast.LENGTH_SHORT).show()
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            tempTakenImageFile = myFile
            val result = BitmapFactory.decodeFile(myFile.path)



            binding?.imgUploadPreview?.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@AddStoryActivity)
            tempTakenImageFile = myFile

            binding?.imgUploadPreview?.setImageURI(selectedImg)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (!allPermissionsGranted()) {
            binding?.root?.let {
                showSnackBar(it, getString(R.string.permission_denied))
            }
        }
    }


    private fun getLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    binding?.locationText?.text=getString(R.string.location,location.latitude,location.longitude)
                } else {
                    Toast.makeText(
                        this@AddStoryActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        const val EXTRA_TOKEN = "extra_token"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }


}