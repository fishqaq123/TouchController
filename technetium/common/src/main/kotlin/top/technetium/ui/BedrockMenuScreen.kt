/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui

import androidx.compose.runtime.Composable
import net.minecraft.client.Minecraft
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.placement.fillMaxHeight
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.height
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.screen.LocalCloseHandler
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.BlackstoneTheme
import top.fifthlight.combine.theme.invoke
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.Text

/**
 * 仿基岩版主菜单。
 *
 * 布局(用户规定):
 *  - 屏幕正中间一条灰色辅助线(竖线);
 *  - 辅助线右侧全部铺成与辅助线相同的灰色(右半区蒙版);
 *  - 辅助线左侧单独布置:
 *    - 按钮列(回到游戏 / 设置 / 保存并退出),上下无间隔、紧凑;
 *    - 按钮在"屏幕左侧与辅助线之间"居中。
 *
 * 注:顶部 logo 暂不渲染(等用户提供本地图片做内部引用)。
 */
@Composable
fun BedrockMenuScreen() {
    val gray = Color(0xFF808080u)
    BlackstoneTheme {
        val onClose = LocalCloseHandler.current
        Row(modifier = Modifier.fillMaxSize()) {
            // 左半区:按钮列垂直居中,按钮上下紧凑无间隔
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.width(200),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0),
                ) {
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

            // 中间灰色辅助线(竖线)
            Box(
                modifier = Modifier
                    .width(3)
                    .fillMaxHeight()
                    .background(gray),
            )

            // 右半区:全部铺成跟辅助线一样的灰色(蒙版)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(gray),
            )
        }
    }
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
