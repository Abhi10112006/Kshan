package com.abhi.kshan.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.abhi.kshan.MainActivity
import com.abhi.kshan.R
import com.abhi.kshan.data.ChronoDatabase
import com.abhi.kshan.data.ChronoEvent
import com.abhi.kshan.engine.TimeEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class ChronoWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "KshanWidget"
        const val ACTION_BOUNDARY_REFRESH = "com.abhi.kshan.widget.ACTION_BOUNDARY_REFRESH"
        private const val ALARM_REQUEST_CODE = 4201
        private const val DAY_MILLIS = 86_400_000L

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

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidgetsInternal(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            Intent.ACTION_TIME_CHANGED, // "android.intent.action.TIME_SET"
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            ACTION_BOUNDARY_REFRESH -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, ChronoWidgetProvider::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                if (ids.isNotEmpty()) {
                    updateWidgetsInternal(context, appWidgetManager, ids)
                }
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
        val customSettings = ChronoWidgetPreferences.loadSettings(context)

        CoroutineScope(Dispatchers.IO).launch {
            val activeEvent = dao.getPinnedEventDirect() ?: dao.getNextUpcomingEventDirect()
            val now = ZonedDateTime.now()
            val nowEpochMillis = now.toInstant().toEpochMilli()

            var targetZdt: ZonedDateTime? = null
            if (activeEvent != null) {
                targetZdt = try {
                    val rawZdt = ZonedDateTime.parse(activeEvent.targetIsoString)
                    val (effectiveZdt, _) = TimeEngine.resolveEffectiveTarget(
                        now = now,
                        raw = rawZdt,
                        repeatsYearly = activeEvent.repeatsYearly,
                        isCountUpMode = false
                    )
                    effectiveZdt
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse widget event ISO string: ${activeEvent.targetIsoString}", e)
                    now.plusDays(30)
                }
            }

            val targetEpochMillis = targetZdt?.toInstant()?.toEpochMilli() ?: 0L
            val remainingMillis = if (activeEvent != null) (targetEpochMillis - nowEpochMillis) else 0L
            val isReached = activeEvent != null && remainingMillis <= 0L

            // Calculate Days and Remainder Millis: target - (days * 24h)
            val days = if (remainingMillis > 0L) remainingMillis / DAY_MILLIS else 0L
            val rawRemainderMillis = if (remainingMillis > 0L) remainingMillis - (days * DAY_MILLIS) else 0L
            val remainderMillis = if (remainingMillis > 0L && rawRemainderMillis <= 0L) DAY_MILLIS else rawRemainderMillis

            // Schedule target-relative boundary alarm (no SCHEDULE_EXACT_ALARM permission required)
            scheduleNextBoundaryRefresh(context, activeEvent != null && !isReached, remainderMillis)

            for (widgetId in appWidgetIds) {
                val options = appWidgetManager.getAppWidgetOptions(widgetId)
                val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
                val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)

                val isCompact2x2 = minWidthDp < 200
                val isPill4x1 = !isCompact2x2 && minHeightDp < 95

                val views = RemoteViews(context.packageName, R.layout.widget_chrono_scope)

                val widgetBitmap = renderWidgetBitmap(
                    event = activeEvent,
                    targetZdt = targetZdt,
                    daysCount = days,
                    isReached = isReached,
                    isCompact2x2 = isCompact2x2,
                    isPill4x1 = isPill4x1,
                    settings = customSettings
                )

                if (widgetBitmap != null) {
                    views.setImageViewBitmap(R.id.widget_canvas_view, widgetBitmap)
                }

                // Configure live ticking Chronometer on 4x2 & 4x1 when countdown is active
                if (activeEvent != null && !isReached && !isCompact2x2) {
                    val chronometerBase = SystemClock.elapsedRealtime() + remainderMillis
                    views.setViewVisibility(R.id.widget_live_chronometer, View.VISIBLE)
                    views.setTextColor(R.id.widget_live_chronometer, customSettings.activeColorHex)
                    views.setChronometerCountDown(R.id.widget_live_chronometer, true)
                    views.setChronometer(R.id.widget_live_chronometer, chronometerBase, "%s", true)
                } else {
                    views.setViewVisibility(R.id.widget_live_chronometer, View.GONE)
                    views.setChronometer(R.id.widget_live_chronometer, SystemClock.elapsedRealtime(), null, false)
                }

                // Update semantic accessibility fields
                if (activeEvent != null) {
                    val telemetry = TimeEngine.calculateCountdown(now, targetZdt ?: now)
                    views.setTextViewText(R.id.widget_title, activeEvent.title)
                    views.setTextViewText(R.id.widget_category, activeEvent.category)
                    views.setTextViewText(R.id.widget_days_number, if (isReached) "0" else "${telemetry.days}")
                    views.setTextViewText(R.id.widget_days_label, if (isReached) "REACHED" else "DAYS")
                    views.setTextViewText(R.id.widget_status, if (isReached) "Milestone Reached" else "remaining")
                } else {
                    views.setTextViewText(R.id.widget_title, "No active countdowns")
                    views.setTextViewText(R.id.widget_category, "New Event")
                    views.setTextViewText(R.id.widget_days_number, "+")
                    views.setTextViewText(R.id.widget_days_label, "TRACK")
                    views.setTextViewText(R.id.widget_status, "Tap to add")
                }

                val clickIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    /**
     * Schedules an inexact wake alarm at the next target-relative 24h boundary (or exact target instant)
     * using setAndAllowWhileIdle so Android 14+ requires zero special alarm permissions.
     */
    private fun scheduleNextBoundaryRefresh(
        context: Context,
        shouldSchedule: Boolean,
        remainderMillis: Long
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ChronoWidgetProvider::class.java).apply {
                action = ACTION_BOUNDARY_REFRESH
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (!shouldSchedule || remainderMillis <= 0L) {
                alarmManager.cancel(pendingIntent)
                return
            }

            // Trigger 1 second after the day boundary so integer division cleanly steps to the new day count
            val triggerAtMillis = System.currentTimeMillis() + remainderMillis + 1000L
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to schedule widget boundary refresh alarm", e)
        }
    }

    private fun renderWidgetBitmap(
        event: ChronoEvent?,
        targetZdt: ZonedDateTime?,
        daysCount: Long,
        isReached: Boolean,
        isCompact2x2: Boolean,
        isPill4x1: Boolean,
        settings: WidgetCustomSettings
    ): Bitmap? {
        val width = if (isCompact2x2) 340 else 640
        val height = when {
            isPill4x1 -> 160
            isCompact2x2 -> 340
            else -> 320
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val accentColor = if (isReached) 0xFFFFD700.toInt() else settings.activeColorHex
        val opacity = settings.opacity.coerceIn(0.2f, 1.0f)

        val alphaInt = (opacity * 255).toInt().coerceIn(0, 255)
        val baseFillColor = (alphaInt shl 24) or (0x0B101E and 0x00FFFFFF)

        val cardRect = RectF(6f, 6f, width - 6f, height - 6f)
        val cornerRadius = if (isPill4x1) 70f else 36f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = baseFillColor
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            val glowColor = (0x2E shl 24) or (accentColor and 0x00FFFFFF)
            shader = RadialGradient(
                width / 2f, 12f,
                width * 0.6f,
                glowColor,
                0x00000000,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, glowPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            val startSpecular = 0x45FFFFFF.toInt()
            val midSpecular = (0x55 shl 24) or (accentColor and 0x00FFFFFF)
            shader = LinearGradient(
                0f, 0f,
                width.toFloat(), height.toFloat(),
                intArrayOf(startSpecular, midSpecular, 0x00000000),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isSubpixelText = true
        }

        val eventTitle = event?.title ?: "Tap to add your first event"
        val targetDateStr = targetZdt?.format(TimeEngine.SHORT_DATE_FORMATTER) ?: ""

        if (isCompact2x2) {
            // 2x2 Compact Orbit: Days-only orbital dial
            val cx = width / 2f
            val cy = height * 0.46f
            val radius = 78f

            if (settings.showProgressArc) {
                val ringBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 12f
                    color = 0x22FFFFFF
                }
                canvas.drawCircle(cx, cy, radius, ringBg)

                val progPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 12f
                    strokeCap = Paint.Cap.ROUND
                    color = accentColor
                }
                val sweep = if (isReached) 360f else ((365 - (daysCount % 365)) / 365f) * 360f
                val rRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
                canvas.drawArc(rRect, -90f, sweep.coerceIn(30f, 360f), false, progPaint)
            }

            textPaint.apply {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textSize = if (isReached) 34f else 56f
                color = 0xFFFFFFFF.toInt()
                textAlign = Paint.Align.CENTER
            }
            val centerText = when {
                event == null -> "--"
                isReached -> "DONE"
                else -> "$daysCount"
            }
            canvas.drawText(centerText, cx, cy + 16f, textPaint)

            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 18f
                color = accentColor
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(if (isReached) "REACHED" else "DAYS LEFT", cx, cy + 44f, textPaint)

            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 22f
                color = 0xFFF1F5F9.toInt()
                textAlign = Paint.Align.CENTER
            }
            val shortTitle = if (eventTitle.length > 18) eventTitle.take(16) + "…" else eventTitle
            canvas.drawText(shortTitle, cx, height - 34f, textPaint)
            return bitmap
        }

        if (isPill4x1) {
            // 4x1 Minimal Pill: Brand + Event Title on left, Days badge next to live Chronometer on right
            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 26f
                color = accentColor
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("क्षण", 34f, 68f, textPaint)

            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 26f
                color = 0xFFFFFFFF.toInt()
            }
            val shortTitle = if (eventTitle.length > 22) eventTitle.take(20) + "…" else eventTitle
            canvas.drawText(shortTitle, 34f, 108f, textPaint)

            textPaint.apply {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textSize = if (isReached) 26f else 36f
                color = accentColor
                textAlign = Paint.Align.RIGHT
            }
            val rightLabel = when {
                event == null -> "--"
                isReached -> "MILESTONE REACHED"
                else -> "${daysCount}d •"
            }
            canvas.drawText(rightLabel, width - 190f, 94f, textPaint)
            return bitmap
        }

        // 4x2 Panoramic Layout
        textPaint.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
            color = accentColor
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("क्षण • Kshan", 36f, 52f, textPaint)

        val categoryText = if (isReached) "MILESTONE REACHED" else (event?.category?.uppercase() ?: "COUNTDOWN")
        if (settings.showCategoryBadge || isReached) {
            val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = (0x33 shl 24) or (accentColor and 0x00FFFFFF)
            }
            val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = (0x66 shl 24) or (accentColor and 0x00FFFFFF)
            }
            val pillWidth = (categoryText.length * 14f) + 32f
            val pillRect = RectF(width - 36f - pillWidth, 26f, width - 36f, 62f)
            canvas.drawRoundRect(pillRect, 18f, 18f, pillPaint)
            canvas.drawRoundRect(pillRect, 18f, 18f, pillBorderPaint)

            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 19f
                color = accentColor
            }
            canvas.drawText(categoryText, width - 36f - pillWidth + 16f, 50f, textPaint)
        }

        val arcCenterX = 96f
        val arcCenterY = 160f
        val arcRadius = 52f

        if (settings.showProgressArc) {
            val ringBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 10f
                color = 0x22FFFFFF
            }
            canvas.drawCircle(arcCenterX, arcCenterY, arcRadius, ringBgPaint)

            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 10f
                strokeCap = Paint.Cap.ROUND
                color = accentColor
            }
            val sweep = if (isReached) 360f else ((365 - (daysCount % 365)) / 365f) * 360f
            val ringRect = RectF(arcCenterX - arcRadius, arcCenterY - arcRadius, arcCenterX + arcRadius, arcCenterY + arcRadius)
            canvas.drawArc(ringRect, -90f, sweep.coerceIn(25f, 360f), false, progressPaint)
        }

        val numberStartX = if (settings.showProgressArc) 176f else 44f

        if (isReached) {
            textPaint.apply {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textSize = 42f
                color = accentColor
            }
            canvas.drawText("REACHED!", numberStartX, 168f, textPaint)

            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 22f
                color = 0xCCFFFFFF.toInt()
            }
            canvas.drawText("Target moment has arrived", numberStartX, 200f, textPaint)
        } else {
            textPaint.apply {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textSize = 86f
                color = 0xFFFFFFFF.toInt()
            }
            val daysText = if (event != null) "$daysCount" else "--"
            canvas.drawText(daysText, numberStartX, 190f, textPaint)

            val daysNumWidth = textPaint.measureText(daysText)

            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 22f
                color = accentColor
            }
            canvas.drawText("DAYS", numberStartX + daysNumWidth + 14f, 152f, textPaint)

            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 18f
                color = 0x99FFFFFF.toInt()
            }
            canvas.drawText("+ LIVE TIMER", numberStartX + daysNumWidth + 14f, 182f, textPaint)
        }

        textPaint.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 26f
            color = 0xFFF1F5F9.toInt()
        }
        val clippedTitle = if (eventTitle.length > 28) eventTitle.take(26) + "…" else eventTitle
        canvas.drawText(clippedTitle, 36f, 275f, textPaint)

        if (settings.showTargetDate && targetDateStr.isNotBlank()) {
            textPaint.apply {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textSize = 20f
                color = 0x99FFFFFF.toInt()
            }
            val targetLabel = "Target: $targetDateStr"
            val targetWidth = textPaint.measureText(targetLabel)
            canvas.drawText(targetLabel, width - 36f - targetWidth, 275f, textPaint)
        }

        return bitmap
    }
}
