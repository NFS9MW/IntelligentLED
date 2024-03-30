package com.example.intelligentled.network

import com.google.gson.Gson

//使用此单例类来解析各个请求传回的json文件
object GSONUtil {
    //开关灯操作返回数据解析
    fun operationParse(jsonData:String):Int{
        //GSON可以将一段JSON格式的字符串自动映射成一个对象
        val gson= Gson()
        val response=gson.fromJson(jsonData,OperationResponse::class.java)

        return response.code
    }

    //查询状态操作返回数据解析
    fun stateParse(jsonData: String): Map<String, String> {
        val gson = Gson()
        val response = gson.fromJson(jsonData, StateResponse::class.java)


        return mapOf(
            "code" to response.code.toString(),
            "msg" to response.data[0].msg,
            "time" to response.data[0].time
        )
    }

    //查询在线操作返回数据解析
    fun onlineParse(jsonData: String):Map<String,String>{
        val gson =Gson()
        val response = gson.fromJson(jsonData,OnlineResponse::class.java)

        return mapOf(
            "code" to response.code.toString(),
            "data" to response.data.toString()
        )
    }
}