/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.core.node.LocalInputHandler
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.theme.Theme
import top.fifthlight.combine.theme.blackstone.BlackstoneTextures
import top.fifthlight.combine.theme.blackstone.BlackstoneTheme

val LocalTechnetiumTheme = staticCompositionLocalOf { TechnetiumTheme() }

data class TechnetiumTheme(
    val borderBackgroundDark: Drawable = BlackstoneTextures.widget_background_background_dark,

    val appBarBackground: Drawable = BlackstoneTextures.widget_background_background_gray_title,

    val titleBoxBackground: Drawable = BlackstoneTextures.widget_background_background_lightgray_title,

    val base: Theme = BlackstoneTheme,
) {
    companion object {
        val default = TechnetiumTheme()

        @Composable
        inline operator fun invoke(crossinline block: @Composable TechnetiumTheme.() -> Unit) {
            default(block)
        }
    }
}

@Composable
inline operator fun TechnetiumTheme.invoke(crossinline block: @Composable TechnetiumTheme.() -> Unit) {
    CompositionLocalProvider(
        LocalTechnetiumTheme provides TechnetiumTheme(),
        LocalInputHandler provides TechnetiumInputHandler,
        LocalTheme provides base,
    ) {
        block()
    }
}