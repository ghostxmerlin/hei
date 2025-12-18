package com.bytetwins.hei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bytetwins.hei.mode.HeiMode
import com.bytetwins.hei.mode.ModeStorage
import com.bytetwins.hei.ui.EyesScreen
import com.bytetwins.hei.ui.theme.HeiTheme

class MainActivity : ComponentActivity() {

    // 用 Compose 的状态持有当前模式，这样 UI 会自动刷新
    private var currentModeState by mutableStateOf(HeiMode.LOGIC)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 初次启动读取模式
        currentModeState = ModeStorage.loadMode(this)

        setContent {
            HeiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    EyesScreen(currentMode = currentModeState)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 回到前台时重新从文件读取模式并写回状态，触发 UI 更新
        currentModeState = ModeStorage.loadMode(this)
    }
}
