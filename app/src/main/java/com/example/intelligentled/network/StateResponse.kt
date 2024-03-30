package com.example.intelligentled.network

//发送查询状态指令后服务器传回的数据，使用数据类进行封装，交给GSON解析
data class StateResponse(val code: Int,
                         val message: String,
                         val data: List<MyData>)

data class MyData(val msg: String,
                  val time: String,
                  val unix: Long)