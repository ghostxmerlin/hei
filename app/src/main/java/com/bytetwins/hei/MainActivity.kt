package com.bytetwins.hei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.bytetwins.hei.mode.HeiMode
import com.bytetwins.hei.mode.ModeStorage
import com.bytetwins.hei.ui.EyesScreen
import com.bytetwins.hei.ui.theme.HeiTheme

class MainActivity : ComponentActivity() {

    // 用 Compose 的状态持有当前模式，这样 UI 会自动刷新
    private var currentModeState by mutableStateOf(HeiMode.LOGIC)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用真正的全屏：隐藏状态栏，让内容绘制到系统栏下面
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            // 允许用户从顶部短暂滑动呼出系统栏，再自动隐藏
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        enableEdgeToEdge()

        // 从存储读取模式 id，再映射到 HeiMode
        val storedId = ModeStorage.loadModeId(this)
        currentModeState = HeiMode.fromId(storedId)

        setContent {
            HeiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    MainWithBottomBar(
                        currentMode = currentModeState,
                        onAnyInteraction = {
                            // 这里目前不需要额外逻辑，但保留扩展点
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 回到前台时重新从文件读取模式 id 并映射
        val storedId = ModeStorage.loadModeId(this)
        currentModeState = HeiMode.fromId(storedId)
    }
}

@Composable
private fun MainWithBottomBar(
    currentMode: HeiMode,
    onAnyInteraction: () -> Unit
) {
    var barVisible by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // 自动隐藏效果：监听时间差，每次重组时检查
    LaunchedEffect(barVisible, lastInteractionTime) {
        if (barVisible) {
            val startTime = lastInteractionTime
            // 等待 2 秒
            kotlinx.coroutines.delay(2000L)
            // 若这 2 秒内没有新的交互，则隐藏
            if (barVisible && lastInteractionTime == startTime) {
                barVisible = false
            }
        }
    }

    fun registerInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        onAnyInteraction()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { registerInteraction() }
    ) {
        // 原有眼睛主界面
        EyesScreen(currentMode = currentMode)

        // 底部触发区域 + 按钮栏：整体位置上移 50 像素
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp) // 向上抬高 50 像素
        ) {
            // 触发区：点击或从下向上轻扫都可以唤出底部栏
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -10f) { // 向上拖动
                                registerInteraction()
                                barVisible = true
                            }
                        }
                    }
                    .clickable {
                        registerInteraction()
                        barVisible = true
                    }
            ) { /* 底部手势触发区 */ }

            if (barVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBottomButton(icon = Icons.Filled.Home) { registerInteraction() }
                    IconBottomButton(icon = Icons.Filled.RadioButtonChecked) { registerInteraction() }
                    IconBottomButton(icon = Icons.Filled.Brush) { registerInteraction() }
                }
            }
        }
    }
}

@Composable
private fun IconBottomButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp) // 原 64.dp，放大约 25~30%
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFB0C4DE),
            modifier = Modifier.size(32.dp) // 原 28.dp，略微变大以匹配圆形
        )
    }
}
