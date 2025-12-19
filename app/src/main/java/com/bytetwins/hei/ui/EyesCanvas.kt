package com.bytetwins.hei.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.bytetwins.hei.viewmodel.EyesUiState
import kotlin.math.min

@Composable
fun EyesCanvas(
    uiState: EyesUiState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        drawEyes(uiState)
    }
}

private fun DrawScope.drawEyes(uiState: EyesUiState) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)

    // 基础半径放大 50%（原来 0.09f * 1.5 = 0.135f）
    val eyeRadius = min(w, h) * 0.11f

    // 将间距减小到当前的一半：5.2f -> 2.6f
    val eyeSpacing = eyeRadius * 3.3f
    val leftCenter = center.copy(x = center.x - eyeSpacing / 2f)
    val rightCenter = center.copy(x = center.x + eyeSpacing / 2f)

    val maxPupilOffset = 0f //eyeRadius * 0.4f
    val pupilOffset = Offset(
        x = uiState.pupilOffset.x * maxPupilOffset,
        y = uiState.pupilOffset.y * maxPupilOffset
    )

    if (uiState.isBlinking) {
        val lineHeight = eyeRadius * 0.15f
        drawBlinkEye(leftCenter, eyeRadius, lineHeight)
        drawBlinkEye(rightCenter, eyeRadius, lineHeight)
    } else {
        drawOpenEye(leftCenter, eyeRadius, pupilOffset)
        drawOpenEye(rightCenter, eyeRadius, pupilOffset)
    }
}

private fun DrawScope.drawOpenEye(center: Offset, radius: Float, pupilOffset: Offset) {
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center
    )

    val pupilRadius = radius * 0.42f
    val pupilCenter = center + pupilOffset

    drawCircle(
        color = Color.Black,
        radius = pupilRadius,
        center = pupilCenter
    )
}

private fun DrawScope.drawBlinkEye(center: Offset, radius: Float, lineHeight: Float) {
    val halfHeight = lineHeight / 2f
    drawRect(
        color = Color.White,
        topLeft = Offset(center.x - radius, center.y - halfHeight),
        size = androidx.compose.ui.geometry.Size(width = radius * 2f, height = lineHeight)
    )
}
