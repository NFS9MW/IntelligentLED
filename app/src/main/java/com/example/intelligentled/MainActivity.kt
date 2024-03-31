package com.example.intelligentled

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intelligentled.network.HttpUtil
import com.example.intelligentled.ui.theme.IntelligentLEDTheme

class MainActivity : ComponentActivity() {

    private lateinit var runnable: Runnable

    val viewModel by lazy { ViewModelProvider(this)[StateViewModel::class.java] }
    //定义定时任务
    private val handler= object : Handler(Looper.getMainLooper()){
        override fun handleMessage(message: Message) {
            super.handleMessage(message)
            when(message.what){
                1->{
                    val stateFuture=HttpUtil.sendStateRequest()
                    val onlineFuture=HttpUtil.sendOnlineRequest()

                    //刷新状态
                    stateFuture.thenAccept {
                        val code=it["code"]
                        val msg=it["msg"]
                        val time=it["time"]

                        if (code!=null){
                            Log.w("Runnable",code)
                            if (msg != null) {
                                Log.w("Runnable",msg)
                            }
                            if (time != null) {
                                Log.w("Runnable",time)
                            }
                            //传回了正常数据
                            if (code.toInt()==0){
                                if (msg=="on"){
                                    viewModel.deviceIsOn()
                                }else if (msg=="off"){
                                    viewModel.deviceIsOff()
                                }
                                if (time!=null){
                                    viewModel.setUpdateTime(time)
                                }
                            }else{
                                Log.w("Handler","code is $code")
                            }
                        }else{
                            // 处理结果为 null 的情况
                            Log.w("Handler","code is null")
                        }
                    }.exceptionally { ex ->
                        // 处理异常情况
                        Log.w("stateFuture","Exception occurred: ${ex.message}")
                        null
                    }

                    //刷新在线
                    onlineFuture.thenAccept {
                        val code=it["code"]
                        val data=it["data"]

                        if (code != null) {
                            if (code.toInt()==0){
                                //传回了正常数据
                                if (data.toBoolean()){
                                    viewModel.deviceIsOnline()
                                }else{
                                    viewModel.deviceIsOffline()
                                }
                            }
                        }else{
                            // 处理结果为 null 的情况
                            Log.w("Handler","code is null")
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelligentLEDTheme {
                Screen()
            }
        }

        //创建需要定时执行的任务
        runnable = Runnable {
            handler.postDelayed(runnable, 1000) //每隔1秒执行
            val message = Message()
            message.what = 1
            handler.sendMessage(message)
        }

        //启动定时器
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) //取消定时器
    }
}

//组件拼装部分
@Composable
fun Screen(){
    //Compose中获取viewModel的方式，需要引入"androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
    val viewModel:StateViewModel= viewModel()

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer) //背景颜色适配深色模式
    ) {
        //根据是否为深色模式来切换背景图
        if (isSystemInDarkTheme()){
            Image(
                painter= rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.ic_dark_welcome_bg)),
                contentDescription = "welcome_bg",
                modifier = Modifier
                    .fillMaxSize()
            )
        }else{
            Image(
                painter= rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.ic_light_welcome_bg)),
                contentDescription = "welcome_bg",
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier= Modifier
                .fillMaxSize()
                //启用可滚动是为了解决横屏时部件显示不正常的问题
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier=Modifier.size(20.dp))
            Bulb(isOn = viewModel.isOn.value) //灯泡显示部分
            InformationCard( //信息卡片部分
                isOn = viewModel.isOn.value,
                isOnline = viewModel.isOnline.value,
                time = viewModel.updateTime.value
            )
            OperationButton(viewModel=viewModel) //操作按钮部分
            Spacer(modifier = Modifier.size(50.dp)) //留白，方便操作
        }
    }
}

//灯泡显示部分
@Composable
fun Bulb(isOn: Boolean){
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(300.dp)
    ){
        //淡入淡出动画
        AnimatedVisibility(
            visible = isOn,
            enter = fadeIn(),
            exit = fadeOut(),
            label = "灯泡状态发生变化时的淡入淡出动画"
        ) {
             Image(
                 painter = painterResource(id = R.drawable.bulb_light),
                 contentDescription = "灯泡亮起")
        }

        //淡入淡出动画
        AnimatedVisibility(
            visible = !isOn,
            enter = fadeIn(),
            exit = fadeOut(),
            label = "灯泡状态发生变化时的淡入淡出动画"
        ) {
            Image(
                painter = painterResource(id = R.drawable.bulb_dark),
                contentDescription = "灯泡熄灭")

        }
//        if (isOn){
//            Image(
//                painter = painterResource(id = R.drawable.bulb_light),
//                contentDescription = "灯泡亮起")
//        }else{
//            Image(
//                painter = painterResource(id = R.drawable.bulb_dark),
//                contentDescription = "灯泡熄灭")
//        }
    }
}

