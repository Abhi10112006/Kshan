package com.example.widget

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
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.ChronoDatabase
import com.example.data.ChronoEvent
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

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ChronoWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                updateWidgetsInternal(context, appWidgetManager, ids)
            }
        }
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
        val customSettings = ChronoWidgetPreferences.loadSettings(context)

        CoroutineScope(Dispatchers.IO).launch {
            val activeEvent = dao.getPinnedEventDirect() ?: dao.getNextUpcomingEventDirect()
            val now = ZonedDateTime.now()

            // Render a crisp 640x320 Canvas Bitmap (well below 800x400 max, strictly Binder safe)
            val widgetBitmap = renderWidgetBitmap(activeEvent, now, customSettings)

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_chrono_scope)

                if (widgetBitmap != null) {
                    views.setImageViewBitmap(R.id.widget_canvas_view, widgetBitmap)
                }

                // Also update semantic accessibility fields in fallback layout
                if (activeEvent != null) {
                    val target = try {
                        ZonedDateTime.parse(activeEvent.targetIsoString)
                    } catch (e: Exception) {
                        now.plusDays(30)
                    }
                    val telemetry = TimeEngine.calculateCountdown(now, target)
                    views.setTextViewText(R.id.widget_title, activeEvent.title)
                    views.setTextViewText(R.id.widget_category, activeEvent.category)
                    views.setTextViewText(R.id.widget_days_number, "${telemetry.days}")
                    views.setTextViewText(R.id.widget_days_label, "DAYS")
                    views.setTextViewText(R.id.widget_status, "remaining")
                } else {
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

    private fun renderWidgetBitmap(
        event: ChronoEvent?,
        now: ZonedDateTime,
        settings: WidgetCustomSettings
    ): Bitmap? {
        val width = 640
        val height = 320
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val accentColor = settings.activeColorHex
        val opacity = settings.opacity.coerceIn(0.2f, 1.0f)

        // 1. Base Glass Fill (#0B101E with user opacity)
        val alphaInt = (opacity * 255).toInt().coerceIn(0, 255)
        val baseFillColor = (alphaInt shl 24) or (0x0B101E and 0x00FFFFFF)

        val cardRect = RectF(6f, 6f, width - 6f, height - 6f)
        val cornerRadius = 36f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = baseFillColor
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)

        // 2. Top-center radial illumination glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            val glowColor = (0x28 shl 24) or (accentColor and 0x00FFFFFF)
            shader = RadialGradient(
                width / 2f, 12f,
                width * 0.55f,
                glowColor,
                0x00000000,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, glowPaint)

        // 3. Asymmetric specular glass border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            val startSpecular = 0x40FFFFFF.toInt()
            val midSpecular = (0x45 shl 24) or (accentColor and 0x00FFFFFF)
            shader = LinearGradient(
                0f, 0f,
                width.toFloat(), height.toFloat(),
                intArrayOf(startSpecular, midSpecular, 0x00000000),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)

        // Text & Content Paints
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isSubpixelText = true
        }

        // 4. Header: Brand squircle mark + Category pill
        textPaint.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
            color = accentColor
        }
        canvas.drawText("क्षण • Kshan", 36f, 52f, textPaint)

        val categoryText = event?.category?.uppercase() ?: "COUNTDOWN"
        if (settings.showCategoryBadge) {
            val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = (0x33 shl 24) or (accentColor and 0x00FFFFFF)
            }
            val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = (0x66 shl 24) or (accentColor and 0x00FFFFFF)
            }
            val pillWidth = (categoryText.length * 16f) + 32f
            val pillRect = RectF(width - 36f - pillWidth, 26f, width - 36f, 62f)
            canvas.drawRoundRect(pillRect, 18f, 18f, pillPaint)
            canvas.drawRoundRect(pillRect, 18f, 18f, pillBorderPaint)

            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 20f
                color = accentColor
            }
            canvas.drawText(categoryText, width - 36f - pillWidth + 16f, 51f, textPaint)
        }

        // 5. Main Telemetry: Number of Days + Optional Progress Arc
        var daysCount = 0L
        var targetDateStr = ""
        var eventTitle = "Tap to add your first event"

        if (event != null) {
            eventTitle = event.title
            val target = try {
                ZonedDateTime.parse(event.targetIsoString)
            } catch (e: Exception) {
                now.plusDays(30)
            }
            val telemetry = TimeEngine.calculateCountdown(now, target)
            daysCount = telemetry.days
            targetDateStr = target.format(TimeEngine.SHORT_DATE_FORMATTER)
        }

        val arcCenterX = 100f
        val arcCenterY = 160f
        val arcRadius = 55f

        if (settings.showProgressArc) {
            // Background ring
            val ringBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 10f
                color = 0x22FFFFFF
            }
            canvas.drawCircle(arcCenterX, arcCenterY, arcRadius, ringBgPaint)

            // Progress sweep
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 10f
                strokeCap = Paint.Cap.ROUND
                color = accentColor
            }
            val sweep = ((365 - (daysCount % 365)) / 365f) * 360f
            val ringRect = RectF(arcCenterX - arcRadius, arcCenterY - arcRadius, arcCenterX + arcRadius, arcCenterY + arcRadius)
            canvas.drawArc(ringRect, -90f, sweep.coerceIn(25f, 360f), false, progressPaint)
        }

        // Days Monospace Number
        val numberStartX = if (settings.showProgressArc) 190f else 46f

        textPaint.apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 96f
            color = 0xFFFFFFFF.toInt()
        }
        val daysText = if (event != null) "$daysCount" else "--"
        canvas.drawText(daysText, numberStartX, 192f, textPaint)

        val daysNumWidth = textPaint.measureText(daysText)

        // "DAYS remaining" stack
        textPaint.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            color = accentColor
        }
        canvas.drawText("DAYS", numberStartX + daysNumWidth + 16f, 150f, textPaint)

        textPaint.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 20f
            color = 0x99FFFFFF.toInt()
        }
        canvas.drawText("remaining", numberStartX + daysNumWidth + 16f, 184f, textPaint)

        // 6. Event Title & Target Date at bottom
        textPaint.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
            color = 0xFFF1F5F9.toInt()
        }
        val clippedTitle = if (eventTitle.length > 32) eventTitle.take(30) + "…" else eventTitle
        canvas.drawText(clippedTitle, 36f, 275f, textPaint)

        if (settings.showTargetDate && targetDateStr.isNotBlank()) {
            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 22f
                color = 0x99FFFFFF.toInt()
            }
            val targetLabel = "Target: $targetDateStr"
            val targetWidth = textPaint.measureText(targetLabel)
            canvas.drawText(targetLabel, width - 36f - targetWidth, 275f, textPaint)
        }

        return bitmap
    }
}
