package com.bytetwins.hei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 在任意屏幕上提供一个“固定宽高比”的内容区域，并在周围填充灰色。
 *
 * - targetWidth/targetHeight 用来定义宽高比（例如 1080x1280）
 * - 实际上用一个固定的 dp 宽度渲染内容，高度按比例计算
 * - 在更大屏幕上会看到上下/左右的 letterbox 灰边
 */
@Composable
fun FixedResolutionContainer(
    modifier: Modifier = Modifier,
    targetWidth: Dp = 1080.dp,
    targetHeight: Dp = 1280.dp,
    // 提高灰色亮度，让与中间黑色内容的对比更明显
    letterboxColor: Color = Color(0xFF20232A),
    content: @Composable BoxScope.() -> Unit
) {
    // 选择一个在大部分手机上合适的逻辑宽度（dp）
    // 例如很多 1080px 宽的设备密度接近 3，对应逻辑宽度约 360dp
    val logicalWidth = 360.dp

    // 按 targetWidth:targetHeight 的比例计算逻辑高度
    val aspectRatio = targetHeight.value / targetWidth.value // 1280 / 1080
    val logicalHeight = logicalWidth * aspectRatio

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(letterboxColor),
        contentAlignment = Alignment.Center
    ) {
        // 中间这个 Box 就是固定“画布”区域，外面都是灰边
        Box(
            modifier = Modifier
                .width(logicalWidth)
                .height(logicalHeight),
            content = content
        )
    }
}
