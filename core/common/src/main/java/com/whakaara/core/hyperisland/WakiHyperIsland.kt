package com.whakaara.core.hyperisland

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.whakaara.core.LogUtils.logD
import io.github.d4viddf.hyperisland_kit.HyperAction
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo
import io.github.d4viddf.hyperisland_kit.models.TimerInfo

/**
 * Xiaomi HyperOS Super Island / focus notification helper.
 *
 * Collapsed island uses [param_island] digit templates.
 * Expanded island uses chat/base templates with [TimerInfo] so the live time is visible.
 */
object WakiHyperIsland {
    private const val ICON_KEY = "waki_icon"
    private const val PIC_PREFIX = "miui.focus.pic_"
    private const val ACTION_PREFIX = "miui.focus.action_"
    private const val WAKI_ORANGE = "#FF6A3D"
    private const val ISLAND_BG = "#FF1C1C1E"
    private const val BUTTON_BG = "#FF3A3A3C"
    private const val BUTTON_TEXT = "#FFFFFFFF"
    private const val BROADCAST = 2

    fun isIslandDevice(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty().lowercase()
        val brand = Build.BRAND.orEmpty().lowercase()
        val xiaomiFamily = listOf("xiaomi", "redmi", "poco", "blackshark")
            .any { manufacturer.contains(it) || brand.contains(it) }
        return xiaomiFamily && isSupportIsland()
    }

    fun clearFocusExtras(builder: NotificationCompat.Builder) {
        val extras = builder.extras ?: return
        extras.keySet()
            .filter { it.startsWith("miui.focus.") }
            .toList()
            .forEach { extras.remove(it) }
    }

    fun applyCountdown(
        context: Context,
        builder: NotificationCompat.Builder,
        label: String,
        endTimeMillis: Long,
        totalDurationMs: Long,
        primary: IslandAction,
        secondary: IslandAction,
    ): NotificationCompat.Builder {
        if (!isIslandDevice(context)) return builder
        return runCatching {
            clearFocusExtras(builder)
            val now = System.currentTimeMillis()
            val remaining = (endTimeMillis - now).coerceAtLeast(0L)
            val total = totalDurationMs.coerceAtLeast(remaining).coerceAtLeast(1L)
            val progress = ((total - remaining) * 100 / total).toInt().coerceIn(0, 100)
            val timer = TimerInfo(
                timerType = -1,
                timerWhen = endTimeMillis,
                timerTotal = total,
                timerSystemCurrent = now,
            )

            val island = baseIsland(context, business = "waki_timer", ticker = label)
                // Expanded: chat template shows live timer digits (not a static title).
                .setChatInfo(
                    title = label,
                    timer = timer,
                    pictureKey = ICON_KEY,
                    actionKeys = listOf(primary.key, secondary.key),
                    titleColor = "#FFFFFF",
                    contentColor = WAKI_ORANGE,
                )
                // Collapsed: digit countdown in the capsule.
                .setBigIslandCountdown(endTimeMillis, ICON_KEY)
                .setSmallIslandCircularProgress(ICON_KEY, progress, WAKI_ORANGE)

            wireActions(island, primary, secondary)
            addAppIcon(context, island)
            attachExtras(builder, island, primary, secondary, context)
        }.getOrElse {
            logD(message = "Hyper Island countdown skipped", throwable = it)
            builder
        }
    }

    fun applyCountUp(
        context: Context,
        builder: NotificationCompat.Builder,
        label: String,
        startTimeMillis: Long,
        primary: IslandAction,
        secondary: IslandAction,
    ): NotificationCompat.Builder {
        if (!isIslandDevice(context)) return builder
        return runCatching {
            clearFocusExtras(builder)
            val now = System.currentTimeMillis()
            val timer = TimerInfo(
                timerType = 1,
                timerWhen = startTimeMillis,
                timerTotal = startTimeMillis,
                timerSystemCurrent = now,
            )

            val island = baseIsland(context, business = "waki_stopwatch", ticker = label)
                .setChatInfo(
                    title = label,
                    timer = timer,
                    pictureKey = ICON_KEY,
                    actionKeys = listOf(primary.key, secondary.key),
                    titleColor = "#FFFFFF",
                    contentColor = WAKI_ORANGE,
                )
                .setBigIslandCountUp(startTimeMillis, ICON_KEY)
                .setSmallIsland(ICON_KEY)

            wireActions(island, primary, secondary)
            addAppIcon(context, island)
            attachExtras(builder, island, primary, secondary, context)
        }.getOrElse {
            logD(message = "Hyper Island count-up skipped", throwable = it)
            builder
        }
    }

