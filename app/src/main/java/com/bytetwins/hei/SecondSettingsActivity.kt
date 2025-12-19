package com.bytetwins.hei

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytetwins.hei.ui.theme.HeiTheme
import java.util.Locale

class SecondSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            HeiTheme {
                Surface(color = Color.Black) {
                    SecondSettingsScreen(onClose = { finish() })
                }
            }
        }
    }
}

private fun isDeviceOwner(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    return dpm.isDeviceOwnerApp(context.packageName)
}

// 不再在应用内清除 Device Owner，仅退出锁定模式
// Device Owner 身份一旦通过 adb 授权后，将持续存在，方便在隐藏菜单中反复进入/退出 kiosk 模式

@Composable
fun SecondSettingsScreen(onClose: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentLocale = context.resources.configuration.locales[0]
    val languageLabel = remember(currentLocale) {
        currentLocale.displayLanguage.uppercase(Locale.getDefault())
    }

    // 连续点击标题“System Settings”计数与弹窗显示状态
    var titleTapCount by remember { mutableStateOf(0) }
    var showDeviceOwnerDialog by remember { mutableStateOf(false) }

    // 4 秒内未继续点击则重置计数
    LaunchedEffect(titleTapCount, showDeviceOwnerDialog) {
        if (titleTapCount > 0 && !showDeviceOwnerDialog) {
            kotlinx.coroutines.delay(4000)
            titleTapCount = 0
        }
    }

    // 参考设计图：卡片内比背景稍微提亮、偏青灰
    val cardBackground = Color(0xFF111C2B)      // 比之前更亮，明显区别于纯黑
    val innerChipBackground = Color(0xFF1B2840) // chip 再亮一档，蓝青感更强
    val buttonBackground = Color(0xFF131F32)    // 底部按钮略深于卡片，形成层次

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020712)) // 比纯黑略亮一点，接近参考图的深蓝底
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 顶部标题行（点击标题 5 次触发隐藏 Device Owner 设置窗口）
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 竖线：绿色表示当前正在 kiosk（lockTask）模式，黄色表示已暂停
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .width(6.dp)
                        .background(
                            if (MainLockTaskController.isLockTaskActive) Color(0xFF1ED760) else Color(0xFFFACC15),
                            shape = RoundedCornerShape(50)
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(id = R.string.settings_title),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            titleTapCount += 1
                            if (titleTapCount >= 5) {
                                titleTapCount = 0
                                showDeviceOwnerDialog = true
                            }
                        }
                )

                // 右上角直接显示 X，而不是本地化的 Close 文本
                IconButton(onClick = onClose) {
                    Text(text = "X", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Language / 语言 区块：略微压缩高度，仍然比初版大
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBackground, RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF313C4F), RoundedCornerShape(18.dp))
                    .clickable {
                        context.startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                    }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.settings_language_label),
                    color = Color(0xFFE5E7EB),
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .background(innerChipBackground, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = languageLabel,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Network Config 卡片：使用同样提亮的卡片色
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBackground, RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF313C4F), RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.settings_network_title),
                    color = Color(0xFFE5E7EB),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                NetworkRowClickable(
                    label = stringResource(id = R.string.settings_network_wifi),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                )

                NetworkRowClickable(
                    label = stringResource(id = R.string.settings_network_bluetooth),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    }
                )

                NetworkRowClickable(
                    label = stringResource(id = R.string.settings_network_4g),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 底部两个按钮外层卡片：同样的提亮卡片色
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(cardBackground, RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFF313C4F), RoundedCornerShape(18.dp))
                        .padding(8.dp)
                ) {
                    SettingsLargeButton(
                        title = stringResource(id = R.string.settings_digital_wallet),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        containerColor = buttonBackground,
                        onClick = {
                            val targetPackage = "com.gemwallet.android"
                            val explicitActivity = "com.gemwallet.android.MainActivity"
                            val pm = context.packageManager

                            // 1) 首选：使用 getLaunchIntentForPackage（方法1），看系统是否还能正常拉起
                            val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try {
                                    Log.d("SecondSettings", "Opening GemWallet via launchIntent: $targetPackage")
                                    context.startActivity(launchIntent)
                                    return@SettingsLargeButton
                                } catch (e: Exception) {
                                    Log.e("SecondSettings", "Error opening GemWallet via launch intent, fallback to explicit", e)
                                }
                            } else {
                                Log.w("SecondSettings", "No launchIntent for $targetPackage, fallback to explicit activity")
                            }

                            // 2) 回退：显式 Activity（保持你之前的兼容性）
                            val explicitIntent = Intent().apply {
                                setClassName(targetPackage, explicitActivity)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                Log.d("SecondSettings", "Opening GemWallet explicitly: $targetPackage/$explicitActivity")
                                context.startActivity(explicitIntent)
                            } catch (e: android.content.ActivityNotFoundException) {
                                Log.e("SecondSettings", "Explicit GemWallet activity not found", e)
                                Toast.makeText(
                                    context,
                                    "Cannot open GemWallet: not installed or no launchable activity",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                Log.e("SecondSettings", "Error opening GemWallet explicitly", e)
                                Toast.makeText(context, "Error opening GemWallet", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(cardBackground, RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFF313C4F), RoundedCornerShape(18.dp))
                        .padding(8.dp)
                ) {
                    SettingsLargeButton(
                        title = stringResource(id = R.string.settings_id_config),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Badge,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        containerColor = buttonBackground,
                        onClick = {
                            context.startActivity(Intent(context, IdSettingsActivity::class.java))
                        }
                    )
                }
            }
        }

        // Device Owner 模式切换弹窗
        if (showDeviceOwnerDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000))
                    .clickable { showDeviceOwnerDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .background(Color(0xFF111827), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .clickable(enabled = false) { },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_do_title),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.settings_do_description),
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 进入 DO（kiosk）：假定 adb 已经设置好 Device Owner，只做 lockTask 相关操作
                    Button(
                        onClick = {
                            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                            val pkg = context.packageName
                            if (dpm.isDeviceOwnerApp(pkg)) {
                                val admin = ComponentName(context, HeiDeviceAdminReceiver::class.java)
                                // 把应用包加入 lockTask 白名单
                                dpm.setLockTaskPackages(admin, arrayOf(pkg))
                                // 通过 MainActivity 启动 lockTask，使应用“占用手机”
                                MainLockTaskController.startLockTaskIfPossible()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Not device owner. Use adb dpm set-device-owner first.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            showDeviceOwnerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = stringResource(id = R.string.settings_do_enter), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 退出 DO（kiosk）：只退出 lockTask，不清除 Device Owner
                    Button(
                        onClick = {
                            MainLockTaskController.stopLockTaskIfRunning()
                            showDeviceOwnerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4B5563),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = stringResource(id = R.string.settings_do_exit), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showDeviceOwnerDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF9CA3AF)
                        )
                    ) {
                        Text(text = stringResource(id = R.string.settings_do_close), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkRowClickable(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = ">",
            color = Color(0xFF9CA3AF),
            fontSize = 15.sp
        )
    }
}

@Composable
private fun SettingsLargeButton(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    containerColor: Color = Color(0xFF131F32),
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    leadingIcon()
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
    }
}
