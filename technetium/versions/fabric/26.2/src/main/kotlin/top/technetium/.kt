/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.screen.ScreenFactoryFactory

/**
 * 打开「仿基岩版主菜单」的入口。
 *
 * 用法(在 Mixin 中):
 *   Minecraft client = Minecraft.getInstance();
 *   BedrockMenu.openFor(client);
 *
 * 原理见 combine hello_world 示例:
 *   ScreenFactoryFactory.of().getScreen(parent, title) { @Composable content }
 *   返回一个 MC 的 (combine)Screen 对象,再 client.gui.setScreen() 显示。
 */
object BedrockMenu {
    fun openFor(client: Minecraft) {
        val parent = client.gui.screen()
        val screen = ScreenFactoryFactory.of().getScreen(
            parent = parent,
            renderBackground = true,
            title = Text.literal("Technetium"),
        ) {
            BedrockMenuScreen()
        } as Screen
        // 从 Mixin(非 compose context)调用,直接 setScreen 显示。
        client.gui.setScreen(screen)
    }
}
