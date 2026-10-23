package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.MainActivity

object ChronoWidgetPinningManager {

    fun isPinningSupported(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.isRequestPinAppWidgetSupported
        } else {
            false
        }
    }

    fun requestPinWidget(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val provider = ComponentName(context, ChronoWidgetProvider::class.java)

                val callbackIntent = Intent(context, MainActivity::class.java).apply {
                    action = "com.example.widget.PIN_SUCCESS"
                }
                val successCallback = PendingIntent.getActivity(
                    context,
                    101,
                    callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                return appWidgetManager.requestPinAppWidget(provider, null, successCallback)
            }
        }
        return false
    }
}
