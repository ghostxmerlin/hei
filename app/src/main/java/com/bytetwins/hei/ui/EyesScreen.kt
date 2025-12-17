package com.bytetwins.hei.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.bytetwins.hei.SecondSettingsActivity
import com.bytetwins.hei.viewmodel.EyesViewModel

// 眼睛与文字之间的垂直距离（dp）
private val EyeToTextGapDp = 0.dp

@Composable
fun EyesScreen(
    modifier: Modifier = Modifier,
    viewModel: EyesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState(initial = com.bytetwins.hei.viewmodel.EyesUiState())

    // 传感器跟踪
    DisposableEffect(Unit) {
        viewModel.startTracking()
        onDispose { viewModel.stopTracking() }
    }

    val context = LocalContext.current

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
                text = "WAITING FOR INPUT",
                color = Color(0xFFB0C4DE),
                style = MaterialTheme.typography.labelMedium,
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

        // 底部按钮：单独贴底，不影响中间区域
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            color = Color.Transparent
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, SecondSettingsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .height(9.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF666666),
                    contentColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                // 无文字
            }
        }
    }
}
