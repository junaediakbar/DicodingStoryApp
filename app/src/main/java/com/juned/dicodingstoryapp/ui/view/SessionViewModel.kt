package com.juned.dicodingstoryapp.ui.view

import androidx.lifecycle.*
import com.juned.dicodingstoryapp.data.pref.SessionPreferences
import kotlinx.coroutines.launch

class SessionViewModel (private val pref: SessionPreferences) : ViewModel() {
    fun getToken(): LiveData<String> {
        return pref.getSavedToken().asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            pref.saveToken(token)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val pref: SessionPreferences) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SessionViewModel(pref) as T
        }
    }
}