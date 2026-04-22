package com.sendaurjc.ui.screen

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.content.Intent
import android.content.BroadcastReceiver
import android.app.NotificationManager
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sendaurjc.ui.viewmodel.MainViewModel
import com.sendaurjc.service.AlertForegroundService
import kotlinx.coroutines.delay
import android.content.IntentFilter
import androidx.core.content.ContextCompat

@Composable
fun PreAlertScreen(viewModel: MainViewModel, onDeactivate: () -> Unit) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current
    var timeLeft by remember { mutableIntStateOf(30) }
    
    fun t(key: String): String = viewModel.getTranslation(key)

    val backgroundColor = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val buttonColor = if (isDarkMode) Color.White else Color.Black
    val buttonTextColor = if (isDarkMode) Color.Black else Color.White

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // Iniciar servicio y mostrar notificación
    LaunchedEffect(Unit) {
        val intent = Intent(context, AlertForegroundService::class.java).apply {
            action = AlertForegroundService.ACTION_START_PREALERT
        }
        ContextCompat.startForegroundService(context, intent)
    }

    // Escuchar si la notificación desactiva la prealerta
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AlertForegroundService.ACTION_PREALERT_DEACTIVATED) {
                    onDeactivate()
                }
            }
        }
        val filter = IntentFilter(AlertForegroundService.ACTION_PREALERT_DEACTIVATED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(Unit) {
        val pattern = longArrayOf(0, 500, 200)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            vibrator.cancel()
        }
    }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else {
            // Activar la alerta real si el contador llega a 0
            val intent = Intent(context, AlertForegroundService::class.java).apply {
                action = AlertForegroundService.ACTION_EMERGENCY
            }
            context.startService(intent)
            viewModel.setAlertModeActive(true)
            onDeactivate()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = t("pre_alert_activated"),
            color = textColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = t("pre_alert_deactivate_msg"),
            color = textColor,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = timeLeft.toString(),
            color = Color.Red,
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(64.dp))
        Button(
            onClick = {
                vibrator.cancel()
                val intent = Intent(context, AlertForegroundService::class.java).apply {
                    action = AlertForegroundService.ACTION_EMERGENCY
                }
                context.startService(intent)
                viewModel.setAlertModeActive(true)
                onDeactivate()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(t("emergency"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                vibrator.cancel()
                val intent = Intent(context, AlertForegroundService::class.java).apply {
                    action = AlertForegroundService.ACTION_OK
                }
                context.startService(intent)
                onDeactivate()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = buttonTextColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(t("deactivate_pre_alert"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
