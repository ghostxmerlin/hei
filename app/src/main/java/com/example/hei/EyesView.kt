package com.example.hei

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class EyesView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 半径、间距（直接用 dp 转 px，避免 R 引用错误）
    private var eyeRadius: Float = 0f
    private var pupilRadius: Float = 0f
    private var eyeSpacing: Float = 0f

    private var leftEyeCenterX: Float = 0f
    private var rightEyeCenterX: Float = 0f
    private var eyesCenterY: Float = 0f

    // 瞳孔中心位置，初始等于各自眼睛的中心
    private var leftPupilX: Float = 0f
    private var leftPupilY: Float = 0f
    private var rightPupilX: Float = 0f
    private var rightPupilY: Float = 0f

    // 简单的画笔
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    init {
        // 再小一号的眼睛&瞳孔，保持适中间距
        val density = resources.displayMetrics.density
        eyeRadius = 22f * density      // 从 28dp 再缩小到 22dp
        pupilRadius = 9f * density     // 按比例缩小
        // 原先约 88dp，这里按你要求缩小到 40% 左右 => ~35dp
        eyeSpacing = 35f * density     // 间距略缩小，让眼睛更聚拢
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (eyeSpacing + 4f * eyeRadius).toInt()
        val desiredHeight = (4f * eyeRadius).toInt()

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val centerX = w / 2f
        // 整体稍微往上移一些，看起来偏上半部分
        eyesCenterY = h / 2f - eyeRadius

        leftEyeCenterX = centerX - eyeSpacing / 2f
        rightEyeCenterX = centerX + eyeSpacing / 2f

        leftPupilX = leftEyeCenterX
        rightPupilX = rightEyeCenterX
        leftPupilY = eyesCenterY
        rightPupilY = eyesCenterY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(leftEyeCenterX, eyesCenterY, eyeRadius, eyePaint)
        canvas.drawCircle(rightEyeCenterX, eyesCenterY, eyeRadius, eyePaint)

        canvas.drawCircle(leftPupilX, leftPupilY, pupilRadius, pupilPaint)
        canvas.drawCircle(rightPupilX, rightPupilY, pupilRadius, pupilPaint)
    }
}
