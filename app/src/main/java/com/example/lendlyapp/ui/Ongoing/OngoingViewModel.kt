package com.example.lendlyapp.ui.ongoing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OngoingViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is Ongoing Fragment"
    }
    val text: LiveData<String> = _text
}
