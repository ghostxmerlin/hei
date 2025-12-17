package com.bytetwins.hei

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytetwins.hei.ui.theme.HeiTheme

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF1ED760), shape = RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "SYSTEM SETTINGS",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onClose) {
                    Text(text = "X", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Language / 语言 区块：加浅色底色矩形
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1220), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF3A4A5F), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Language / 语言",
                    color = Color(0xFFCCCCCC),
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(Color(0xFF111827), RoundedCornerShape(16.dp))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF1F2937), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "CN", color = Color.White, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "EN", color = Color(0xFF9CA3AF), fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Network Config 区块：同样加浅色底色矩形
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1220), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF3A4A5F), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "NETWORK CONFIG",
                    color = Color(0xFFCCCCCC),
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                NetworkRowClickable(
                    label = "Wi-Fi Link",
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                )

                NetworkRowClickable(
                    label = "Bluetooth",
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    }
                )

                NetworkRowClickable(
                    label = "4G Data Plan",
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 底部按钮区域：按钮本身已有底色，这里只控制位置和间距
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SettingsLargeButton(
                    title = "Digital Wallet",
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                SettingsLargeButton(
                    title = "ID Config",
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Badge,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = ">",
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SettingsLargeButton(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = { /* TODO: 功能稍后实现 */ },
        modifier = modifier
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF111827),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = title,
                textAlign = TextAlign.Start,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
