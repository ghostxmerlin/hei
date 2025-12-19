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
    val center = Offset(w / 2f, 50f + h / 2f)

    // 基础半径：控制整体眼睛大小
    val baseEyeRadius = min(w, h) * 0.13f

    // 眼睛间距：系数越大，两只眼距离越远
    val eyeSpacing = baseEyeRadius * 3.1f
    val baseLeftCenter = center.copy(x = center.x - eyeSpacing / 2f)
    val baseRightCenter = center.copy(x = center.x + eyeSpacing / 2f)

    // --- 眨眼进度：0 = 正常睁眼，1 = 完全眨眼 ---
    // 目前 EyesUiState 只有布尔 isBlinking，我们先用一个简单的数值近似：
    // 眨眼时 blinkProgress = 1f，否则为 0f。后续如果你在 ViewModel 里加上
    // 连续的 blinkProgress，就可以直接替换这里的实现。
    val blinkProgress = if (uiState.isBlinking) 1f else 0f

    // 向左整体平移：30 像素 * blinkProgress，实现从 0 -> 30 的连续过渡
    val maxBlinkShiftX = -30f
    val blinkShift = Offset(maxBlinkShiftX * blinkProgress, 0f)

    // 半径缩小 5%：从 1.0 -> 0.95 的线性插值
    val blinkScale = 1f - 0.05f * blinkProgress
    val eyeRadius = baseEyeRadius * blinkScale

    val leftCenter = baseLeftCenter + blinkShift
    val rightCenter = baseRightCenter + blinkShift

    if (blinkProgress >= 1f) {
        // 完全眨眼：使用细线表示
        val lineHeight = eyeRadius * 0.15f
        drawBlinkEye(leftCenter, eyeRadius, lineHeight)
        drawBlinkEye(rightCenter, eyeRadius, lineHeight)
    } else {
        // 非完全眨眼：按正常睁眼逻辑绘制（可根据需要在将来加半眨眼形态）
        val maxPupilOffset = 0f // baseEyeRadius * 0.4f
        val pupilOffset = Offset(
            x = uiState.pupilOffset.x * maxPupilOffset,
            y = uiState.pupilOffset.y * maxPupilOffset
        )

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
