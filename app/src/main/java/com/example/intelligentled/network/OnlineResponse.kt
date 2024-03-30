package com.example.intelligentled.network

//发送在线查询指令后服务器传回的数据，使用数据类进行封装，交给GSON解析
data class OnlineResponse(val code: Int, val message: String, val data: Boolean)