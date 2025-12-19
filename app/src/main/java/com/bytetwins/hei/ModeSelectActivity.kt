package com.bytetwins.hei

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytetwins.hei.mode.HeiMode
import com.bytetwins.hei.mode.ModeStorage
import com.bytetwins.hei.ui.theme.HeiTheme
import kotlinx.coroutines.delay

class ModeSelectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 从存储读取模式 id 并映射到 HeiMode
        val initialMode = HeiMode.fromId(ModeStorage.loadModeId(this))

        setContent {
            HeiTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    ModeSelectScreen(
                        initialMode = initialMode,
                        onModeChanged = { mode ->
                            // 每次切换时立即写入文件，供其他应用和 Main 使用
                            ModeStorage.saveModeId(this, mode.id)
                        },
                        onFinished = { finalMode ->
                            // 返回给 MainActivity 当前模式
                            val data = Intent().putExtra("hei_mode", finalMode.id)
                            setResult(Activity.RESULT_OK, data)
                            finish()
                        },
                        onTimeout = {
                            // 超时时直接结束即可，Main 会在 onResume 中重新读取文件
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModeSelectScreen(
    initialMode: HeiMode,
    onModeChanged: (HeiMode) -> Unit,
    onFinished: (HeiMode) -> Unit,
    onTimeout: () -> Unit
) {
    var currentMode by remember { mutableStateOf(initialMode) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // 1 秒无操作自动退出
    LaunchedEffect(currentMode, lastInteractionTime) {
        val start = lastInteractionTime
        delay(2000) // 原 1000ms，现在延长为 2000ms（2 秒）
        if (start == lastInteractionTime) {
            onTimeout()
        }
    }

    val accent = modeAccentColor(currentMode)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                lastInteractionTime = System.currentTimeMillis()
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部大图标区域：点击切换到下一模式
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clickable {
                        val next = nextMode(currentMode)
                        currentMode = next
                        lastInteractionTime = System.currentTimeMillis()
                        onModeChanged(next)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (currentMode) {
                        HeiMode.CREATE -> Icons.Filled.Brush
                        HeiMode.EMPATHY -> Icons.Filled.Favorite
                        HeiMode.EXECUTE -> Icons.Filled.Bolt
                        HeiMode.LOGIC -> Icons.Filled.Hub // 近似大脑，可后续替换为自定义图标
                    },
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (currentMode) {
                    HeiMode.CREATE -> "CREATE"
                    HeiMode.EMPATHY -> "EMPATHY"
                    HeiMode.EXECUTE -> "EXECUTE"
                    HeiMode.LOGIC -> "LOGIC"
                },
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (currentMode) {
                    HeiMode.CREATE -> "DIVERGENT THINKING."
                    HeiMode.EMPATHY -> "EMOTIONAL RESONANCE."
                    HeiMode.EXECUTE -> "RESULT ORIENTED ACTION."
                    HeiMode.LOGIC -> "PURE ANALYTICAL REASONING."
                },
                color = Color(0xFFB0C4DE),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 底部三枚圆形小图标行
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左：对应 create/功能入口，使用刷子图标
                ModeCircleIcon(
                    icon = Icons.Filled.Brush,
                    active = false,
                    accent = accent
                )
                // 中：当前模式/设置，使用齿轮图标，高亮
                ModeCircleIcon(
                    icon = Icons.Filled.Settings,
                    active = true,
                    accent = accent
                )
                // 右：连接/逻辑，使用 Hub 图标
                ModeCircleIcon(
                    icon = Icons.Filled.Hub,
                    active = false,
                    accent = accent
                )
            }
        }

        // 底部长条不再显示
    }
}

@Composable
private fun ModeCircleIcon(icon: ImageVector, active: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (active) accent.copy(alpha = 0.12f) else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = if (active) accent else Color(0xFF4B5563),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) accent else Color(0xFF9CA3AF),
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun nextMode(current: HeiMode): HeiMode = when (current) {
    HeiMode.CREATE -> HeiMode.EMPATHY
    HeiMode.EMPATHY -> HeiMode.EXECUTE
    HeiMode.EXECUTE -> HeiMode.LOGIC
    HeiMode.LOGIC -> HeiMode.CREATE
}

@Composable
private fun modeAccentColor(mode: HeiMode): Color = when (mode) {
    HeiMode.CREATE -> Color(0xFFC084FC) // 紫色
    HeiMode.EMPATHY -> Color(0xFFFFA94D) // 橙色
    HeiMode.EXECUTE -> Color(0xFF34D399) // 绿色
    HeiMode.LOGIC -> Color(0xFF60A5FA) // 蓝色
}
