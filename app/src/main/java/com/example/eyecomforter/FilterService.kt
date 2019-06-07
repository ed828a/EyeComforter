package com.example.eyecomforter

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.RemoteViews
import com.example.eyecomforter.Utility.ACTION_REQUEST_CODE
import com.example.eyecomforter.Utility.CHANNEL_ID
import com.example.eyecomforter.Utility.FOREGROUND_SERVICE_NOTIFICATION_ID
import com.example.eyecomforter.Utility.MAIN_ACTION
import com.example.eyecomforter.Utility.NEXT_ACTION
import com.example.eyecomforter.Utility.NOTIFICATION_TITLE
import com.example.eyecomforter.Utility.PLAY_ACTION
import com.example.eyecomforter.Utility.PREV_ACTION
import com.example.eyecomforter.Utility.START_FOREGROUND_ACTION
import com.example.eyecomforter.Utility.STOP_FOREGROUND_ACTION


@Suppress("DEPRECATION")
class FilterService : Service() {

    private lateinit var mOverlayView: AppCompatImageView

    private var currentLevel = 50
    private var isStartup = true

    private lateinit var params: WindowManager.LayoutParams
    private lateinit var wm: WindowManager

    override fun onBind(intent: Intent): IBinder {
        throw Exception("No binding on Filter Service")
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate()")

        mOverlayView = AppCompatImageView(this)

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
        params.dimAmount = 0.5f
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.addView(mOverlayView, params)
    }

    /**
     * When the value of agb changed,
     * @return
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (isStartup) {
            currentLevel = 50
            isStartup = false
        } else {
            currentLevel = intent.getIntExtra("level", currentLevel)
        }

        Log.d(TAG, "onStartCommand(): currentLevel = $currentLevel")
        // to update alpha & dimAmount by the rule:
        //         params.alpha = (float) (currentLevel *5 / 1000.0)
        //        params.dimAmount = (float) (currentLevel / 100.0)

        val b = 255 - 255 * Math.sqrt(currentLevel * 1.0 / 100)
        mOverlayView.setBackgroundColor(Color.rgb(255, 225, b.toInt()))

        wm.updateViewLayout(mOverlayView, params)

        when (intent.action) {
            START_FOREGROUND_ACTION -> {
                Log.i(TAG, "Received Start Foreground Intent ")

                val notificationIntent = Intent(this, MainActivity::class.java)
                notificationIntent.action = MAIN_ACTION
                notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val notificationPendingIntent = PendingIntent.getActivity(
                    this,
                    ACTION_REQUEST_CODE,
                    notificationIntent,
                    0
                )

                val playIntent = Intent(this, FilterService::class.java)
                playIntent.action = STOP_FOREGROUND_ACTION
                val pplayIntent = PendingIntent.getService(
                    this,
                    ACTION_REQUEST_CODE,
                    playIntent,
                    0
                )

                val simpleContentView = RemoteViews(this.packageName, R.layout.notification_blf_layout)
//                    .apply {
//                    setImageViewResource(R.id.btnPause, R.drawable.pause)
//                    setImageViewResource(R.id.btnPlay, R.drawable.play)
//                    setImageViewResource(R.id.btnDelete, R.drawable.cross)
//                }

                val expandedView = RemoteViews(this.packageName, R.layout.big_notification_blf_layout)
//                    .apply {
//                    setImageViewResource(R.id.btnPause, R.drawable.pause)
//                    setImageViewResource(R.id.btnPlay, R.drawable.play)
//                    setImageViewResource(R.id.btnDelete, R.drawable.cross)
//                }

                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setSmallIcon(R.mipmap.ic_blf_notification_logo)
                    .setContentIntent(notificationPendingIntent) // the action when tapping the notification
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(simpleContentView)
                    .setCustomBigContentView(expandedView)
                    .build()

                startForeground(
                    FOREGROUND_SERVICE_NOTIFICATION_ID,
                    notification
                )
            }

            PREV_ACTION -> {
                Log.i(TAG, "Clicked Previous")
                // perform play previous action here
            }

            PLAY_ACTION -> {
                Log.i(TAG, "Clicked Play")
                // perform play/pause action here
            }

            NEXT_ACTION -> {
                Log.i(TAG, "Clicked Next")
                // perform play next action here
            }

            STOP_FOREGROUND_ACTION -> {
                Log.i(TAG, "Received Stop Foreground Intent")

                stopForeground(true) //
                stopSelf() // stop this service
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy")

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.removeView(mOverlayView)
    }

    /**
     * Notification click listeners
     * @param view
     */
    fun setListeners(view: RemoteViews) {
//        val delete = Intent(NOTIFY_DELETE)
//        val pause = Intent(NOTIFY_PAUSE)
//        val play = Intent(NOTIFY_PLAY)
//
//        val pDelete = PendingIntent.getBroadcast(applicationContext, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT)
//        view.setOnClickPendingIntent(R.id.btnDelete, pDelete)
//
//        val pPause = PendingIntent.getBroadcast(applicationContext, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT)
//        view.setOnClickPendingIntent(R.id.btnPause, pPause)
//
//        val pPlay = PendingIntent.getBroadcast(applicationContext, 0, play, PendingIntent.FLAG_UPDATE_CURRENT)
//        view.setOnClickPendingIntent(R.id.btnPlay, pPlay)

    }


    companion object {
        private const val TAG = "FilterService"
    }

}
