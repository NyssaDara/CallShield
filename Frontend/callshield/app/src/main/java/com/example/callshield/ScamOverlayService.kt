package com.example.callshield

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class ScamOverlayService : Service() {

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        showScamWarning()

        return START_NOT_STICKY
    }

    private fun showScamWarning() {

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val warningText = TextView(this)

        warningText.text = "⚠️ SCAM WARNING\n\nThis number has been reported as suspicious."
        warningText.textSize = 20f
        warningText.gravity = Gravity.CENTER
        warningText.setPadding(40, 40, 40, 40)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        overlayView = warningText

        windowManager?.addView(overlayView, params)
    }

    override fun onDestroy() {

        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}