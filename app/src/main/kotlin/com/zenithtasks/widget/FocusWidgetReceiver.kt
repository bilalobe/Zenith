package com.zenithtasks.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Widget receiver that handles widget-related broadcasts and lifecycle events.
 * This includes creation, updates, and deletion of widget instances.
 */
class FocusWidgetReceiver : GlanceAppWidgetReceiver() {
    private val TAG = "FocusWidgetReceiver"
    
    override val glanceAppWidget: GlanceAppWidget = FocusWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "First widget instance created")
        // Update all widgets when first widget is added
        FocusWidgetManager.updateWidgets(context)
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "Widget update requested for ${appWidgetIds.size} widget(s)")
        // Update all widgets when the system requests an update
        FocusWidgetManager.updateWidgets(context)
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Last widget instance removed")
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Handle additional broadcasts that might require widget updates
        when (intent.action) {
            // You can add custom actions here if needed
            "com.zenithtasks.action.FOCUS_STATE_CHANGED" -> {
                Log.d(TAG, "Focus state changed, updating widgets")
                FocusWidgetManager.updateWidgets(context)
            }
            AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED -> {
                Log.d(TAG, "Widget size changed")
                FocusWidgetManager.updateWidgets(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Device booted, updating widgets")
                FocusWidgetManager.updateWidgets(context)
            }
        }
    }
}