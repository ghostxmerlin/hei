package com.bytetwins.hei.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import com.bytetwins.hei.R
import com.bytetwins.hei.SecondSettingsActivity
import com.bytetwins.hei.viewmodel.EyesViewModel
import com.bytetwins.hei.ModeSelectActivity
import com.bytetwins.hei.mode.HeiMode

// 眼睛与文字之间的垂直距离（dp）
private val EyeToTextGapDp = 0.dp

@Composable
fun EyesScreen(
    modifier: Modifier = Modifier,
    viewModel: EyesViewModel = viewModel(),
    currentMode: HeiMode = HeiMode.LOGIC
) {
    val uiState by viewModel.uiState.collectAsState(initial = com.bytetwins.hei.viewmodel.EyesUiState())

    // 传感器跟踪
    DisposableEffect(Unit) {
        viewModel.startTracking()
        onDispose { viewModel.stopTracking() }
    }

    val context = LocalContext.current

    // 根据当前模式选择右上角图标和颜色，与模式选择页保持一致
    val modeIcon = when (currentMode) {
        HeiMode.CREATE -> Icons.Filled.Brush
        HeiMode.EMPATHY -> Icons.Filled.Favorite
        HeiMode.EXECUTE -> Icons.Filled.Bolt
        HeiMode.LOGIC -> Icons.Filled.Hub // 近似大脑
    }
    val modeTint = when (currentMode) {
        HeiMode.CREATE -> Color(0xFFC084FC)
        HeiMode.EMPATHY -> Color(0xFFFFA94D)
        HeiMode.EXECUTE -> Color(0xFF34D399)
        HeiMode.LOGIC -> Color(0xFF60A5FA)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 中间区域：眼睛 + 文字（整体垂直居中）
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EyesCanvas(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp) // 固定一个相对合理的高度，避免眼睛区域过大导致整体下沉
            )

            Spacer(modifier = Modifier.height(EyeToTextGapDp))

            Text(
                text = stringResource(id = R.string.main_waiting_for_input),
                color = Color(0xFFB0C4DE),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 16.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }

        // 左上角设置按钮：点击后直接进入 SecondSettingsActivity
        IconButton(
            onClick = {
                val intent = Intent(context, SecondSettingsActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Color.LightGray
            )
        }

        // 右上角模式图标按钮：点击进入模式选择界面
        IconButton(
            onClick = {
                val intent = Intent(context, ModeSelectActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = modeIcon,
                contentDescription = "Mode",
                tint = modeTint
            )
        }
    }
}
