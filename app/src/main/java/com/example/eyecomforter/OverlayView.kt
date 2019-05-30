package com.example.eyecomforter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log

class OverlayView(context: Context) : android.support.v7.widget.AppCompatImageView(context) {
    private val mLoadPaint: Paint

    private var opacityPercent = 20
        set(opacityPercent) {
            mLoadPaint.alpha = 255 / 100 * opacityPercent
            field = opacityPercent
        } // 20% opacity by default

    private var color = Color.BLACK
        set(color) {
            if (this.color != color) {
                Log.d(javaClass.simpleName, "Changing color to " + Integer.toHexString(color))
            }
            mLoadPaint.color = color

            opacityPercent = opacityPercent
            field = color
        }

    init {
        Log.d(javaClass.simpleName, "OverlayView created")

        mLoadPaint = Paint()
        mLoadPaint.isAntiAlias = true
        mLoadPaint.textSize = 10f

        mLoadPaint.color = color // Black filter by default
        mLoadPaint.alpha = 255 / 100 * opacityPercent
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPaint(mLoadPaint)
    }


    fun redraw(): Boolean {
        this.invalidate()

        return true
    }
}