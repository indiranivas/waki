package com.whakaara.feature.alarm.map

import android.content.Context
import java.io.File
import org.osmdroid.config.Configuration

object OsmdroidInitializer {
    fun init(context: Context) {
        val appContext = context.applicationContext
        val config = Configuration.getInstance()

        val basePath = File(appContext.cacheDir, "osmdroid").apply { mkdirs() }
        val tileCache = File(basePath, "tiles").apply { mkdirs() }

        config.osmdroidBasePath = basePath
        config.osmdroidTileCache = tileCache

        config.load(
            appContext,
            appContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        // OSM tile servers require a descriptive User-Agent; apply after load so prefs cannot clear it.
        config.userAgentValue = "Waki/${appContext.packageName}"
    }
}
