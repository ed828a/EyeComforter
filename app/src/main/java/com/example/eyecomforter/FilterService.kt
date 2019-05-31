package com.example.eyecomforter

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager

@Suppress("DEPRECATION")
class FilterService : Service() {

    private lateinit var mOverlayView: OverlayView

    private var currentLevel = 0

    private lateinit var params: WindowManager.LayoutParams
    private lateinit var wm: WindowManager

    override fun onBind(intent: Intent): IBinder {
        throw Exception("No binding on Filter Service")
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate()")

        mOverlayView = OverlayView(this)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        // An alpha value to apply to this entire window.
        // An alpha of 1.0 means fully opaque and 0.0 means fully transparent
        params.alpha = 0.3f

        // When FLAG_DIM_BEHIND is set, this is the amount of dimming to apply.
        // Range is from 1.0 for completely opaque to 0.0 for no dim.
        params.dimAmount = 0f
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.addView(mOverlayView, params)
    }


    /**
     * When the value of agb changed,
     * @return
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        currentLevel = intent.getIntExtra("level", currentLevel)

        Log.d(TAG, "onStartCommand(): currentLevel = $currentLevel")
        // to update alpha & dimAmount by the rule:
        //         params.alpha = (float) (currentLevel *5 / 1000.0)
        //        params.dimAmount = (float) (currentLevel / 100.0)
        val b = 255 - 255 * Math.sqrt(currentLevel * 1.0 / 100)
        mOverlayView.setBackgroundColor(Color.rgb(225, 225, b.toInt()))

        wm.updateViewLayout(mOverlayView, params)

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy")

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.removeView(mOverlayView)
    }


    companion object {
        private const val TAG = "FilterService"
    }

}
