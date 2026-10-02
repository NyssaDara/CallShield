package com.example.callshield

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val state =
            intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        val number =
            intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        // EVERY INCOMING CALL
        if (state == TelephonyManager.EXTRA_STATE_RINGING) {

            val overlayIntent =
                Intent(context, ScamOverlayService::class.java).apply {

                    putExtra(
                        "PHONE_NUMBER",
                        number
                    )

                    putExtra(
                        "REPORTED_COUNT",
                        0
                    )
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                context.startForegroundService(
                    overlayIntent
                )

            } else {

                context.startService(
                    overlayIntent
                )
            }
        }

        // CALL ENDED
        if (state == TelephonyManager.EXTRA_STATE_IDLE) {

            context.stopService(
                Intent(
                    context,
                    ScamOverlayService::class.java
                )
            )
        }
    }
}