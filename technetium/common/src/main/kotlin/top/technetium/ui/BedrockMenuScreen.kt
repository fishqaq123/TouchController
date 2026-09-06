/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import top.fifthlight.combine.backend.minecraft.render.v26_2.TextureImpl
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.height
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.combine.core.screen.LocalCloseHandler
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.BlackstoneTheme
import top.fifthlight.combine.theme.invoke
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.Text

/**
 * 仿基岩版主菜单(基岩版暂停菜单布局)。
 *
 * 布局(参照基岩版菜单截图):
 *  - 顶部:原版 "MINECRAFT" 大 logo(用 TextureImpl 加载原版 GUI 标题纹理,资源包改图会同步)
 *  - 中间一列三个大按钮:回到游戏 / 设置 / 保存并退出
 *
 * 说明:combine 是 Compose 风格,按钮点击回调直接写 onClick。
 *      设置按钮暂为占位(后续做二级 UI)。
 */
@Composable
fun BedrockMenuScreen() {
    BlackstoneTheme {
        val onClose = LocalCloseHandler.current
        Box(
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.width(300).padding(24),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12),
            ) {
                // 顶部原版 "MINECRAFT" 大 logo
                TitleLogo()

                // 三个大按钮:回到游戏 / 设置 / 保存并退出
                MenuButton("回到游戏") {
                    onClose.close() // 关闭本菜单,回到游戏
                }
                MenuButton("设置") {
                    // TODO: 后续做设置二级 UI。
                    onClose.close()
                }
                MenuButton("保存并退出") {
                    // 保存并退出到标题。
                    onClose.close()
                    disconnectAndReturnToTitle()
                }
            }
        }
    }
}

/** 原版 Minecraft 主菜单大 logo。用原版 GUI 标题纹理渲染(资源包改图会同步)。 */
@Composable
private fun TitleLogo() {
    val logo: Drawable = remember {
        TextureImpl(
            identifier = Identifier.withDefaultNamespace("gui/title/minecraft"),
            sprite = false,
        )
    }
    Box(
        modifier = Modifier
            .width(300)
            .height(80)
            .background(logo),
    )
}

/** 基岩风格大按钮封装。 */
@Composable
private fun MenuButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(28),
    ) {
        Text(text)
    }
}

/**
 * 保存并退出到标题。
 *
 * 用反射调用 ClientLevel.disconnect(Component),避免编译期依赖
 * com.mojang.brigadier.Message(Component 的超类型)——该库不在 Technetium 编译类路径上。
 */
private fun disconnectAndReturnToTitle() {
    val client = Minecraft.getInstance()
    val level = client.level ?: return
    try {
        val componentClass = Class.forName("net.minecraft.network.chat.Component")
        val literal = componentClass.getMethod("literal", String::class.java)
            .invoke(null, "保存并退出")
        val disconnect = level.javaClass.getMethod("disconnect", componentClass)
        disconnect.invoke(level, literal)
    } catch (_: Exception) {
        // 反射失败时静默忽略,避免崩溃(可后续改为日志)。
    }
}
