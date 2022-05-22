package com.juned.dicodingstoryapp.ui.view.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.juned.dicodingstoryapp.data.repository.StoryRepository

class HomeViewModel (storyRepository: StoryRepository) : ViewModel() {
    val stories = storyRepository.getStoriesPaged().cachedIn(viewModelScope)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val storyRepository: StoryRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(storyRepository) as T
        }
    }
}