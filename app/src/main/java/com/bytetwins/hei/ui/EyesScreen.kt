package com.bytetwins.hei.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.telephony.TelephonyManager
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellular0Bar
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
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
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.delay

// 眼睛与文字之间的垂直距离（dp）
private val EyeToTextGapDp = 32.dp // 原 12.dp，略微加大，让整体重心稍微下移

// 状态栏可见时长（毫秒）：显示 2 秒
private const val STATUS_BAR_VISIBLE_DURATION_MS = 4000L

// 简单的配置对象：通过这个开关控制是否显示顶部网络/蓝牙状态图标
object TopStatusConfig {
    // 设为 false 则完全关闭顶部状态显示（不读系统状态、不画图标）
    const val ENABLED: Boolean = true
}

@Composable
fun EyesScreen(
    modifier: Modifier = Modifier,
    viewModel: EyesViewModel = viewModel(),
    currentMode: HeiMode = HeiMode.LOGIC
) {
    val uiState by viewModel.uiState.collectAsState(initial = com.bytetwins.hei.viewmodel.EyesUiState())

    val context = LocalContext.current

    // --- 电量：通过 ACTION_BATTERY_CHANGED 广播读取实际电量 ---
    var batteryLevelPercent by remember { mutableStateOf(100) }
    LaunchedEffect(Unit) {
        // ACTION_BATTERY_CHANGED 是粘性广播，可以用 registerReceiver(null, filter) 直接读取当前值
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val statusIntent = context.registerReceiver(null, filter)
        if (statusIntent != null) {
            val level = statusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = statusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                batteryLevelPercent = ((level * 100f) / scale).toInt().coerceIn(0, 100)
            }
        }
    }

    // 只有在配置开启时才去读取系统网络/蓝牙状态
    val connectivityManager = remember {
        if (TopStatusConfig.ENABLED)
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        else null
    }
    val telephonyManager = remember {
        if (TopStatusConfig.ENABLED)
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        else null
    }
    val bluetoothAdapter = remember {
        if (TopStatusConfig.ENABLED) {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            manager.adapter
        } else null
    }

    // 每次重组时直接同步读取当前网络与蓝牙状态（对我们这个简单 UI 足够）
    val activeNetwork = connectivityManager?.activeNetwork
    val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)

    val hasWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    val hasCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    val hasSim = telephonyManager?.simState == TelephonyManager.SIM_STATE_READY

    val isBluetoothConnected = remember(bluetoothAdapter) {
        try {
            val btOn = bluetoothAdapter?.isEnabled == true
            btOn && bluetoothAdapter?.bondedDevices?.isNotEmpty() == true
        } catch (_: SecurityException) {
            false
        }
    }

    val showCellularBars = hasCellular && hasSim
    val networkTypeLabel = remember(hasCellular, hasWifi) {
        if (!TopStatusConfig.ENABLED || !hasCellular || hasWifi) "" else "3G"
    }

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

    var showIdCard by remember { mutableStateOf(false) }
    var showBack by remember { mutableStateOf(false) }
    var idData by remember { mutableStateOf(IdCardStorage.load(context)) }

    // 顶部状态栏显隐：默认隐藏，只有在滑动手势触发后短暂显示
    var showStatusBar by remember { mutableStateOf(false) }

    // 如果状态栏当前可见，2 秒后自动隐藏；依赖 LaunchedEffect 的取消机制，
    // 当用户在这 2 秒内再次触发显示时，会重置倒计时而不是叠加多个任务
    LaunchedEffect(showStatusBar) {
        if (showStatusBar) {
            delay(STATUS_BAR_VISIBLE_DURATION_MS)
            showStatusBar = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 左上角设置按钮：始终可见，不跟随状态栏显隐
        IconButton(
            onClick = {
                val intent = Intent(context, SecondSettingsActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Color.LightGray
            )
        }

        // 顶部手势触发区：从屏幕上边缘向下滑时显示状态栏（前提是配置开启），
        // 为了不挡住左上角设置按钮，这里在左侧预留一段不接收手势的区域
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(48.dp)
                .padding(start = 72.dp) // 预留左上角按钮空间，避免拦截其点击
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (TopStatusConfig.ENABLED && dragAmount > 10f) {
                            showStatusBar = true
                        }
                    }
                }
        ) { /* 顶部滑动触发区 */ }

        // 使用上下权重精细控制：上半部分略大一些，让眼睛位置比刚才稍低一点
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 上部区域：眼睛区域改为触发 ID 卡
            Column(
                modifier = Modifier
                    .weight(1.0f) // 原 1.05f，略微减小上半部分权重
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(128.dp)) // 原 12.dp，增加上方留白让眼睛整体下移

                // 眼睛绘制区域：点击时弹出 ID 卡
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            idData = IdCardStorage.load(context)
                            showIdCard = true
                            showBack = false
                        },
                    horizontalArrangement = Arrangement.Center
                ) {
                    EyesCanvas(
                        uiState = uiState,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
            }

            // 下部区域：文字 + 空白
            Column(
                modifier = Modifier
                    .weight(1.0f) // 原 0.95f，与上半相等，让眼睛下沿更接近垂直中线
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(EyeToTextGapDp))

                Text(
                    text = stringResource(id = R.string.main_waiting_for_input),
                    color = Color(0xFFB0C4DE),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        }

        if (TopStatusConfig.ENABLED && showStatusBar) {
            // 顶部中间状态图标 + 右侧模式按钮：仅在状态栏显示期间可见
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp)) // 占位，避免覆盖左侧固定设置按钮

                // 中间状态图标区域
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1) Wi‑Fi 优先：连上 Wi‑Fi 时显示亮色 Wi‑Fi 图标
                    if (hasWifi) {
                        Icon(
                            imageVector = Icons.Filled.Wifi,
                            contentDescription = "Wi‑Fi connected",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        // 2) 未连接 Wi‑Fi：显示蜂窝状态
                        when {
                            !hasSim -> {
                                // 没有 SIM：显示无信号/禁用图标
                                Icon(
                                    imageVector = Icons.Filled.SignalCellularOff,
                                    contentDescription = "No SIM",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            showCellularBars -> {
                                Icon(
                                    imageVector = Icons.Filled.SignalCellular4Bar,
                                    contentDescription = "Cellular connected",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            else -> {
                                Icon(
                                    imageVector = Icons.Filled.SignalCellular0Bar,
                                    contentDescription = "Cellular idle",
                                    tint = Color(0xFF4B5563),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (networkTypeLabel.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = networkTypeLabel,
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // 3) 蓝牙图标：连接耳机/设备时高亮，否则灰色
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = if (isBluetoothConnected) Icons.Filled.Bluetooth else Icons.Filled.BluetoothDisabled,
                        contentDescription = "Bluetooth",
                        tint = if (isBluetoothConnected) Color(0xFF60A5FA) else Color(0xFF4B5563),
                        modifier = Modifier.size(18.dp)
                    )

                    // 电池电量图标 + 文本
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Filled.BatteryFull,
                        contentDescription = "Battery",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$batteryLevelPercent%",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }

                // 右上角模式按钮
                IconButton(
                    onClick = {
                        val intent = Intent(context, ModeSelectActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = modeIcon,
                        contentDescription = "Mode",
                        tint = modeTint
                    )
                }
            }
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
