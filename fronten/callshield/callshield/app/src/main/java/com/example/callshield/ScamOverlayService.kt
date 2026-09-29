package com.example.callshield

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class ScamOverlayService : Service() {

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    companion object {
        private const val CHANNEL_ID = "callshield_security"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notification = createNotification()

        startForeground(
            NOTIFICATION_ID,
            notification
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val phoneNumber =
            intent?.getStringExtra("PHONE_NUMBER")

        val reportedCount =
            intent?.getIntExtra("REPORTED_COUNT", 0) ?: 0

        showScamWarning(
            phoneNumber,
            reportedCount
        )

        return START_NOT_STICKY
    }

    private fun showScamWarning(
        phoneNumber: String?,
        reportedCount: Int
    ) {

        // Remove old overlay if one already exists
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (_: Exception) {
            }

            overlayView = null
        }

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        // =========================
        // MAIN WARNING BOX
        // =========================

        val container = LinearLayout(this)

        container.orientation = LinearLayout.VERTICAL

        container.setPadding(
            45,
            35,
            45,
            35
        )

        container.setBackgroundColor(
            Color.rgb(30, 12, 45)
        )

        // =========================
        // WARNING TITLE
        // =========================

        val title = TextView(this)

        title.text = "⚠️  SCAM WARNING"

        title.textSize = 23f

        title.setTextColor(
            Color.rgb(255, 90, 100)
        )

        title.setGravity(Gravity.CENTER)

        title.setPadding(
            0,
            0,
            0,
            15
        )

        container.addView(title)

        // =========================
        // PHONE NUMBER
        // =========================

        val numberText = TextView(this)

        numberText.text =
            if (!phoneNumber.isNullOrEmpty()) {
                phoneNumber
            } else {
                "Unknown number"
            }

        numberText.textSize = 18f

        numberText.setTextColor(
            Color.WHITE
        )

        numberText.setGravity(Gravity.CENTER)

        container.addView(numberText)

        // =========================
        // REPORT COUNT
        // =========================

        if (reportedCount > 0) {

            val reportText = TextView(this)

            reportText.text =
                "Reported $reportedCount times"

            reportText.textSize = 15f

            reportText.setTextColor(
                Color.LTGRAY
            )

            reportText.setGravity(Gravity.CENTER)

            reportText.setPadding(
                0,
                12,
                0,
                0
            )

            container.addView(reportText)
        }

        // =========================
        // DESCRIPTION
        // =========================

        val description = TextView(this)

        description.text =
            "This number has been reported as suspicious.\nBe careful before answering."

        description.textSize = 15f

        description.setTextColor(
            Color.WHITE
        )

        description.setGravity(Gravity.CENTER)

        description.setPadding(
            0,
            20,
            0,
            0
        )

        container.addView(description)

        // =========================
        // OVERLAY PARAMETERS
        // =========================

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity =
            Gravity.TOP or Gravity.CENTER_HORIZONTAL

        params.y = 80

        // =========================
        // SHOW OVERLAY
        // =========================

        overlayView = container

        try {
            windowManager?.addView(
                overlayView,
                params
            )
        } catch (_: Exception) {
        }
    }

    // =========================
    // NOTIFICATION CHANNEL
    // =========================

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "CallShield Security",
                NotificationManager.IMPORTANCE_LOW
            )

            channel.description =
                "CallShield scam-call protection"

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }
    }

    // =========================
    // FOREGROUND NOTIFICATION
    // =========================

    private fun createNotification(): Notification {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder(
                this,
                CHANNEL_ID
            )
                .setContentTitle("CallShield is active")
                .setContentText(
                    "Protecting you from suspicious calls"
                )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_alert
                )
                .setOngoing(true)
                .build()

        } else {

            Notification.Builder(this)
                .setContentTitle("CallShield is active")
                .setContentText(
                    "Protecting you from suspicious calls"
                )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_alert
                )
                .setOngoing(true)
                .build()
        }
    }

    // =========================
    // CLEAN UP
    // =========================

    override fun onDestroy() {

        if (overlayView != null) {

            try {
                windowManager?.removeView(
                    overlayView
                )
            } catch (_: Exception) {
            }

            overlayView = null
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
