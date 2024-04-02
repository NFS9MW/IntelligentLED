package com.example.intelligentled

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intelligentled.ui.theme.IntelligentLEDTheme

class SetInformationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //由于InformationViewModel是单例类，所以这里获取的viewModel实例与Compose中获取的相同
        val viewModel by lazy { ViewModelProvider(this)[InformationViewModel::class.java] }

        //取出持久化保存的数据
        val sp=MyApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE)
        val key:String?=sp.getString("key","")
        val theme:String?=sp.getString("theme","")

        //若已有存储数据，则取出并存储到ViewModel中
        if (key != null) {
            viewModel.setKey(key)
        }
        if (theme != null) {
            viewModel.setTheme(theme)
        }

        setContent {
            IntelligentLEDTheme {
                SettingActivity()
            }
        }
    }
}

@Composable
fun SettingActivity(){
    val viewModel:InformationViewModel= viewModel()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer) //背景颜色适配深色模式
    ){
        if (isSystemInDarkTheme()){
            Image(
                painter= rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.ic_dark_welcome_bg)),
                contentDescription = "welcome_bg",
                modifier = Modifier.fillMaxSize()
            )
        }else{
            Image(
                painter= rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.ic_light_welcome_bg)),
                contentDescription = "welcome_bg",
                modifier = Modifier.fillMaxSize()
            )
        }
        SettingPage(viewModel=viewModel)
    }
}

@Composable
fun SettingPage(viewModel: InformationViewModel){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) //设计图稿所规定的内边距
            //启用可滚动是为了解决横屏时部件显示不正常的问题
            .verticalScroll(rememberScrollState())
    ) {
        Title()
        Spacer(modifier = Modifier.height(30.dp))
        InformationInputBox(viewModel=viewModel)
        Spacer(modifier = Modifier.height(50.dp))
        ConfirmButton(viewModel=viewModel)
    }
}

@Composable
fun Title(){
    Text(
        text = "修改私钥和主题",
        fontSize = 24.sp,
        modifier = Modifier
            .fillMaxWidth()
            .paddingFromBaseline(top = 184.dp, bottom = 16.dp),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )
}


@Composable
fun InformationInputBox(viewModel: InformationViewModel){
    Column {
        KeyTextField(label = "输入私钥",viewModel = viewModel)
        Spacer(modifier = Modifier.height(8.dp))
        ThemeTextFiled(label = "输入主题", viewModel = viewModel)
    }
}

//私钥输入框
@Composable
fun KeyTextField(label:String,viewModel: InformationViewModel){
    OutlinedTextField(
        value = viewModel.key.value,
        onValueChange = {
            viewModel.setKey(it)
        },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
        label = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = MaterialTheme.colorScheme.background,
            focusedBorderColor = MaterialTheme.colorScheme.onBackground,//被选中时的下边框颜色
        )
    )
}

//主题输入框
@Composable
fun ThemeTextFiled(label:String,viewModel: InformationViewModel){
    OutlinedTextField(
        value = viewModel.theme.value,
        onValueChange = {
            viewModel.setTheme(it)
        },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
        label = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = MaterialTheme.colorScheme.background,
            focusedBorderColor = MaterialTheme.colorScheme.onBackground,//被选中时的下边框颜色
        )
    )
}

//确认按键
@Composable
fun ConfirmButton(viewModel: InformationViewModel){
    ExtendedFloatingActionButton(
        onClick = {
            //使用SharedPreference来持久化存储EditText中的内容
            val sp=MyApplication.context.getSharedPreferences("data", Context.MODE_PRIVATE).edit()

            sp.putString("key",viewModel.key.value)
            sp.putString("theme",viewModel.theme.value)
            sp.apply() //注意最后需要调用apply()方法进行提交

            Toast.makeText(MyApplication.context,"保存完毕",Toast.LENGTH_LONG).show()
        },
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "按下保存",
        )
    }
}