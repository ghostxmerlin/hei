package com.bytetwins.hei.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bytetwins.hei.R
import com.bytetwins.hei.SecondSettingsActivity
import com.bytetwins.hei.viewmodel.EyesViewModel
import com.bytetwins.hei.ModeSelectActivity
import com.bytetwins.hei.mode.HeiMode
import com.bytetwins.hei.mode.IdCardStorage
import com.bytetwins.hei.mode.IdCardData
import com.journeyapps.barcodescanner.BarcodeEncoder
import androidx.compose.ui.text.font.FontWeight

// 眼睛与文字之间的垂直距离（dp）
private val EyeToTextGapDp = 0.dp

@Composable
fun EyesScreen(
    modifier: Modifier = Modifier,
    viewModel: EyesViewModel = viewModel(),
    currentMode: HeiMode = HeiMode.LOGIC
) {
    val uiState by viewModel.uiState.collectAsState(initial = com.bytetwins.hei.viewmodel.EyesUiState())

    // 模式对应的右上角图标和颜色
    val modeIcon = when (currentMode) {
        HeiMode.CREATE -> Icons.Filled.Brush
        HeiMode.EMPATHY -> Icons.Filled.Favorite
        HeiMode.EXECUTE -> Icons.Filled.Bolt
        HeiMode.LOGIC -> Icons.Filled.Hub
    }
    val modeTint = when (currentMode) {
        HeiMode.CREATE -> Color(0xFFC084FC)
        HeiMode.EMPATHY -> Color(0xFFFFA94D)
        HeiMode.EXECUTE -> Color(0xFF34D399)
        HeiMode.LOGIC -> Color(0xFF60A5FA)
    }

    // 传感器跟踪
    DisposableEffect(Unit) {
        viewModel.startTracking()
        onDispose { viewModel.stopTracking() }
    }

    val context = LocalContext.current
    var showIdCard by remember { mutableStateOf(false) }
    var showBack by remember { mutableStateOf(false) }
    var idData by remember { mutableStateOf(IdCardStorage.load(context)) }

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
            // 黑色可点击区域：在眼睛上方或包裹眼睛区域，用于唤出 ID 卡
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable {
                        idData = IdCardStorage.load(context)
                        showIdCard = true
                        showBack = false
                    }
            ) {}

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

        if (showIdCard) {
            IdCardOverlay(
                data = idData,
                showBack = showBack,
                onToggleSide = { showBack = !showBack },
                onDismiss = { showIdCard = false }
            )
        }
    }
}

@Composable
private fun IdCardOverlay(
    data: IdCardData,
    showBack: Boolean,
    onToggleSide: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(260.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF111827))
                .clickable {
                    onToggleSide()
                }
        ) {
            if (!showBack) {
                IdCardFront(data)
            } else {
                IdCardBack(data)
            }
        }
    }
}

@Composable
private fun IdCardFront(data: IdCardData) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF6366F1))
        ) {}

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = data.name.ifBlank { stringResource(id = R.string.id_force_label) },
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.role,
                color = Color(0xFF818CF8),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.id_card_tap_to_flip),
                color = Color(0xFF6B7280),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun IdCardBack(data: IdCardData) {
    val qrBitmap = remember(data.dataLink) {
        try {
            if (data.dataLink.isNotBlank()) {
                BarcodeEncoder().encodeBitmap(
                    data.dataLink,
                    com.google.zxing.BarcodeFormat.QR_CODE,
                    256,
                    256
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.id_data_link_title),
            color = Color(0xFF10B981),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.dataLink,
            color = Color(0xFF9CA3AF),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (qrBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR",
                modifier = Modifier.size(120.dp)
            )
        }
    }
}
