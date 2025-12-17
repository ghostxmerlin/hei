package com.bytetwins.hei.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

/**
 * Holds state for the animated eyes, combining sensor-driven pupil offset and blinking.
 */
class EyesViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gravitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val mutableUiState = MutableStateFlow(EyesUiState())
    val uiState: StateFlow<EyesUiState> = mutableUiState.asStateFlow()

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            mutableUiState.update { current ->
                current.copy(pupilOffset = event.toNormalizedOffset())
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    init {
        startBlinkLoop()
    }

    fun startTracking() {
        gravitySensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(sensorListener)
    }

    fun recenterEyes() {
        mutableUiState.update { it.copy(pupilOffset = Offset.Zero) }
    }

    fun blinkNow() {
        viewModelScope.launch {
            triggerBlink(durationMillis = 150L)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }

    private fun startBlinkLoop() {
        viewModelScope.launch {
            val random = Random(System.currentTimeMillis())
            while (isActive) {
                val waitDuration = random.nextLong(MIN_BLINK_INTERVAL_MS, MAX_BLINK_INTERVAL_MS)
                delay(waitDuration)
                triggerBlink()
            }
        }
    }

    private suspend fun triggerBlink(durationMillis: Long = DEFAULT_BLINK_DURATION_MS) {
        mutableUiState.update { it.copy(isBlinking = true) }
        delay(durationMillis)
        mutableUiState.update { it.copy(isBlinking = false) }
    }

    private fun SensorEvent.toNormalizedOffset(): Offset {
        val rawX = -values.getOrNull(0).orZero()
        val rawY = values.getOrNull(1).orZero()
        val normalizedX = normalizeForGravity(rawX)
        val normalizedY = normalizeForGravity(rawY)
        return Offset(normalizedX, normalizedY)
    }

    private fun Float?.orZero(): Float = this ?: 0f

    private fun normalizeForGravity(value: Float): Float {
        val normalized = value / SensorManager.GRAVITY_EARTH
        return normalized.coerceIn(-1f, 1f)
    }

    companion object {
        private const val MIN_BLINK_INTERVAL_MS = 2_800L
        private const val MAX_BLINK_INTERVAL_MS = 5_800L
        private const val DEFAULT_BLINK_DURATION_MS = 120L
    }
}

data class EyesUiState(
    val pupilOffset: Offset = Offset.Zero,
    val isBlinking: Boolean = false
)

