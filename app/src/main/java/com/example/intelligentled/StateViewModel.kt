package com.example.intelligentled

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

//使用ViewModel来存储状态信息
object StateViewModel:ViewModel() {
    private var _isOn= mutableStateOf(false)
    val isOn: State<Boolean> get() = _isOn

    private var _isOnline= mutableStateOf(false)
    val isOnline:State<Boolean> get() = _isOnline

    private var _updateTime= mutableStateOf("")
    val updateTime:State<String> get() = _updateTime

    fun deviceIsOn(){
        _isOn.value=true
    }

    fun deviceIsOff(){
        _isOn.value=false
    }

    fun deviceIsOnline(){
        _isOnline.value=true
    }

    fun deviceIsOffline(){
        _isOnline.value=false
    }

    fun setUpdateTime(string:String){
        _updateTime.value=string
    }
}