//设备信息显示部分
@Composable
fun InformationCard(isOn: Boolean,isOnline:Boolean,time:String){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        OutlinedCard(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(50.dp)
        ) {
            Spacer(modifier = Modifier.size(10.dp))
            if (isOnline){
                Text(
                    text = "设备在线",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }else{
                Text(
                    text = "设备离线",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            if (isOn){
                Text(
                    text = "已开灯",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }else{
                Text(
                    text = "已关灯",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = "数据更新时间: $time",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}

//操作按钮部分
@Composable
fun OperationButton(viewModel: StateViewModel){

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier= Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier= Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 100.dp)
        ) {
            if (viewModel.isOn.value){
                ExtendedFloatingActionButton(onClick = {
                    val resultFuture = HttpUtil.sendOperation("off")

                    resultFuture.thenAccept { code ->
                        if (code != null) {
                            if (code==0){
                                viewModel.deviceIsOff()
                            }else{
                                Log.w("HttpUtil.sendOperation",code.toString())
                            }
                        } else {
                            // 处理结果为 null 的情况
                            Log.w("HttpUtil.sendOperation","code is null")
                        }
                    }.exceptionally { ex ->
                        // 处理异常情况
                        Log.w("resultFuture","Exception occurred: ${ex.message}")
                        null
                    }
                },
                    modifier=Modifier.weight(1f)
                    ) {
                    Text(
                        text ="关闭",
                        fontSize = 20.sp
                        )
                }
            }else{
                ExtendedFloatingActionButton(onClick = {
                    val resultFuture = HttpUtil.sendOperation("on")

                    resultFuture.thenAccept { code ->
                        if (code != null) {
                            if (code==0){
                                viewModel.deviceIsOn()
                            }else{
                                Log.w("HttpUtil.sendOperation",code.toString())
                            }
                        } else {
                            // 处理结果为 null 的情况
                            Log.w("HttpUtil.sendOperation","code is null")
                        }
                    }.exceptionally { ex ->
                        // 处理异常情况
                        Log.w("resultFuture","Exception occurred: ${ex.message}")
                        null
                    }
                },
                    modifier=Modifier.weight(1f)
                ) {
                    Text(
                        text ="开启",
                        fontSize = 20.sp
                        //modifier=Modifier.fillMaxWidth(),
                    )
                }
            }

            //刷新按钮
//            Button(onClick = {
//                val stateFuture=HttpUtil.sendStateRequest()
//                val onlineFuture=HttpUtil.sendOnlineRequest()
//
//                //刷新状态
//                stateFuture.thenAccept {
//                    val code=it["code"]
//                    val msg=it["msg"]
//                    val time=it["time"]
//
//                    if (code!=null){
//                        //传回了正常数据
//                        if (code.toInt()==0){
//                            if (msg=="on"){
//                                viewModel.deviceIsOn()
//                            }else if (msg=="off"){
//                                viewModel.deviceIsOff()
//                            }
//
//                            if (time!=null){
//                                viewModel.setUpdateTime(time)
//                            }
//                        }else{
//                            Toast
//                                .makeText(context,"code is $code",Toast.LENGTH_SHORT)
//                                .show()
//                        }
//                    }else{
//                        Toast.makeText(context,"code is null",Toast.LENGTH_SHORT).show()
//                        // 处理结果为 null 的情况
//                    }
//                }.exceptionally { ex ->
//                    Toast
//                        .makeText(context,"Exception occurred: ${ex.message}",Toast.LENGTH_SHORT)
//                        .show()
//                    // 处理异常情况
//                    null
//                }
//
//                //刷新在线
//                onlineFuture.thenAccept {
//                    val code=it["code"]
//                    val data=it["data"]
//
//                    if (code != null) {
//                        if (code.toInt()==0){
//                            //传回了正常数据
//                            if (data.toBoolean()){
//                                viewModel.deviceIsOnline()
//                            }else{
//                                viewModel.deviceIsOffline()
//                            }
//                        }
//                    }else{
//                        Toast.makeText(context,"code is null",Toast.LENGTH_SHORT).show()
//                        // 处理结果为 null 的情况
//                    }
//                }
//            }) {
//                Text(text = "刷新")
//            }
        }

        Spacer(modifier = Modifier.height(30.dp))


        //多包裹了一层容器，以防止按键的出现和消失导致整体布局的上移和下移
        Row(
            modifier= Modifier
                .fillMaxWidth()
                .height(50.dp) //固定高度
                .padding(horizontal = 80.dp)
        ) {
            //为按键设定出场和离场动画
            AnimatedVisibility(
                visible = viewModel.isOn.value,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier= Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                    //.padding(horizontal = 80.dp)
                ) {
                    Button(onClick = {
                        val resultFuture = HttpUtil.sendOperation("bright")

                        resultFuture.thenAccept { code ->
                            if (code != null) {
                                if (code==0){
                                    viewModel.deviceIsOn()
                                }else{
                                    Log.w("HttpUtil.sendOperation",code.toString())
                                }
                            } else {
                                // 处理结果为 null 的情况
                                Log.w("HttpUtil.sendOperation","code is null")
                            }
                        }.exceptionally { ex ->
                            // 处理异常情况
                            Log.w("resultFuture","Exception occurred: ${ex.message}")
                            null
                        }
                    },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "亮",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Button(onClick = {
                        val resultFuture = HttpUtil.sendOperation("dark")

                        resultFuture.thenAccept { code ->
                            if (code != null) {
                                if (code==0){
                                    viewModel.deviceIsOn()
                                }else{
                                    Log.w("HttpUtil.sendOperation",code.toString())
                                }
                                // 在这里处理 result
                                // 例如，根据 result 做不同的操作
                            } else {
                                // 处理结果为 null 的情况
                                Log.w("HttpUtil.sendOperation","code is null")
                            }
                        }.exceptionally { ex ->
                            // 处理异常情况
                            Log.w("resultFuture","Exception occurred: ${ex.message}")
                            null
                        }
                    },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "暗",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}