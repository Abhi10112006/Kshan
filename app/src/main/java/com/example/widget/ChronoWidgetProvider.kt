package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.ChronoDatabase
import com.example.engine.CountdownStatus
import com.example.engine.TimeEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class ChronoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ChronoWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                val provider = ChronoWidgetProvider()
                provider.updateWidgetsInternal(context, appWidgetManager, ids)
            }
        }
    }

    internal fun updateWidgetsInternal(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val db = ChronoDatabase.getInstance(context)
        val dao = db.chronoDao()

        CoroutineScope(Dispatchers.IO).launch {
            // Check pinned event first, or fallback to the nearest upcoming event
            val activeEvent = dao.getPinnedEventDirect() ?: dao.getNextUpcomingEventDirect()
            val now = ZonedDateTime.now()

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_chrono_scope)

                if (activeEvent != null) {
                    val target = try {
                        ZonedDateTime.parse(activeEvent.targetIsoString)
                    } catch (e: Exception) {
                        now.plusDays(30)
                    }

                    val telemetry = TimeEngine.calculateCountdown(now, target)
                    val daysStr = "${telemetry.days}"
                    val statusStr = when (telemetry.status) {
                        CountdownStatus.TARGET_REACHED -> "arrived!"
                        CountdownStatus.EVENT_PASSED -> "passed"
                        else -> "remaining"
                    }

                    views.setTextViewText(R.id.widget_title, activeEvent.title)
                    views.setTextViewText(R.id.widget_category, activeEvent.category)
                    views.setTextViewText(R.id.widget_days_number, daysStr)
                    views.setTextViewText(R.id.widget_days_label, "DAYS")
                    views.setTextViewText(R.id.widget_status, statusStr)
                } else {
                    // Empty state: elegant frosted prompt to add a new countdown
                    views.setTextViewText(R.id.widget_title, "No active countdowns")
                    views.setTextViewText(R.id.widget_category, "New Event")
                    views.setTextViewText(R.id.widget_days_number, "+")
                    views.setTextViewText(R.id.widget_days_label, "TRACK")
                    views.setTextViewText(R.id.widget_status, "Tap to add")
                }

                // Intent to open app
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
}
