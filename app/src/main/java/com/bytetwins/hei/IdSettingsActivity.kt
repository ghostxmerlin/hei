package com.bytetwins.hei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytetwins.hei.mode.IdCardData
import com.bytetwins.hei.mode.IdCardStorage
import com.bytetwins.hei.ui.theme.HeiTheme

class IdSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeiTheme {
                Surface(color = Color.Black) {
                    IdSettingsScreen(onClose = { finish() })
                }
            }
        }
    }
}

@Composable
fun IdSettingsScreen(onClose: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val initial = remember { IdCardStorage.load(context) }

    var name by remember { mutableStateOf(initial.name) }
    var role by remember { mutableStateOf(initial.role) }
    var dataLink by remember { mutableStateOf(initial.dataLink) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020712))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部标题行，参考 SecondSettings 的风格
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .width(6.dp)
                        .background(Color(0xFF6366F1), shape = RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.id_settings_title),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.IconButton(onClick = onClose) {
                    Text(text = "X", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF111C2B), RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 顶部彩色条
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF6366F1), RoundedCornerShape(999.dp))
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(id = R.string.id_designation_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text(stringResource(id = R.string.id_classification_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dataLink,
                    onValueChange = { dataLink = it },
                    label = { Text(stringResource(id = R.string.id_data_link_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        IdCardStorage.save(
                            context,
                            IdCardData(name = name, role = role, dataLink = dataLink)
                        )
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    )
                ) {
                    Text(text = stringResource(id = R.string.id_save_button))
                }
            }
        }
    }
}
