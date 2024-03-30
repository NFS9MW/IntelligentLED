package com.example.intelligentled.network

//发送操作指令后服务器传回的数据，使用数据类进行封装，交给GSON解析
data class OperationResponse(val code: Int, val message: String, val data: Int)