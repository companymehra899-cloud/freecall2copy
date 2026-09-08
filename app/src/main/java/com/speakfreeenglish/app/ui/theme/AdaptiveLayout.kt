package com.speakfreeenglish.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AdaptiveSpec(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val isCompactWidth: Boolean,
    val isCompactHeight: Boolean,
    val isSmallPhone: Boolean,
    val horizontalPadding: Dp,
    val pulseSize: Dp,
    val radarSize: Dp,
)

val LocalAdaptive = staticCompositionLocalOf {
    AdaptiveSpec(
        screenWidthDp = 360,
        screenHeightDp = 640,
        isCompactWidth = true,
        isCompactHeight = true,
        isSmallPhone = true,
        horizontalPadding = 14.dp,
        pulseSize = 180.dp,
        radarSize = 200.dp,
    )
}

@Composable
fun ProvideAdaptiveLayout(content: @Composable () -> Unit) {
    val config = LocalConfiguration.current
    val width = config.screenWidthDp
    val height = config.screenHeightDp
    val isCompactWidth = width < 380
    val isCompactHeight = height < 700
    val isSmallPhone = width < 400 || height < 760

    val spec = AdaptiveSpec(
        screenWidthDp = width,
        screenHeightDp = height,
        isCompactWidth = isCompactWidth,
        isCompactHeight = isCompactHeight,
        isSmallPhone = isSmallPhone,
        horizontalPadding = if (isCompactWidth) 12.dp else 20.dp,
        pulseSize = when {
            isCompactHeight -> 160.dp
            isCompactWidth -> 180.dp
            else -> 220.dp
        },
        radarSize = when {
            isCompactHeight -> 180.dp
            isCompactWidth -> 220.dp
            else -> 280.dp
        },
    )

    CompositionLocalProvider(
        LocalAdaptive provides spec,
        content = content,
    )
}
