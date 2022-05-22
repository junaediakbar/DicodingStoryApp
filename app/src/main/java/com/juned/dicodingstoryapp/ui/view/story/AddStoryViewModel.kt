package com.juned.dicodingstoryapp.ui.view.story

import android.location.Location
import androidx.lifecycle.*
import com.juned.dicodingstoryapp.data.api.ApiService
import com.juned.dicodingstoryapp.data.repository.StoryRepository
import com.juned.dicodingstoryapp.helper.Event
import com.juned.dicodingstoryapp.helper.getErrorResponse
import com.juned.dicodingstoryapp.helper.reduceFileImage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Event<Boolean>>()
    val isSuccess: LiveData<Event<Boolean>> = _isSuccess

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    fun uploadStory(image: File, description: String, location: Location? = null, compress: Boolean = true) {
        val reducedImage = if (compress) {
            reduceFileImage(image)
        } else {
            image
        }

        val descPart = description.toRequestBody("text/plain".toMediaType())
        val imageMultiPart = MultipartBody.Part.createFormData(
            ApiService.PHOTO_FIELD,
            reducedImage.name,
            reducedImage.asRequestBody("image/jpeg".toMediaType())
        )

        val params = mutableMapOf(
            "description" to descPart
        )

        if (location != null) {
            val latPart = location.latitude.toString().toRequestBody("text/plain".toMediaType())
            val lonPart = location.longitude.toString().toRequestBody("text/plain".toMediaType())

            params.apply {
                put("lat", latPart)
                put("lon", lonPart)
            }
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                _isSuccess.value = Event(storyRepository.addNewStory(imageMultiPart, HashMap(params)))
            } catch (httpEx: HttpException) {
                httpEx.response()?.errorBody()?.let {
                    val errorResponse = getErrorResponse(it)

                    _error.value = Event(errorResponse.message)
                }
            } catch (genericEx: Exception) {
                _error.value = Event(genericEx.localizedMessage ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val storyRepository: StoryRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddStoryViewModel(storyRepository) as T
        }
    }
}