    fun applyStatic(
        context: Context,
        builder: NotificationCompat.Builder,
        title: String,
        content: String,
        business: String,
        primary: IslandAction? = null,
        secondary: IslandAction? = null,
        expandOnShow: Boolean = false,
    ): NotificationCompat.Builder {
        if (!isIslandDevice(context)) return builder
        return runCatching {
            clearFocusExtras(builder)
            val actionKeys = listOfNotNull(primary?.key, secondary?.key).ifEmpty { null }
            val island = baseIsland(context, business = business, ticker = content.ifBlank { title })
                .setIslandFirstFloat(expandOnShow)
                .setEnableFloat(expandOnShow)
                .setBaseInfo(
                    title = content.ifBlank { title },
                    content = title,
                    pictureKey = ICON_KEY,
                    actionKeys = actionKeys,
                    colorTitle = "#FFFFFF",
                    colorContent = "#FFAAAAAA",
                )
                .setBigIslandInfo(
                    left = ImageTextInfoLeft(
                        type = 1,
                        picInfo = PicInfo(type = 1, pic = ICON_KEY),
                        textInfo = TextInfo(
                            title = content.ifBlank { title },
                            content = title,
                        ),
                    ),
                )
                .setSmallIsland(ICON_KEY)

            if (primary != null) {
                wireActions(island, primary, secondary)
            }
            addAppIcon(context, island)
            attachExtras(builder, island, primary, secondary, context)
        }.getOrElse {
            logD(message = "Hyper Island static skipped", throwable = it)
            builder
        }
    }

    data class IslandAction(
        val key: String,
        val label: String,
        val pendingIntent: PendingIntent,
    )

    private fun baseIsland(
        context: Context,
        business: String,
        ticker: String,
    ): HyperIslandNotification =
        HyperIslandNotification.Builder(context, business, ticker)
            .setScene("timer")
            .setIslandFirstFloat(false)
            .setEnableFloat(false)
            .setShowNotification(true)
            .setIslandConfig(
                priority = 2,
                dismissible = false,
                maxSize = false,
                highlightColor = WAKI_ORANGE,
            )
            .setBackground(color = ISLAND_BG, type = 1)
            .setPadding(true)
            .setShowSmallIcon(true)
            .setHideDeco(false)

    /** Compact actions for expanded island — avoid setTextButtons (oversized pills). */
    private fun wireActions(
        island: HyperIslandNotification,
        primary: IslandAction,
        secondary: IslandAction?,
    ) {
        island.addAction(compactAction(primary))
        if (secondary != null) {
            island.addAction(compactAction(secondary))
        }
    }

    private fun compactAction(action: IslandAction): HyperAction =
        HyperAction(
            key = action.key,
            title = action.label,
            pendingIntent = action.pendingIntent,
            actionIntentType = BROADCAST,
            bgColor = BUTTON_BG,
            titleColor = BUTTON_TEXT,
        )

    private fun addAppIcon(context: Context, island: HyperIslandNotification) {
        appIconBitmap(context)?.let { bitmap ->
            island.addPicture(HyperPicture(key = ICON_KEY, bitmap = bitmap))
        }
    }

    private fun attachExtras(
        builder: NotificationCompat.Builder,
        island: HyperIslandNotification,
        primary: IslandAction?,
        secondary: IslandAction?,
        context: Context,
    ): NotificationCompat.Builder {
        val resourceBundle = Bundle()
        val picsBundle = Bundle()
        appIconBitmap(context)?.let { bitmap ->
            picsBundle.putParcelable(PIC_PREFIX + ICON_KEY, Icon.createWithBitmap(bitmap))
        }
        resourceBundle.putBundle("miui.focus.pics", picsBundle)

        val actionsBundle = Bundle()
        listOfNotNull(primary, secondary).forEach { action ->
            val transparent = Icon.createWithResource(
                context,
                android.R.drawable.screen_background_light_transparent,
            )
            actionsBundle.putParcelable(
                ACTION_PREFIX + action.key,
                Notification.Action.Builder(transparent, action.label, action.pendingIntent).build(),
            )
        }
        resourceBundle.putBundle("miui.focus.actions", actionsBundle)

        // Also merge library bundle when permission allows (icons for template actions).
        runCatching { builder.addExtras(island.buildResourceBundle()) }

        builder.addExtras(resourceBundle)
        val extras = builder.extras ?: Bundle()
        extras.putString("miui.focus.param", island.buildJsonParam())
        builder.setExtras(extras)
        return builder
    }

    private fun isSupportIsland(): Boolean =
        try {
            val method = Class.forName("android.os.SystemProperties")
                .getDeclaredMethod("getBoolean", String::class.java, Boolean::class.javaPrimitiveType)
            method.invoke(null, "persist.sys.feature.island", false) as Boolean
        } catch (_: Exception) {
            try {
                val method = Class.forName("android.os.SystemProperties")
                    .getDeclaredMethod("get", String::class.java, String::class.java)
                method.invoke(null, "persist.sys.feature.island", "0") == "1"
            } catch (_: Exception) {
                false
            }
        }

    private fun appIconBitmap(context: Context): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, context.applicationInfo.icon) ?: return null
        val width = drawable.intrinsicWidth.coerceAtLeast(48)
        val height = drawable.intrinsicHeight.coerceAtLeast(48)
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
