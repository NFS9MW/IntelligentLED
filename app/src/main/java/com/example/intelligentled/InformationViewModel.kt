package com.example.intelligentled

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

object InformationViewModel:ViewModel() {
    private var _key= mutableStateOf("")
    private var _theme= mutableStateOf("")

    val key get() = _key
    val theme get() = _theme

    fun setKey(key:String){
        _key.value=key
    }

    fun setTheme(theme:String){
        _theme.value=theme
    }
}