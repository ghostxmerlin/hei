package com.bytetwins.hei.ui

import android.content.Intent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.math.abs

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
            // 放大后的隐藏按钮：高度 72dp，宽度为中间区域的 60%
            Box(
                modifier = Modifier
                    .height(72.dp)
                    .fillMaxWidth(0.6f)
                    .padding(bottom = 8.dp)
                    .clickable {
                        idData = IdCardStorage.load(context)
                        showIdCard = true
                        showBack = false
                    }
            ) { /* hidden ID card trigger */ }

            // 眼睛绘制区域
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
    val rotation by animateFloatAsState(
        targetValue = if (showBack) 180f else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        ),
        label = "idCardFlipRotation"
    )
    // 在可组合环境中读取 density 数值，传给 graphicsLayer 使用
    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF0000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(280.dp)
                .height(420.dp)
                .border(2.dp, Color(0xFF020617), RoundedCornerShape(32.dp))
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12 * density
                }
                .clickable { onToggleSide() },
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFF000000),
            shadowElevation = 32.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 20.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF18263B))
            ) {
                // 根据旋转角度选择正反面，避免动画中间闪烁
                val isBackVisible = rotation >= 90f
                if (isBackVisible) {
                    Box(Modifier.graphicsLayer { rotationY = 180f }) {
                        IdCardBack(data)
                    }
                } else {
                    IdCardFront(data)
                }
            }
        }
    }
}

@Composable
private fun IdCardFront(data: IdCardData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部紫色 header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFF6366F1)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.id_card_title),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 彩色碎块头像：将头像区域划分为 4x4 小块，每块从调色板中选一个颜色
                val baseSeed = (data.name + "|" + data.role).hashCode()
                val palette = listOf(
                    Color(0xFFF97373), // 红
                    Color(0xFF4ADE80), // 绿
                    Color(0xFF60A5FA), // 蓝
                    Color(0xFFFBBF24), // 黄
                    Color(0xFFEC4899), // 粉
                    Color(0xFFA855F7)  // 紫
                )
                val blockColors = remember(data.name, data.role) {
                    // 4x4 共 16 个小块
                    List(16) { index ->
                        val seed = baseSeed + index * 31
                        val colorIndex = abs(seed) % palette.size
                        palette[colorIndex]
                    }
                }

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF111827))
                ) {
                    val rows = 4
                    val cols = 4
                    val blockSize = 88.dp / rows

                    Column(Modifier.fillMaxSize()) {
                        repeat(rows) { r ->
                            Row(Modifier.fillMaxWidth()) {
                                repeat(cols) { c ->
                                    val color = blockColors[r * cols + c]
                                    Box(
                                        modifier = Modifier
                                            .size(blockSize)
                                            .background(color)
                                    ) {}
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = data.name.ifBlank { stringResource(id = R.string.id_force_label) },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = data.role,
                    color = Color(0xFF8B9BFF),
                    fontSize = 14.sp
                )
            }

            Text(
                text = stringResource(id = R.string.id_card_tap_to_flip),
                color = Color(0xFF9CA3AF),
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 只显示“联系方式/CONTACT”标题
        Text(
            text = stringResource(id = R.string.id_contact_title),
            color = Color(0xFF60A5FA),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 中间只保留二维码
        if (qrBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            )
        }

        // 底部留一些空白以保持视觉平衡
        Spacer(modifier = Modifier.height(8.dp))
    }
}
