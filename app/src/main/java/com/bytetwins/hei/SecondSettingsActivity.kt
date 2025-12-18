package com.bytetwins.hei

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
        setContent {
            HeiTheme {
                Surface(color = Color.Black) {
                    SecondSettingsScreen(onClose = { finish() })
                }
            }
        }
    }
}

@Composable
fun SecondSettingsScreen(onClose: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentLocale = context.resources.configuration.locales[0]
    val languageLabel = remember(currentLocale) {
        // 简单用语言代码展示，例如 "EN", "ZH"，也可以根据需要扩展
        currentLocale.displayLanguage.uppercase(Locale.getDefault())
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
            // 顶部标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 绿色状态圆形：高度与标题行接近，看起来更粗更明显
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .width(6.dp)
                        .background(Color(0xFF1ED760), shape = RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(id = R.string.settings_title),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

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
                            val targetActivity = "com.gemwallet.android.MainActivity"
                            Log.d("SecondSettings", "Trying to open GemWallet explicitly: $targetPackage/$targetActivity")

                            val explicitIntent = Intent().apply {
                                setClassName(targetPackage, targetActivity)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }

                            try {
                                context.startActivity(explicitIntent)
                                Toast.makeText(context, "Opening GemWallet...", Toast.LENGTH_SHORT).show()
                            } catch (e: android.content.ActivityNotFoundException) {
                                Log.e("SecondSettings", "Explicit GemWallet activity not found, trying launch intent", e)
                                // fallback: use launch intent resolved by PackageManager
                                val pm = context.packageManager
                                val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
                                if (launchIntent != null) {
                                    try {
                                        context.startActivity(launchIntent)
                                        Toast.makeText(context, "Opening GemWallet (launcher)...", Toast.LENGTH_SHORT).show()
                                    } catch (e2: Exception) {
                                        Log.e("SecondSettings", "Error opening GemWallet via launch intent", e2)
                                        Toast.makeText(context, "Error opening GemWallet", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Log.e("SecondSettings", "No launch intent found for GemWallet")
                                    Toast.makeText(
                                        context,
                                        "Cannot open GemWallet: no launchable activity",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
                        containerColor = buttonBackground
                    )
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
