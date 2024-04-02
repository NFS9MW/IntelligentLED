package com.example.intelligentled.network

import android.content.Context
import android.util.Log
import com.example.intelligentled.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

//通过此单例类发送网络请求，获取到的json数据将交给GSON进行解析
object HttpUtil{
    //发送操作指令（开、关灯），使用POST接口
    fun sendOperation(operation:String):CompletableFuture<Int> {

        val future = CompletableFuture<Int>()

        thread {
            try {
                //取出持久化保存的数据
                val sp=MyApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE)
                val key:String?=sp.getString("key","key is null")
                val theme:String?=sp.getString("theme","theme is null")

                val client = OkHttpClient()

                //trimIndent() 是 Kotlin 中的一个字符串处理函数，它的作用是移除字符串中每一行开头的空白字符，直到非空白字符为止，并返回一个新的字符串。
                val json = """
                {
                    "uid": "$key",
                    "topic": "$theme",
                    "type": 3,
                    "msg": "$operation"
                }
            """.trimIndent()
                /*  toRequestBody() 是 OkHttp 库中 String 类的扩展函数，它的作用是将字符串转换为请求体 (RequestBody)
                  对象。这个函数接受一个参数，即要设置的媒体类型 (MediaType)，并返回一个请求体对象。
                    toMediaType() 也是 OkHttp 库中 String 类的扩展函数，它的作用是将字符串解析为媒体类型 (MediaType) 对象。
                 */
                val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder() //如果想要发起一条HTTP请求，则需要创建一个Request对象
                    .url("https://apis.bemfa.com/va/postJsonMsg") //可以在.build()方法前连缀其他方法来丰富Request对象，如url()方法可以设置网络地址
                    .post(requestBody)
                    .build()

                //调用OkHttpClient的newCall()方法来创建一个Call对象，并调用它的execute()方法来发送请求并获取服务器返回的数据
                val response = client.newCall(request).execute()
                //Response对象就是服务器返回的数据，下方代码写法可以获取返回的具体内容
                val responseData=response.body?.string()

                if (responseData != null) {
                    // 在协程中解析 responseData
                    GlobalScope.launch {
                        withContext(Dispatchers.Default) {
                            val parsedData = GSONUtil.operationParse(responseData)
                            future.complete(parsedData)
                        }
                    }
                } else {
                    future.complete(null)
                }
            }catch (e:Exception){
                e.printStackTrace()
                future.completeExceptionally(e)
            }
        }
        return future
    }

    //发送状态查询指令，使用GET接口
    fun sendStateRequest():CompletableFuture<Map<String,String>>{

        val future=CompletableFuture<Map<String,String>>()

        thread {
            //取出持久化保存的数据
            val sp=MyApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE)
            val key:String?=sp.getString("key","key is null")
            val theme:String?=sp.getString("theme","theme is null")

            try {
                //使用GET接口传递参数
                val urlBuilder = "https://apis.bemfa.com/va/getmsg".toHttpUrlOrNull()?.newBuilder()
                urlBuilder?.addQueryParameter("uid", "$key")
                urlBuilder?.addQueryParameter("topic", "$theme")
                urlBuilder?.addQueryParameter("type", 3.toString())

                val url = urlBuilder?.build().toString()
                val client = OkHttpClient()

                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                val responseData= response.body?.string()

                if (responseData != null) {
                    Log.w("sendStateRequest",responseData)
                }

                if (responseData!=null){
                    // 在协程中解析 responseData
                    GlobalScope.launch {
                        withContext(Dispatchers.Default) {
                            val parsedData = GSONUtil.stateParse(responseData)
                            future.complete(parsedData)
                        }
                    }
                } else {
                    future.complete(null)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return future
    }

    //发送在线查询指令，使用GET接口
    fun sendOnlineRequest():CompletableFuture<Map<String,String>>{

        val future=CompletableFuture<Map<String,String>>()

        thread {
            //取出持久化保存的数据
            val sp=MyApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE)
            val key:String?=sp.getString("key","key is null")
            val theme:String?=sp.getString("theme","theme is null")

            try {
                //使用GET接口传递参数
                val urlBuilder = "https://apis.bemfa.com/va/online".toHttpUrlOrNull()?.newBuilder()
                urlBuilder?.addQueryParameter("uid", "$key")
                urlBuilder?.addQueryParameter("topic", "$theme")
                urlBuilder?.addQueryParameter("type", 3.toString())

                val url = urlBuilder?.build().toString()
                val client = OkHttpClient()

                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                val responseData= response.body?.string()

                if (responseData != null) {
                    Log.w("sendOnlineRequest",responseData)
                }

                if (responseData!=null){
                    // 在协程中解析 responseData
                    GlobalScope.launch {
                        withContext(Dispatchers.Default) {
                            val parsedData = GSONUtil.onlineParse(responseData)
                            future.complete(parsedData)
                        }
                    }
                } else {
                    future.complete(null)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return future
    }
}
