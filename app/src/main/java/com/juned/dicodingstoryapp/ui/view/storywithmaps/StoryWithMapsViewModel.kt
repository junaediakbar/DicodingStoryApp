package com.juned.dicodingstoryapp.ui.view.storywithmaps

import androidx.lifecycle.*
import com.juned.dicodingstoryapp.data.api.response.StoryItem
import com.juned.dicodingstoryapp.data.repository.StoryRepository
import com.juned.dicodingstoryapp.helper.Event
import com.juned.dicodingstoryapp.helper.getErrorResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StoriesWithMapViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<List<StoryItem>>()
    val stories: LiveData<List<StoryItem>> = _stories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    init {
        getAllStories()
    }

    private fun getAllStories() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _stories.value = storyRepository.getStoriesWithLocation()
            } catch (httpEx: HttpException) {
                httpEx.response()?.errorBody()?.let {
                    val errorResponse = getErrorResponse(it)

                    _error.value = Event(errorResponse.message)
                }
            } catch (ex: Exception) {
                _error.value = Event(ex.localizedMessage ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val storyRepository: StoryRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StoriesWithMapViewModel(storyRepository) as T
        }
    }
}