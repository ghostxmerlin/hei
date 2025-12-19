package com.bytetwins.hei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.bytetwins.hei.mode.HeiMode
import com.bytetwins.hei.mode.ModeStorage
import com.bytetwins.hei.ui.EyesScreen
import com.bytetwins.hei.ui.FixedResolutionContainer
import com.bytetwins.hei.ui.theme.HeiTheme

class MainActivity : ComponentActivity() {

    // 用 Compose 的状态持有当前模式，这样 UI 会自动刷新
    private var currentModeState by mutableStateOf(HeiMode.LOGIC)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    // 在一个固定宽高比的逻辑画布中渲染整个主界面，周围用较亮的灰色填充
                    FixedResolutionContainer(
                        targetWidth = 1080.dp,
                        targetHeight = 1280.dp,
                        letterboxColor = Color(0xFF20232A)
                    ) {
                        // 先直接使用原来的主界面，确保 UI 正常
                        MainWithBottomBar(
                            currentMode = currentModeState,
                            onAnyInteraction = {
                                // 保留扩展点
                            }
                        )
                    }
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
            .clickable {
                // 主区域任意点击也算一次交互
                registerInteraction()
            }
    ) {
        // 原有眼睛主界面
        EyesScreen(currentMode = currentMode)

        // 底部触发区域 + 按钮栏
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp) // 整体上移一些
        ) {
            // 可点击区域：点击时弹出底部按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable {
                        registerInteraction()
                        barVisible = true
                    }
            ) { /* 这个区域可以根据需要画个箭头图标等 */ }

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
            .size(64.dp) // 变大一些
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFB0C4DE),
            modifier = Modifier.size(28.dp)
        )
    }
}
