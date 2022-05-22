package com.juned.dicodingstoryapp.ui.view.login

import androidx.lifecycle.*
import com.google.gson.Gson
import com.juned.dicodingstoryapp.data.api.ApiConfig
import com.juned.dicodingstoryapp.data.api.response.LoginResponse
import com.juned.dicodingstoryapp.data.api.response.GeneralResponse
import com.juned.dicodingstoryapp.data.repository.AuthRepository
import com.juned.dicodingstoryapp.helper.Event
import com.juned.dicodingstoryapp.helper.getErrorResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _token = MutableLiveData<Event<String>>()
    val token: LiveData<Event<String>> = _token

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    fun login(email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _token.value = Event(authRepository.login(email, password))
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
    class Factory(private val authRepository: AuthRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository) as T
        }
    }
}