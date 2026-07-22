package com.example.widget

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle

class WidgetHostManager(private val context: Context) {

    companion object {
        const val HOST_ID = 1024
        const val REQUEST_PICK_APPWIDGET = 2048
        const val REQUEST_CREATE_APPWIDGET = 4096
    }

    private val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetHost: AppWidgetHost = AppWidgetHost(context, HOST_ID)

    fun startListening() {
        try {
            appWidgetHost.startListening()
        } catch (e: Exception) {
            // Handle listening start failure if permissions limited
        }
    }

    fun stopListening() {
        try {
            appWidgetHost.stopListening()
        } catch (e: Exception) {
            // Handle listening stop failure
        }
    }

    fun allocateAppWidgetId(): Int {
        return appWidgetHost.allocateAppWidgetId()
    }

    fun deleteAppWidgetId(appWidgetId: Int) {
        appWidgetHost.deleteAppWidgetId(appWidgetId)
    }

    fun createWidgetView(context: Context, appWidgetId: Int): AppWidgetHostView? {
        val appWidgetInfo: AppWidgetProviderInfo? = appWidgetManager.getAppWidgetInfo(appWidgetId)
        return if (appWidgetInfo != null) {
            appWidgetHost.createView(context, appWidgetId, appWidgetInfo)
        } else {
            null
        }
    }

    fun getInstalledProviders(): List<AppWidgetProviderInfo> {
        return appWidgetManager.installedProviders
    }
}
