package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple

data class WidgetCustomSettings(
    val opacity: Float = 0.85f,
    val colorIndex: Int = 0,
    val showCategoryBadge: Boolean = true,
    val showProgressArc: Boolean = true,
    val showTargetDate: Boolean = true
) {
    val activeColor: Color
        get() = when (colorIndex) {
            1 -> Color(0xFF10B981) // Emerald
            2 -> Color(0xFF8B5CF6) // Violet
            3 -> Color(0xFFF59E0B) // Solar Gold
            4 -> Color(0xFFF43F5E) // Neon Rose
            else -> Color(0xFF00F2FE) // Electric Cyan
        }

    val activeColorHex: Int
        get() = when (colorIndex) {
            1 -> 0xFF10B981.toInt()
            2 -> 0xFF8B5CF6.toInt()
            3 -> 0xFFF59E0B.toInt()
            4 -> 0xFFF43F5E.toInt()
            else -> 0xFF00F2FE.toInt()
        }
}

object ChronoWidgetPreferences {
    private const val PREFS_NAME = "chrono_widget_prefs"
    private const val KEY_OPACITY = "pref_widget_opacity"
    private const val KEY_COLOR_INDEX = "pref_widget_color_index"
    private const val KEY_SHOW_BADGE = "pref_show_badge"
    private const val KEY_SHOW_PROGRESS = "pref_show_progress"
    private const val KEY_SHOW_TARGET = "pref_show_target"

    val COLOR_SWATCHES = listOf(
        Pair("Electric Cyan", Color(0xFF00F2FE)),
        Pair("Emerald", Color(0xFF10B981)),
        Pair("Violet", Color(0xFF8B5CF6)),
        Pair("Solar Gold", Color(0xFFF59E0B)),
        Pair("Neon Rose", Color(0xFFF43F5E))
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun loadSettings(context: Context): WidgetCustomSettings {
        val prefs = getPrefs(context)
        return WidgetCustomSettings(
            opacity = prefs.getFloat(KEY_OPACITY, 0.85f),
            colorIndex = prefs.getInt(KEY_COLOR_INDEX, 0),
            showCategoryBadge = prefs.getBoolean(KEY_SHOW_BADGE, true),
            showProgressArc = prefs.getBoolean(KEY_SHOW_PROGRESS, true),
            showTargetDate = prefs.getBoolean(KEY_SHOW_TARGET, true)
        )
    }

    fun saveSettings(context: Context, settings: WidgetCustomSettings, broadcastUpdate: Boolean = true) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putFloat(KEY_OPACITY, settings.opacity)
            .putInt(KEY_COLOR_INDEX, settings.colorIndex)
            .putBoolean(KEY_SHOW_BADGE, settings.showCategoryBadge)
            .putBoolean(KEY_SHOW_PROGRESS, settings.showProgressArc)
            .putBoolean(KEY_SHOW_TARGET, settings.showTargetDate)
            .apply()

        if (broadcastUpdate) {
            notifyWidgetUpdate(context)
        }
    }

    fun notifyWidgetUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, ChronoWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(context, ChronoWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
