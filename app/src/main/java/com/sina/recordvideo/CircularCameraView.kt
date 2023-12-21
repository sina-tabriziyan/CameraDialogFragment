package com.sina.recordvideo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout

class CircularCameraView(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    init {
        setWillNotDraw(false)
        circlePaint.color = Color.RED // رنگ دایره را اینجا تنظیم کنید
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = width.coerceAtMost(height) / 2f
        val centerX = width / 2f
        val centerY = height / 2f

        path.reset()
        path.addCircle(centerX, centerY, radius, Path.Direction.CW)
        canvas.clipPath(path)

        val previewView = getChildAt(0) as? PreviewView

        previewView?.let {
            val previewWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            val previewHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            it.measure(previewWidthMeasureSpec, previewHeightMeasureSpec)

            val previewWidth = it.measuredWidth
            val previewHeight = it.measuredHeight
            val left = width / 2 - previewWidth / 2
            val top = height / 2 - previewHeight / 2
            val right = left + previewWidth
            val bottom = top + previewHeight

            it.layout(left, top, right, bottom)
        }
    }
}